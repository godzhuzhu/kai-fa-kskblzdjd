package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;

/**
 * HelpCommand — 帮助命令，列出所有可用命令及其说明。
 *
 * @author lfk
 * @since 1.0
 */
public class HelpCommand extends Command {

    private static final String HELP_TEXT =
        "Your available commands are:\n"
        + "  go <direction>  - move to a neighboring room\n"
        + "  back            - return to the previous room\n"
        + "  look            - view the current room\n"
        + "  take <item>     - pick up an item\n"
        + "  drop <item>     - drop an item from your bag\n"
        + "  use <item>      - use an item from your bag\n"
        + "  items           - show items in your bag\n"
        + "  help            - show this help message\n"
        + "  quit            - quit the game";

    @Override
    public boolean execute(Game game, Player player) {
        game.getMessageBridge().send(new SinglePlayerMessage(HELP_TEXT), player);
        return false;
    }
}
