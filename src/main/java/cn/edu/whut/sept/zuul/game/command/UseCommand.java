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
            game.getMessageBridge().send(new SinglePlayerMessage("Use what?"), player);
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

        if (target.getClass().getSimpleName().equals("Sword")
                || target.getClass().getSimpleName().equals("DragonscaleBulwark")
                || target.getClass().getSimpleName().equals("StormCleaver")) {
            game.getMessageBridge().send(new SinglePlayerMessage("Cannot use equipment."), player);
            return false;
        }

        if (target.getClass().getSimpleName().equals("BloodVial")
                && player.getCurrentHealth() >= player.getMaxHealth()) {
            game.getMessageBridge().send(new SinglePlayerMessage(
                    "Your health is already full. Cannot use " + target.getName() + "."), player);
            return false;
        }

        player.useItem(target);
        game.getMessageBridge().send(new SinglePlayerMessage("You used the " + target.getName() + "."), player);
        return false;
    }
}