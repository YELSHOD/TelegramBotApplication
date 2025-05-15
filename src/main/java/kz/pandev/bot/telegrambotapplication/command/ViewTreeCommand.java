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

/**
 * Команда для отображения иерархического дерева категорий.
 * <p>
 * Использует {@link TreeBuilder} для построения дерева категорий,
 * форматирует его в читаемый вид с отступами и отправляет пользователю.
 * При ошибках информирует пользователя и логирует исключения.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewTreeCommand implements BotCommand {

    private final TreeBuilder treeBuilder;

    /**
     * Возвращает имя команды.
     *
     * @return строка "/viewtree"
     */
    @Override
    public String getCommand() {
        return "/viewtree";
    }

    /**
     * Обрабатывает обновление от Telegram.
     * Строит дерево категорий, форматирует его и отправляет пользователю.
     * Логирует ошибки и информирует пользователя в случае сбоев.
     *
     * @param update обновление от Telegram API
     * @param bot    экземпляр TelegramLongPollingBot для отправки сообщений
     */
    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();

        try {
            // Строим дерево категорий с корнем (null означает верхний уровень)
            TreeNode root = treeBuilder.buildTree(null);

            // Форматируем дерево в строку с отступами для удобного чтения
            String treeString = formatTreeWithParentIndicator(root, 0);

            // Отправляем сообщение с отформатированным деревом
            SendMessage message = new SendMessage(chatId, treeString);
            bot.execute(message);

        } catch (Exception e) {
            log.error("Ошибка при построении дерева: {}", e.getMessage());
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при построении дерева категорий."));
            } catch (Exception ex) {
                log.error("Ошибка при отправке сообщения об ошибке: {}", ex.getMessage());
            }
        }
    }

    /**
     * Рекурсивно форматирует дерево категорий в многострочный текст с отступами.
     * <p>
     * Иконки для визуального обозначения уровней:
     * <ul>
     *   <li>Для родительских категорий первого уровня используется иконка "🔹";</li>
     *   <li>Для всех остальных дочерних узлов используется иконка "◦".</li>
     * </ul>
     * <p>
     * Все дочерние узлы сортируются по имени без учёта регистра.
     *
     * @param node  текущий узел дерева
     * @param level уровень вложенности (0 — корень)
     * @return строковое представление узла и его потомков с соответствующими отступами и иконками
     */
    private String formatTreeWithParentIndicator(TreeNode node, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = "    ".repeat(level);

        if (level == 0) {
            sb.append(" ").append(node.getName()).append("\n");

        } else if (level == 1) {
            sb.append(indent).append("🔹 ").append(node.getName()).append("\n");

        } else {
            sb.append(indent).append("◦ ").append(node.getName()).append("\n");
        }

        List<TreeNode> sortedChildren = node.getChildren().stream()
                .sorted(Comparator.comparing(TreeNode::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (TreeNode child : sortedChildren) {
            sb.append(formatTreeWithParentIndicator(child, level + 1));
        }

        return sb.toString();
    }

}
