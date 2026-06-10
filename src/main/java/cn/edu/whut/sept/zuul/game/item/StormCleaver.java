package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 风暴斩刃 — 装备类物品，拾取 +15 攻击力，丢弃 -15。
 *
 * @author gmy
 * @since 1.0
 */
public class StormCleaver extends AbstractItem {

    public StormCleaver() {
        super("StormCleaver", "风暴斩刃", "蕴含雷电之力的传说之刃 (+15 攻击)", 12);
        setAttackRange(2);
        setAttackCooldown(1000);
        setAttackType("ranged");
    }

    @Override
    public void takenBy(Player player) {
        player.setAttack(player.getAttack() + 15);
    }

    @Override
    public void droppedBy(Player player) {
        player.setAttack(player.getAttack() - 15);
    }

    @Override
    public void usedBy(Player player) {
    }
}
