package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

public class HealthPotion extends AbstractItem {

    public HealthPotion() {
        super("HealthPotion", "大血瓶", "散发光芒的红色药剂 (+50 生命)", 3);
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
        int heal = 50;
        player.setCurrentHealth(Math.min(player.getCurrentHealth() + heal, player.getMaxHealth()));
        player.removeFromBag(this);
    }
}
