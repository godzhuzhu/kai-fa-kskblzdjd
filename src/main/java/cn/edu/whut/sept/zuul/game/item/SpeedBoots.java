package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

public class SpeedBoots extends AbstractItem {

    public SpeedBoots() {
        super("SpeedBoots", "疾风靴", "轻便的战斗靴 (+5攻击, +3防御)", 4);
    }

    @Override
    public void takenBy(Player player) {
        player.setAttack(player.getAttack() + 5);
        player.setDefense(player.getDefense() + 3);
    }

    @Override
    public void droppedBy(Player player) {
        player.setAttack(player.getAttack() - 5);
        player.setDefense(player.getDefense() - 3);
    }

    @Override
    public void usedBy(Player player) {
    }
}
