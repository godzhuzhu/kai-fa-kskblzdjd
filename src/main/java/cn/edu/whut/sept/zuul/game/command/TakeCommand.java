package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;

/**
 * TakeCommand — 拾取物品命令。
 *
 * <p>将房间内的物品拾取到玩家背包中，受负重上限限制。</p>
 *
 * @author gmy
 * @since 1.0
 */
public class TakeCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        if (!hasSecondWord()) {
            game.getMessageBridge().send(new SinglePlayerMessage("拾取什么？"), player);
            return false;
        }

        String itemName = getSecondWord();
        Room room = player.getCurrentRoom();
        AbstractItem item = room.getItem(itemName);

        if (item == null) {
            game.getMessageBridge().send(new SinglePlayerMessage("这里没有那个物品。"), player);
            return false;
        }

        if (!player.canCarry(item)) {
            game.getMessageBridge().send(new SinglePlayerMessage(
                    "你拿不动这个物品！最大负重: " + player.getMaxCapacity()), player);
            return false;
        }

        room.removeItem(itemName);
        player.takeItem(item);
        game.getMessageBridge().send(new SinglePlayerMessage("你拾取了 " + item.getDisplayName() + "。"), player);
        return false;
    }
}
