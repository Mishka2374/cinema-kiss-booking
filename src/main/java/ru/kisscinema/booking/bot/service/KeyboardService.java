package ru.kisscinema.booking.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.dto.SeatDtoFull;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeyboardService {

    // ======================= ДНИ =========================

    public InlineKeyboardMarkup getDaySelectionKeyboard() {
        List<List<InlineKeyboardButton>> rows = List.of(
                List.of(
                        btn("Сегодня", "day_0"),
                        btn("Завтра", "day_1"),
                        btn("Послезавтра", "day_2")
                )
        );
        return markup(rows);
    }

    // ===================== СЕАНСЫ ========================

    public InlineKeyboardMarkup getMoviesKeyboard(List<Session> sessions, int dayIndex) {

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Session s : sessions) {
            String label =
                    s.getStartTime().format(timeFmt)
                            + " | " + s.getMovie().getTitle()
                            + " | " + s.getPrice() + "₽";

            rows.add(
                    List.of(
                            btn(label, "movie_" + s.getId() + "_" + dayIndex)
                    )
            );
        }

        // назад
        rows.add(List.of(btn("⬅️ Назад", "back_to_days")));

        return markup(rows);
    }

    // ===================== МЕСТА ========================

    public InlineKeyboardMarkup getSeatsKeyboard(
            Long sessionId,
            List<SeatDtoFull> seats,
            int dayIndex
    ) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // группировка по рядам
        Map<Integer, List<SeatDtoFull>> grouped =
                seats.stream()
                        .collect(Collectors.groupingBy(SeatDtoFull::rowNumber));

        // определяем максимальное количество мест в ряду
        int maxSeatNumber = seats.stream()
                .mapToInt(SeatDtoFull::seatNumber)
                .max()
                .orElse(0);

        // строим сетку мест
        grouped.keySet().stream().sorted().forEach(rowNum -> {

            Map<Integer, SeatDtoFull> seatMap = grouped.get(rowNum).stream()
                    .collect(Collectors.toMap(
                            SeatDtoFull::seatNumber,
                            s -> s
                    ));

            List<InlineKeyboardButton> line = new ArrayList<>();

            for (int seatNum = 1; seatNum <= maxSeatNumber; seatNum++) {
                InlineKeyboardButton b = new InlineKeyboardButton();

                SeatDtoFull seat = seatMap.get(seatNum);

                if (seat == null) {
                    // если seat отсутствует в БД — рисуем пустую клетку
                    b.setText(" ");
                    b.setCallbackData("ignore");
                }
                else if (!seat.taken()) {
                    // свободное место
                    b.setText(rowNum + "-" + seatNum);
                    b.setCallbackData(
                            "seat_" + sessionId + "_" + rowNum + "_" + seatNum + "_" + dayIndex
                    );
                } else if (seat.used()) {
                    // место уже использовано на кассе
                    b.setText("🟩");
                    b.setCallbackData("ignore");

                } else if (seat.mine()) {
                    b.setText("🟦");
                    b.setCallbackData(
                            "myseat_" + sessionId + "_" + rowNum + "_" + seatNum + "_" + dayIndex
                    );

                } else {
                    b.setText("❌");
                    b.setCallbackData("ignore");
                }

                line.add(b);
            }

            rows.add(line);
        });

        // назад к сеансам
        rows.add(
                List.of(
                        btn("⬅️ Назад", "back_to_movies_" + dayIndex)
                )
        );

        return markup(rows);
    }

    // ===================== ПОДТВЕРЖДЕНИЕ отмены ========================

    public InlineKeyboardMarkup getConfirmCancelKeyboard(Long sessionId, int row, int seat, int dayIndex) {

        List<List<InlineKeyboardButton>> rows = List.of(
                List.of(
                        btn("✅ Да", "confirmcancel_" + sessionId + "_" + row + "_" + seat + "_" + dayIndex),
                        btn("❌ Нет", "cancel_" + sessionId + "_" + dayIndex)
                )
        );

        return markup(rows);
    }

    // ===================== HELPERS ========================

    private InlineKeyboardButton btn(String text, String callback) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(callback);
        return b;
    }

    private InlineKeyboardMarkup markup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }
}
