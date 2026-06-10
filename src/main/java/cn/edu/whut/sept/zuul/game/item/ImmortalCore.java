package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class ImmortalCore extends AbstractItem implements IPlayerListener {

    private boolean used = false;

    public ImmortalCore() {
        super("ImmortalCore", "不朽核心", "神秘的能量核心 (免疫一次死亡，保留1HP)", 2);
    }

    @Override
    public void takenBy(Player player) {
        player.addListener(this);
    }

    @Override
    public void droppedBy(Player player) {
        player.removeListener(this);
    }

    @Override
    public void usedBy(Player player) {
    }

    @Override
    public void onHurt(Player player, AttackEvent event) {
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
        if (!used) {
            used = true;
            player.setCurrentHealth(1);
            player.removeListener(this);
            player.removeFromBag(this);
        }
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
