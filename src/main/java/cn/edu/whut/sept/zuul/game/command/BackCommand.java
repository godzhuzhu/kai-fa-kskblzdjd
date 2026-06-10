package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;

/**
 * BackCommand — 回退命令，返回上一个房间。
 *
 * <p>通过 {@code back} 从玩家的历史路径栈中弹出上一个房间并移入。
 * 支持连续回退直到起点。</p>
 *
 * @author lfk
 * @since 1.0
 */
public class BackCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        Room previousRoom = player.goBack();

        if (previousRoom == null) {
            game.getMessageBridge().send(
                new SinglePlayerMessage("没有可以返回的房间。"), player);
            return false;
        }

        game.getMessageBridge().send(
            new SinglePlayerMessage(previousRoom.getLongDescription()), player);
        return false;
    }
}
