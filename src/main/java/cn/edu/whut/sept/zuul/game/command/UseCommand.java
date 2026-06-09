package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import java.util.List;

public class UseCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        if (!hasSecondWord()) {
            game.getMessageBridge().send(new SinglePlayerMessage("要使用什么？"), player);
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
            game.getMessageBridge().send(new SinglePlayerMessage("你没有这个物品。"), player);
            return false;
        }

        if (target.getClass().getSimpleName().equals("Sword")
                || target.getClass().getSimpleName().equals("DragonscaleBulwark")
                || target.getClass().getSimpleName().equals("StormCleaver")) {
            game.getMessageBridge().send(new SinglePlayerMessage("装备类物品无法使用。"), player);
            return false;
        }

        if (target.getClass().getSimpleName().equals("BloodVial")
                && player.getCurrentHealth() >= player.getMaxHealth()) {
            game.getMessageBridge().send(new SinglePlayerMessage(
                    "生命值已满，无法使用 " + target.getName() + "."), player);
            return false;
        }

        player.useItem(target);
        game.getMessageBridge().send(new SinglePlayerMessage("你使用了 " + target.getName() + "."), player);
        return false;
    }
}