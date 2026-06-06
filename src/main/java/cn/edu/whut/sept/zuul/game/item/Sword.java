package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 铁剑 — 装备类物品，拾取 +10 攻击力，丢弃 -10。
 *
 * @author gmy
 * @since 1.0
 */
public class Sword extends AbstractItem {

    public Sword() {
        super("Sword", "A sharp iron sword (+10 attack)", 8);
    }

    @Override
    public void takenBy(Player player) {
        player.setAttack(player.getAttack() + 10);
    }

    @Override
    public void droppedBy(Player player) {
        player.setAttack(player.getAttack() - 10);
    }

    @Override
    public void usedBy(Player player) {
    }
}
