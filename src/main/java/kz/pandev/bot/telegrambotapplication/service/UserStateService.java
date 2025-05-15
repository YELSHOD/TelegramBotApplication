package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.enums.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис управления состояниями пользователей в Telegram-боте.
 * Позволяет отслеживать, на каком этапе взаимодействия находится конкретный пользователь.
 */
@Service
public class UserStateService {

    /**
     * Хранилище состояний пользователей.
     * Ключ — идентификатор чата (chatId), значение — текущее состояние пользователя.
     * Используется потокобезопасная реализация {@link ConcurrentHashMap}.
     */
    private final Map<String, UserState> userStates = new ConcurrentHashMap<>();

    /**
     * Устанавливает состояние пользователя.
     *
     * @param chatId уникальный идентификатор чата пользователя
     * @param state  новое состояние, которое требуется сохранить
     */
    public void setUserStates(String chatId, UserState state) {
        userStates.put(chatId, state);
    }

    /**
     * Получает текущее состояние пользователя.
     * Если состояние отсутствует, возвращается {@link UserState#NONE}.
     *
     * @param chatId идентификатор чата пользователя
     * @return текущее состояние пользователя
     */
    public UserState getUserState(String chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE);
    }

    /**
     * Очищает (удаляет) состояние пользователя из хранилища.
     *
     * @param chatId идентификатор чата пользователя
     */
    public void clearStates(String chatId) {
        userStates.remove(chatId);
    }
}
