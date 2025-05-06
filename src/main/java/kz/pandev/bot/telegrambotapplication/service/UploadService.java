package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final CategoryRepository categoryRepository;

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

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    String parentName = row.getCell(0).getStringCellValue().trim();
                    String childName = row.getCell(1).getStringCellValue().trim();

                    Category parent = categoryRepository.findByName(parentName)
                            .orElseGet(() -> {
                                Category newParent = new Category(parentName, null);
                                categoryRepository.save(newParent);
                                log.info("Создана новая родительская категория: {}", parentName);
                                return newParent;
                            });

                    if (!childName.isEmpty()) {
                        Optional<Category> existingChild = categoryRepository.findByName(childName);
                        if (existingChild.isEmpty()) {
                            categoryRepository.save(new Category(childName, parent));
                            log.info("Создана дочерняя категория: {} -> {}", parentName, childName);
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



    public void sendExcelTemplate(String chatId, TelegramLongPollingBot bot) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Categories");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Parent Category");
            header.createCell(1).setCellValue("Child Category");

            var example = sheet.createRow(1);
            example.createCell(0).setCellValue("Food");
            example.createCell(1).setCellValue("Fruits");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            bot.execute(SendDocument.builder()
                    .chatId(chatId)
                    .document(new InputFile(inputStream, "category_template.xlsx"))
                    .caption("📎 Вот шаблон для загрузки категорий.")
                    .build());

            log.info("Шаблон Excel отправлен пользователю: chatId={}", chatId);

        } catch (Exception e) {
            log.error("Ошибка при отправке шаблона Excel", e);
        }
    }
}
