package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import cn.edu.whut.sept.zuul.game.store.StoreManager;

public class SaveCommand extends Command {

    private StoreManager storeManager;

    public SaveCommand() {
    }

    public void setStoreManager(StoreManager storeManager) {
        this.storeManager = storeManager;
    }

    @Override
    public boolean execute(Game game, Player player) {
        if (storeManager == null) {
            game.getMessageBridge().send(new SinglePlayerMessage("存档系统不可用。"), player);
            return false;
        }
        storeManager.saveGame(game, "save1");
        game.getMessageBridge().send(new SinglePlayerMessage("游戏已保存。"), player);
        return false;
    }
}
