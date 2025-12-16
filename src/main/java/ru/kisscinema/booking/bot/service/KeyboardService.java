package ru.kisscinema.booking.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.dto.SeatDtoFull;

import java.math.BigDecimal;
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

        // Находим общее количество рядов и делаем ее final
        final int totalRows = Math.max(grouped.size(), 1);

        // Создаем заголовок (первая строка)
        List<InlineKeyboardButton> headerRow = new ArrayList<>();

        // Одна кнопка заголовка для информации о ряде
        InlineKeyboardButton headerInfoBtn = new InlineKeyboardButton();
        headerInfoBtn.setText("Ряд | Тип | Цена");
        headerInfoBtn.setCallbackData("ignore");
        headerRow.add(headerInfoBtn);

        // Номера мест в заголовке
        for (int seatNum = 1; seatNum <= maxSeatNumber; seatNum++) {
            InlineKeyboardButton seatNumBtn = new InlineKeyboardButton();
            seatNumBtn.setText("М" + seatNum);
            seatNumBtn.setCallbackData("ignore");
            headerRow.add(seatNumBtn);
        }
        rows.add(headerRow);

        // Строим сетку мест с объединенной информацией о рядах
        grouped.keySet().stream().sorted().forEach(rowNum -> {
            List<InlineKeyboardButton> line = new ArrayList<>();

            // Определяем тип ряда
            String rowType = getRowType(rowNum, totalRows);
            String rowTypeEmoji = getRowTypeEmoji(rowType);

            // Получаем цену для этого ряда
            BigDecimal rowPrice = grouped.get(rowNum).stream()
                    .findFirst()
                    .map(SeatDtoFull::price)
                    .orElse(BigDecimal.ZERO);
            String formattedPrice = rowPrice.stripTrailingZeros().toPlainString() + "₽";

            // Единая кнопка с информацией о ряде
            InlineKeyboardButton rowInfoBtn = new InlineKeyboardButton();
            rowInfoBtn.setText(rowTypeEmoji + " " + rowNum + " | " + rowType + " | " + formattedPrice);
            rowInfoBtn.setCallbackData("ignore");
            line.add(rowInfoBtn);

            // Получаем места текущего ряда
            Map<Integer, SeatDtoFull> seatMap = grouped.get(rowNum).stream()
                    .collect(Collectors.toMap(
                            SeatDtoFull::seatNumber,
                            s -> s
                    ));

            // Добавляем кнопки мест
            for (int seatNum = 1; seatNum <= maxSeatNumber; seatNum++) {
                InlineKeyboardButton seatBtn = new InlineKeyboardButton();
                SeatDtoFull seat = seatMap.get(seatNum);

                if (seat == null) {
                    // если место отсутствует в БД — рисуем пустую клетку
                    seatBtn.setText("⬜");
                    seatBtn.setCallbackData("ignore");
                } else if (!seat.taken()) {
                    // свободное место
                    seatBtn.setText("🟩" + seatNum);
                    seatBtn.setCallbackData(
                            "seat_" + sessionId + "_" + rowNum + "_" + seatNum + "_" + dayIndex
                    );
                } else if (seat.used()) {
                    // место уже использовано на кассе
                    seatBtn.setText("🟫");
                    seatBtn.setCallbackData("ignore");
                } else if (seat.mine()) {
                    // мое забронированное место
                    seatBtn.setText("🔵" + seatNum);
                    seatBtn.setCallbackData(
                            "myseat_" + sessionId + "_" + rowNum + "_" + seatNum + "_" + dayIndex
                    );
                } else {
                    // занятое другим пользователем место
                    seatBtn.setText("🟥");
                    seatBtn.setCallbackData("ignore");
                }

                line.add(seatBtn);
            }

            rows.add(line);
        });

        // Добавляем информацию о типах рядов и ценах
        List<InlineKeyboardButton> infoRow = new ArrayList<>();
        InlineKeyboardButton infoBtn = new InlineKeyboardButton();

        // Получаем информацию о ценах для каждого типа
        String frontPrice = "?";
        String middlePrice = "?";
        String backPrice = "?";

        // Ищем цены для каждого типа рядов
        for (Integer rowNum : grouped.keySet()) {
            String type = getRowType(rowNum, totalRows);
            String price = grouped.get(rowNum).stream()
                    .findFirst()
                    .map(seat -> seat.price().stripTrailingZeros().toPlainString())
                    .orElse("?");

            switch (type) {
                case "Не до поцелуя" -> frontPrice = price;
                case "Идеал" -> middlePrice = price;
                case "Поцел" -> backPrice = price;
            }
        }

        infoBtn.setText(
                "🚫💋 Не до поцелуя: " + frontPrice + "₽ | " +
                        "🌟 Идеальное: " + middlePrice + "₽ | " +
                        "💋 Для поцелуев: " + backPrice + "₽");
        infoBtn.setCallbackData("ignore");
        infoRow.add(infoBtn);
        rows.add(infoRow);

        // Добавляем легенду
        List<InlineKeyboardButton> legendRow = new ArrayList<>();
        InlineKeyboardButton legendBtn = new InlineKeyboardButton();
        legendBtn.setText("🟩 Свободно | 🔵 Ваше | 🟥 Занято | 🟫 Касса");
        legendBtn.setCallbackData("ignore");
        legendRow.add(legendBtn);
        rows.add(legendRow);

        // назад к сеансам
        rows.add(
                List.of(
                        btn("⬅️ Назад", "back_to_movies_" + dayIndex)
                )
        );

        return markup(rows);
    }

    // Вспомогательный метод для определения типа ряда
    private String getRowType(int rowNumber, int totalRows) {
        if (totalRows <= 1) {
            return "Стандарт";
        }

        if (rowNumber <= 1) { // Первый ряд
            return "Не до поцелуя";
        } else if (rowNumber >= totalRows) { // Последний ряд
            return "Поцел";
        } else {
            return "Идеал";
        }
    }

    // Вспомогательный метод для получения эмодзи типа ряда
    private String getRowTypeEmoji(String rowType) {
        return switch (rowType) {
            case "Не до поцелуя" -> "\uD83D\uDEAB\uD83D\uDC8B";
            case "Идеал" -> "\uD83C\uDF1F";
            case "Поцел" -> "\uD83D\uDC8B";
            default -> "🎬";
        };
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
