package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final CategoryRepository categoryRepository;

    /**
     * Обрабатывает Excel-файл, загруженный пользователем, и сохраняет содержащиеся в нём категории и подкатегории.
     * Показывает пользователю прогресс выполнения и отправляет отчёт об успешно добавленных и пропущенных (дублирующихся) записях.
     *
     * @param fileId идентификатор файла, предоставленный Telegram API
     * @param chatId ID чата, в который будут отправляться сообщения
     * @param bot    экземпляр Telegram-бота для выполнения команд
     */
    public void processExcelFile(String fileId, String chatId, TelegramLongPollingBot bot) {
        try {
            File file = bot.execute(new GetFile(fileId));
            String filePath = file.getFilePath();
            String fullUrl = "https://api.telegram.org/file/bot" + bot.getBotToken() + "/" + filePath;

            log.info("Загрузка Excel файла с URL: {}", fullUrl);

            // Первичное сообщение о прогрессе
            Message progressMessage = bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("📊 Прогресс: 0%")
                    .build());

            int messageId = progressMessage.getMessageId();

            try (InputStream inputStream = new URL(fullUrl).openStream();
                 XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

                var sheet = workbook.getSheetAt(0);
                int rowCount = sheet.getPhysicalNumberOfRows();
                int processedRows = 0;
                int lastProgress = -1;

                List<String> duplicates = new ArrayList<>();
                List<String> addedCategories = new ArrayList<>(); // Список добавленных категорий

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Пропускаем заголовок

                    String parentName = row.getCell(0).getStringCellValue().trim();
                    String childName = row.getCell(1).getStringCellValue().trim();

                    // Находим или создаём родительскую категорию
                    Category parent = categoryRepository.findByName(parentName)
                            .orElseGet(() -> {
                                Category newParent = new Category(parentName, null);
                                categoryRepository.save(newParent);
                                log.info("Создана новая родительская категория: {}", parentName);
                                return newParent;
                            });

                    // Добавляем подкатегорию
                    if (!childName.isEmpty()) {
                        Optional<Category> existingChild = categoryRepository.findByName(childName);
                        if (existingChild.isEmpty()) {
                            categoryRepository.save(new Category(childName, parent));
                            log.info("Создана дочерняя категория: {} -> {}", parentName, childName);
                            addedCategories.add("• " + parentName + " -> " + childName); // Добавляем в список добавленных категорий
                        } else {
                            log.info("Пропущена уже существующая категория: {}", childName);
                            duplicates.add(childName);
                        }
                    }

                    processedRows++;
                    int progress = (int) ((double) processedRows / rowCount * 100);

                    if (progress != lastProgress) {
                        lastProgress = progress;
                        bot.execute(EditMessageText.builder()
                                .chatId(chatId)
                                .messageId(messageId)
                                .text("📊 Прогресс: " + progress + "%")
                                .build());
                    }
                }

                // Финальное сообщение об успехе
                bot.execute(EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("✅ Excel файл успешно обработан.")
                        .build());

                log.info("Файл успешно обработан и сохранён: chatId={}", chatId);

                // Если есть дубликаты — отправить отдельным сообщением
                if (!duplicates.isEmpty()) {
                    StringBuilder duplicateMsg = new StringBuilder("⚠️ Найдены дубликаты (уже существуют в базе):\n");
                    for (String dup : duplicates) {
                        duplicateMsg.append("• ").append(dup).append("\n");
                    }

                    bot.execute(SendMessage.builder()
                            .chatId(chatId)
                            .text(duplicateMsg.toString())
                            .build());
                }

                // Сообщаем о добавленных категориях
                if (!addedCategories.isEmpty()) {
                    StringBuilder addedCategoriesMsg = new StringBuilder("✅ Добавленные категории и подкатегории:\n");
                    for (String addedCategory : addedCategories) {
                        addedCategoriesMsg.append(addedCategory).append("\n");
                    }

                    bot.execute(SendMessage.builder()
                            .chatId(chatId)
                            .text(addedCategoriesMsg.toString())
                            .build());
                }

            }

        } catch (Exception e) {
            log.error("❌ Ошибка при обработке Excel файла", e);
            try {
                bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("❌ Произошла ошибка при обработке Excel файла.")
                        .build());
            } catch (Exception ex) {
                log.error("Ошибка при отправке сообщения об ошибке", ex);
            }
        }
    }

    /**
     * Генерирует шаблон Excel-файла для импорта категорий и отправляет его пользователю.
     * Шаблон содержит примеры категорий и подкатегорий, отформатирован с выравниванием и заголовками.
     *
     * @param chatId ID чата, куда будет отправлен файл
     * @param bot    экземпляр Telegram-бота для выполнения команды отправки
     */
    public void sendExcelTemplate(String chatId, TelegramLongPollingBot bot) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Categories");

            sheet.setColumnWidth(0, 20 * 256); // Столбец A
            sheet.setColumnWidth(1, 20 * 256); // Столбец B
            sheet.setColumnWidth(2, 20 * 256); // Столбец C (пустой)

            var headerFont = workbook.createFont();
            headerFont.setBold(true); // Жирный шрифт
            var headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Стиль для обычных ячеек (центрирование)
            var cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Заголовок (первая строка)
            var header = sheet.createRow(0);
            header.setHeightInPoints(25); // Фиксированная высота
            header.createCell(0).setCellValue("Категория");
            header.getCell(0).setCellStyle(headerStyle); // Применяем жирный стиль
            header.createCell(1).setCellValue("Подкатегория");
            header.getCell(1).setCellStyle(headerStyle);
            header.createCell(2).setCellValue(""); // Пустая ячейка
            header.getCell(2).setCellStyle(headerStyle); // Стиль для пустой ячейки заголовка

            // Данные (остальные строки)
            String[][] exampleData = {
                    {"Напитки", "Соса-Соlа", ""},
                    {"Напитки", "Fanta", ""}
            };

            for (int i = 0; i < exampleData.length; i++) {
                var row = sheet.createRow(i + 1); // Начинаем со второй строки
                row.setHeightInPoints(25); // Фиксированная высота
                for (int j = 0; j < exampleData[i].length; j++) {
                    row.createCell(j).setCellValue(exampleData[i][j]);
                    row.getCell(j).setCellStyle(cellStyle); // Центрирование
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            bot.execute(SendDocument.builder()
                    .chatId(chatId)
                    .document(new InputFile(inputStream, "category_template.xlsx"))
                    .caption("\uD83D\uDCCE Вот шаблон для загрузки категорий.")
                    .build());

        } catch (Exception e) {
            log.error("Ошибка при создании шаблона", e);
        }
    }
}
