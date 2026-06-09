package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 龙鳞壁垒 — 装备类物品，拾取 +8 防御力，丢弃 -8。
 *
 * @author gmy
 * @since 1.0
 */
public class DragonscaleBulwark extends AbstractItem {

    public DragonscaleBulwark() {
        super("DragonscaleBulwark", "A shield made of dragon scales (+8 defense)", 10);
        setAttackRange(1);
        setAttackCooldown(1000);
        setAttackType("aoe");
    }

    @Override
    public void takenBy(Player player) {
        player.setDefense(player.getDefense() + 8);
    }

    @Override
    public void droppedBy(Player player) {
        player.setDefense(player.getDefense() - 8);
    }

    @Override
    public void usedBy(Player player) {
    }
}
