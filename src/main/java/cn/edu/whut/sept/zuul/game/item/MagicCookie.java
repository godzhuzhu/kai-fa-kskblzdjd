package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 魔法饼干 — 道具类物品，使用后 +20 负重上限，从背包移除。
 *
 * @author gmy
 * @since 1.0
 */
public class MagicCookie extends AbstractItem {

    public MagicCookie() {
        super("MagicCookie", "魔法饼干", "神奇的饼干，增加负重上限 (+20)", 1);
    }

    @Override
    public void takenBy(Player player) {
    }

    @Override
    public void droppedBy(Player player) {
    }

    @Override
    public void usedBy(Player player) {
        player.setMaxCapacity(player.getMaxCapacity() + 20);
        player.removeFromBag(this);
    }
}
