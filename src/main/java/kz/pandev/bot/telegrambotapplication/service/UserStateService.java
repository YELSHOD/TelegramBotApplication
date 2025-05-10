package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.enums.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {

    private final Map<String, UserState> userStates = new ConcurrentHashMap<>();

    public void setUserStates(String chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getUserState(String chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE);
    }

    public void clearStates(String chatId) {
        userStates.remove(chatId);
    }
}
