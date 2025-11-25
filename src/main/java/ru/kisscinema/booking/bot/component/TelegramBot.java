package ru.kisscinema.booking.bot.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ru.kisscinema.booking.bot.config.BotConfig;
import ru.kisscinema.booking.bot.service.KeyboardService;
import ru.kisscinema.booking.session.service.SessionService;
import ru.kisscinema.booking.booking.service.BookingService;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.dto.SeatDtoFull;
import ru.kisscinema.booking.booking.dto.BookingRequestDto;
import ru.kisscinema.booking.booking.dto.BookingResponse;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final KeyboardService keyboardService;
    private final SessionService sessionService;
    private final BookingService bookingService;

    // ===================== START =======================

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
            return;
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    // ============= РЕГИСТРАЦИЯ КОМАНД ==================

    @Override
    public void onRegister() {
        try {
            this.execute(
                    new SetMyCommands(
                            List.of(new BotCommand("/start", "Запуск бота")),
                            new BotCommandScopeDefault(),
                            null
                    )
            );
        } catch (TelegramApiException ignored) {}
    }

    // ===================== MESSAGE =======================

    private void handleMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (text.equals("/start")) {
            send(chatId,
                    "🎬 Добро пожаловать!\n\nВыберите день:",
                    keyboardService.getDaySelectionKeyboard()
            );
        } else {
            send(chatId, "Неизвестная команда", null);
        }
    }

    // ===================== CALLBACK =======================

    private void handleCallback(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (data.startsWith("day_")) {
            int dayIndex = Integer.parseInt(data.split("_")[1]);
            showMovies(chatId, messageId, dayIndex);
            return;
        }

        if (data.startsWith("movie_")) {
            String[] p = data.split("_");
            Long sessionId = Long.parseLong(p[1]);
            int dayIndex = Integer.parseInt(p[2]);
            showSeats(chatId, messageId, sessionId, dayIndex);
            return;
        }

        if (data.startsWith("seat_")) {
            String[] p = data.split("_");
            Long sessionId = Long.parseLong(p[1]);
            int row = Integer.parseInt(p[2]);
            int seat = Integer.parseInt(p[3]);
            bookSeat(chatId, messageId, sessionId, row, seat);
            return;
        }

        if (data.startsWith("myseat_")) {
            String[] p = data.split("_");
            Long sessionId = Long.parseLong(p[1]);
            int row = Integer.parseInt(p[2]);
            int seat = Integer.parseInt(p[3]);
            int dayIndex = Integer.parseInt(p[4]);
            askCancel(chatId, messageId, sessionId, row, seat, dayIndex);
            return;
        }

        if (data.startsWith("confirmcancel_")) {
            confirmCancel(update);
            return;
        }

        if (data.startsWith("cancel_")) {
            returnCancel(update);
            return;
        }

        if (data.equals("back_to_days")) {
            edit(chatId, messageId,
                    "Выберите день:",
                    keyboardService.getDaySelectionKeyboard()
            );
            return;
        }

        if (data.startsWith("back_to_movies_")) {
            int dayIndex = Integer.parseInt(data.split("_")[3]);
            showMovies(chatId, messageId, dayIndex);
        }
    }

    // ===================== SHOW MOVIES =======================

    private void showMovies(long chatId, long msgId, int dayIndex) {
        LocalDate date = LocalDate.now().plusDays(dayIndex);
        List<Session> sessions = sessionService.getSessionsByDate(date);

        if (sessions.isEmpty()) {
            edit(chatId, msgId,
                    "❌ Нет сеансов на этот день",
                    keyboardService.getDaySelectionKeyboard()
            );
            return;
        }

        edit(chatId, msgId,
                "Выберите сеанс:",
                keyboardService.getMoviesKeyboard(sessions, dayIndex)
        );
    }

    // ===================== SHOW SEATS =======================

    private void showSeats(long chatId, long msgId, Long sessionId, int dayIndex) {

        List<SeatDtoFull> seats =
                bookingService.getSeatsFull(sessionId, chatId); // ты добавил этот метод

        if (seats.isEmpty()) {
            edit(chatId, msgId, "❌ Места не найдены", null);
            return;
        }

        edit(chatId, msgId,
                "Выберите место:",
                keyboardService.getSeatsKeyboard(sessionId, seats, dayIndex)
        );
    }

    // ===================== BOOK SEAT =======================

    private void bookSeat(long chatId, long msgId, Long sessionId, int row, int seat) {

        try {
            Long seatId = bookingService.getSeatId(sessionId, row, seat); // метод нужно добавить
            BookingResponse response = bookingService
                    .createBooking(new BookingRequestDto(sessionId, seatId), chatId);

            edit(chatId, msgId,
                    "🎉 Бронь создана!\n\n" +
                            "Фильм: " + response.movieTitle() + "\n" +
                            "Время: " + response.sessionTime() + "\n" +
                            "Ряд " + response.rowNumber() + ", место " + response.seatNumber() + "\n" +
                            "Цена " + response.price() + "\n" +
                            "Код: *" + response.bookingCode() + "*",
                    null
            );

        } catch (Exception e) {
            edit(chatId, msgId,
                    "❌ Ошибка: " + e.getMessage(),
                    null
            );
        }
    }

    // ===================== ASK CANCEL =======================

    private void askCancel(long chatId, long msgId, Long sessionId, int row, int seat, int dayIndex) {
        edit(chatId, msgId,
                "Вы действительно хотите отменить билет на место " + row + "-" + seat + "?",
                keyboardService.getConfirmCancelKeyboard(sessionId, row, seat, dayIndex)
        );
    }

    // ===================== CONFIRM CANCEL =======================

    private void confirmCancel(Update update) {
        String[] p = update.getCallbackQuery().getData().split("_");
        Long sessionId = Long.parseLong(p[1]);
        int row = Integer.parseInt(p[2]);
        int seat = Integer.parseInt(p[3]);

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long msgId = update.getCallbackQuery().getMessage().getMessageId();

        bookingService.cancelBookingByUser(sessionId, row, seat, chatId);

        edit(chatId, msgId,
                "✔️ Бронирование отменено",
                keyboardService.getDaySelectionKeyboard()
        );
    }

    // ===================== CANCEL RETURN =======================

    private void returnCancel(Update update) {
        String[] p = update.getCallbackQuery().getData().split("_");
        Long sessionId = Long.parseLong(p[1]);
        int dayIndex = Integer.parseInt(p[2]);

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long msgId = update.getCallbackQuery().getMessage().getMessageId();

        showSeats(chatId, msgId, sessionId, dayIndex);
    }


    // ===================== SEND & EDIT =======================

    private void send(long chatId, String text, InlineKeyboardMarkup kb) {
        try {
            SendMessage msg = new SendMessage(String.valueOf(chatId), text);
            if (kb != null) msg.setReplyMarkup(kb);
            msg.enableMarkdown(true);
            execute(msg);
        } catch (Exception ignored) {}
    }

    private void edit(long chatId, long msgId, String text, InlineKeyboardMarkup kb) {
        try {
            EditMessageText m = new EditMessageText();
            m.setChatId(String.valueOf(chatId));
            m.setMessageId((int) msgId);
            m.setText(text);
            m.enableMarkdown(true);
            if (kb != null) m.setReplyMarkup(kb);
            execute(m);
        } catch (Exception ignored) {}
    }
}
