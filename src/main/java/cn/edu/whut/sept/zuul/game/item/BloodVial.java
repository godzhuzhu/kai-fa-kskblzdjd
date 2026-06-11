package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

public class BloodVial extends AbstractItem {

    public BloodVial() {
        super("BloodVial", "血瓶", "恢复生命的红色药水 (+40 生命)", 2);
        setConsumable(true);
    }

    @Override
    public void takenBy(Player player) {
    }

    @Override
    public void droppedBy(Player player) {
    }

    @Override
    public void usedBy(Player player) {
        int newHealth = player.getCurrentHealth() + 40;
        if (newHealth > player.getMaxHealth()) {
            newHealth = player.getMaxHealth();
        }
        player.setCurrentHealth(newHealth);
        player.removeFromBag(this);
    }
}
