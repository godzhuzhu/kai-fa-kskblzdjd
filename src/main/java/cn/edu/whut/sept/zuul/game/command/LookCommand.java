package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;

public class LookCommand extends Command {

    @Override
    public boolean execute(Game game, Player player) {
        game.getMessageBridge().send(
                new SinglePlayerMessage(player.getCurrentRoom().getLongDescription()), player);
        return false;
    }
}
