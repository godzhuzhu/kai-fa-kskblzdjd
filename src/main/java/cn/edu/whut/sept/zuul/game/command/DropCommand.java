package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import java.util.List;

/**
 * DropCommand — 丢弃物品命令。
 *
 * <p>将背包中的物品丢弃到当前房间。</p>
 *
 * @author gmy
 * @since 1.0
 */
public class DropCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        if (!hasSecondWord()) {
            game.getMessageBridge().send(new SinglePlayerMessage("要丢弃什么？"), player);
            return false;
        }

        String itemName = getSecondWord();
        List<AbstractItem> bag = player.getBag();

        AbstractItem target = null;
        for (AbstractItem item : bag) {
            if (item.getName().equals(itemName)) {
                target = item;
                break;
            }
        }

        if (target == null) {
            game.getMessageBridge().send(new SinglePlayerMessage("You don't have this item."), player);
            return false;
        }

        player.dropItem(target);
        player.getCurrentRoom().addItem(target);
        game.getMessageBridge().send(new SinglePlayerMessage("你丢弃了 " + target.getName() + "."), player);
        return false;
    }
}
