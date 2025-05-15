package kz.pandev.bot.telegrambotapplication.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Команда для вывода справочной информации по доступным командам бота.
 */
@Slf4j
@Component
public class HelpCommand implements BotCommand {

    /**
     * Возвращает строку команды, с которой связан данный обработчик.
     * @return строка команды "/help"
     */
    @Override
    public String getCommand() {
        return "/help";
    }

    /**
     * Отправляет пользователю список доступных команд с кратким описанием.
     *
     * @param update объект входящего обновления от Telegram, содержащий данные о сообщении и чате
     * @param bot объект TelegramLongPollingBot, через который происходит отправка сообщений
     */
    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String helpText = """ 
        Доступные команды:
        
                📘 Справка
                 ➤ /help — Показывает доступные команды.
        
                ➕ Добавить элемент
                 ➤  Пользователь может создать новую категорию и указать родительскую.
 
                ➖ Удалить элемент
                 ➤  Можно удалить категорию и все её подкатегории рекурсивно.
       
                🌳 Дерево категорий
                 ➤  Вывод всех категорий в структурированном формате (дерево).

                📥 Скачать Excel
                 ➤  Пользователь может получить дерево категорий в виде .xlsx-файла.

                📤 Импорт Excel
                 ➤  Загрузите .xlsx файл — бот обработает и добавит категории в систему.

                👁 Просмотр категорий
                 ➤ Отображение всех категорий без иерархии, в плоском виде.
        """;

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(helpText);

        try {
            bot.execute(message);
            log.debug("Команда /help выполнена для чата {}", update.getMessage().getChatId());
        } catch (Exception e) {
            log.error("Ошибка при выполнении команды /help для чата {}: {}", update.getMessage().getChatId(), e.getMessage());
        }
    }
}

