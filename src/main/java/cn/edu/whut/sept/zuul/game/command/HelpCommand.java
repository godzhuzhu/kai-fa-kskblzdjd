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
        "可用命令：\n"
        + "  go <方向>     - 移动到相邻房间\n"
        + "  back          - 返回上一个房间\n"
        + "  look          - 查看当前房间\n"
        + "  take <物品>   - 拾取物品\n"
        + "  drop <物品>   - 丢弃物品\n"
        + "  use <物品>    - 使用物品\n"
        + "  items         - 查看背包\n"
        + "  attack <玩家> - 攻击其他玩家\n"
        + "  save          - 保存游戏\n"
        + "  load          - 读取存档\n"
        + "  help          - 显示帮助\n"
        + "  quit          - 退出游戏";

    @Override
    public boolean execute(Game game, Player player) {
        game.getMessageBridge().send(new SinglePlayerMessage(HELP_TEXT), player);
        return false;
    }
}
