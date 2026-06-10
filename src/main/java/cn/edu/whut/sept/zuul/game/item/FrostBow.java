package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

public class FrostBow extends AbstractItem {

    public FrostBow() {
        super("FrostBow", "寒冰弓", "冻结敌人的冰霜之弓 (+12 攻击)", 9);
        setAttackRange(3);
        setAttackCooldown(1000);
        setAttackType("ranged");
    }

    @Override
    public void takenBy(Player player) {
        player.setAttack(player.getAttack() + 12);
    }

    @Override
    public void droppedBy(Player player) {
        player.setAttack(player.getAttack() - 12);
    }

    @Override
    public void usedBy(Player player) {
    }
}
