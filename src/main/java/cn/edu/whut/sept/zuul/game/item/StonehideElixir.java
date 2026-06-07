package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 石化药剂 — 道具类物品，使用后 +10 防御力，从背包移除。
 *
 * @author gmy
 * @since 1.0
 */
public class StonehideElixir extends AbstractItem {

    public StonehideElixir() {
        super("StonehideElixir", "A potion that turns skin to stone (+10 defense)", 3);
    }

    @Override
    public void takenBy(Player player) {
    }

    @Override
    public void droppedBy(Player player) {
    }

    @Override
    public void usedBy(Player player) {
        player.setDefense(player.getDefense() + 10);
    }
}
