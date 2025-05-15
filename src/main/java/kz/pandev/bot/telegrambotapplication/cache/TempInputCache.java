package kz.pandev.bot.telegrambotapplication.cache;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис временного кэширования пользовательского ввода.
 * <p>
 * Используется для хранения промежуточных данных во время диалога с пользователем,
 * например, имени выбранной родительской категории перед вводом подкатегории.
 * <p>
 * Позволяет избежать ошибок, связанных с преждевременной отправкой или случайным нажатием inline-кнопок,
 * которые могут некорректно повлиять на логическую цепочку добавления элементов.
 */
@Service
public class TempInputCache {

    /**
     * Временное хранилище пользовательского ввода, ключ — chatId Telegram-пользователя.
     */
    private final Map<Long, String> inputCache = new ConcurrentHashMap<>();

    /**
     * Сохраняет временное значение для указанного пользователя.
     *
     * @param chatId ID чата пользователя
     * @param value  временное значение (например, имя родительской категории)
     */
    public void save(Long chatId, String value) {
        inputCache.put(chatId, value);
    }

    /**
     * Возвращает сохранённое временное значение по chatId.
     *
     * @param chatId ID чата пользователя
     * @return сохранённое значение или {@code null}, если данных нет
     */
    public String get(Long chatId) {
        return inputCache.get(chatId);
    }

    /**
     * Удаляет временное значение по chatId.
     *
     * @param chatId ID чата пользователя
     */
    public void clear(Long chatId) {
        inputCache.remove(chatId);
    }
}
