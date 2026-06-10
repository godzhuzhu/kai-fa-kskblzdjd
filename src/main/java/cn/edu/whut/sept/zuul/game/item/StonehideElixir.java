package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

public class StonehideElixir extends AbstractItem {

    public StonehideElixir() {
        super("StonehideElixir", "石肤药剂", "使皮肤坚如磐石的药水 (+15 防御)", 3);
    }

    @Override
    public void takenBy(Player player) {
    }

    @Override
    public void droppedBy(Player player) {
    }

    @Override
    public void usedBy(Player player) {
        player.setDefense(player.getDefense() + 15);
        player.removeFromBag(this);
    }
}
