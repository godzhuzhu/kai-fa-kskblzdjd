package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;

/**
 * GoCommand — 移动命令，支持方向移动和传送房间。
 *
 * <p>通过 {@code go <direction>} 移动到指定方向的房间。
 * 若目标为传送房间，则随机传送到其他非传送房间。</p>
 *
 * @author lfk
 * @since 1.0
 */
public class GoCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        if (!hasSecondWord()) {
            game.getMessageBridge().send(new SinglePlayerMessage("要去哪里？"), player);
            return false;
        }

        String direction = getSecondWord();
        Room currentRoom = player.getCurrentRoom();

        // 直接从出口 Map 获取，绕过 Room.getExit() 的传送随机逻辑
        // 传送逻辑由 GoCommand 按 Issue #5 规范自行处理
        Room nextRoom = currentRoom.getExitMap().get(direction);

        if (nextRoom == null) {
            game.getMessageBridge().send(new SinglePlayerMessage("那里没有门！"), player);
            return false;
        }

        // 传送房间处理
        if (nextRoom.isPortal()) {
            game.getMessageBridge().send(
                new SinglePlayerMessage("一股神秘力量将你传送..."), player);
            Room randomRoom = game.getRandomRoom();
            if (randomRoom != null) {
                player.moveTo(randomRoom);
            } else {
                player.moveTo(nextRoom);
            }
        } else {
            player.moveTo(nextRoom);
        }

        // 输出新房间信息
        game.getMessageBridge().send(
            new SinglePlayerMessage(player.getCurrentRoom().getLongDescription()), player);
        return false;
    }
}
