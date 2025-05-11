package kz.pandev.bot.telegrambotapplication.cache;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempInputCache {

    private final Map<Long, String> inputCache = new ConcurrentHashMap<>();

    // Сохраняем временное значение (например, имя родительской категории)
    public void save(Long chatId, String value) {
        inputCache.put(chatId, value);
    }

    // Получаем временное значение
    public String get(Long chatId) {
        return inputCache.get(chatId);
    }

    // Очищаем временное значение
    public void clear(Long chatId) {
        inputCache.remove(chatId);
    }
}
