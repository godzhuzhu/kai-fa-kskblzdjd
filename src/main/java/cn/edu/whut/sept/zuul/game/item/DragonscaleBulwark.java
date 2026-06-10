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
        super("DragonscaleBulwark", "龙鳞壁垒", "龙鳞锻造的盾牌 (+12 防御)", 10);
    }

    @Override
    public void takenBy(Player player) {
        player.setDefense(player.getDefense() + 12);
    }

    @Override
    public void droppedBy(Player player) {
        player.setDefense(player.getDefense() - 12);
    }

    @Override
    public void usedBy(Player player) {
    }
}
