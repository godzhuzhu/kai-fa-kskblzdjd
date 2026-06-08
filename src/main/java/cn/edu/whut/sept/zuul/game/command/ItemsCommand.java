package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;

public class ItemsCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("Your bag: ");
        if (player.getBag().isEmpty()) {
            sb.append("empty");
        } else {
            for (AbstractItem item : player.getBag()) {
                sb.append(item.getName()).append("(").append(item.getWeight()).append("kg) ");
            }
            sb.append("| Load: ").append(player.getCurrentLoad())
              .append("/").append(player.getMaxCapacity());
        }
        game.getMessageBridge().send(new SinglePlayerMessage(sb.toString()), player);
        return false;
    }
}
