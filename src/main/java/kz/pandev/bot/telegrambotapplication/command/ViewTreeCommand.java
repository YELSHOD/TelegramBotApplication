package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.util.TreeBuilder;
import kz.pandev.bot.telegrambotapplication.util.TreeNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewTreeCommand implements BotCommand {

    private final TreeBuilder treeBuilder;

    @Override
    public String getCommand() {
        return "/viewTree";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();

        try {
            // Строю дерево категорий с помощью TreeBuilder (null — корень)
            TreeNode root = treeBuilder.buildTree(null);

            // Форматирую дерево в читаемый текст для Telegram
            String treeString = formatTreeWithParentIndicator(root, 0);

            // Отправляю сообщение с деревом
            SendMessage message = new SendMessage(chatId, treeString);
            bot.execute(message);

        } catch (Exception e) {
            // Если что-то пошло не так — логирую и сообщаю пользователю
            log.error("Ошибка при построении дерева: {}", e.getMessage());
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при построении дерева категорий."));
            } catch (Exception ex) {
                log.error("Ошибка при отправке сообщения об ошибке: {}", ex.getMessage());
            }
        }
    }

    private String formatTreeWithParentIndicator(TreeNode node, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = "    ".repeat(level);

        if (level == 0) {
            sb.append("🔹 ").append(node.getName()).append("\n");
        } else {
            sb.append(indent).append("◦ ").append(node.getName()).append("\n");
        }

        // Сортировка дочерних узлов по алфавиту (игнорируем регистр)
        List<TreeNode> sortedChildren = node.getChildren().stream()
                .sorted(Comparator.comparing(TreeNode::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        // Рекурсивный вызов для каждого потомка
        for (TreeNode child : sortedChildren) {
            sb.append(formatTreeWithParentIndicator(child, level + 1));
        }

        return sb.toString();
    }
}
