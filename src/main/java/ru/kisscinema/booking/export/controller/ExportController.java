package ru.kisscinema.booking.export.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.export.service.ExportService;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * POST /api/admin/export
     * Сохранить дамп в файл внутри проекта (папка exports/).
     * Возвращает имя созданного файла.
     */
    @PostMapping("/export")
    public ResponseEntity<String> exportData() throws IOException {
        exportService.exportToFile();
        return ResponseEntity.ok("Экспорт успешно сохранён в папку 'exports/'");
    }

    /**
     * POST /api/admin/import
     * Восстановить данные из файла.
     * Параметры:
     * - filename (опционально): имя файла в папке exports/
     * - если не указан — берётся самый свежий
     */
    @PostMapping("/import")
    public ResponseEntity<String> importData(@RequestParam String filename) throws IOException {
        exportService.importFromFile(filename);
        return ResponseEntity.ok("Данные успешно восстановлены из файла: " + filename);
    }
}