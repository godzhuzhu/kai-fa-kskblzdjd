package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

public class WarHammer extends AbstractItem {

    public WarHammer() {
        super("WarHammer", "战锤", "粉碎一切的巨锤 (+20攻击, AOE)", 15);
        setAttackRange(1);
        setAttackCooldown(1200);
        setAttackType("aoe");
    }

    @Override
    public void takenBy(Player player) {
        player.setAttack(player.getAttack() + 20);
    }

    @Override
    public void droppedBy(Player player) {
        player.setAttack(player.getAttack() - 20);
    }

    @Override
    public void usedBy(Player player) {
    }
}
