package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class ThornArmor extends AbstractItem implements IPlayerListener {

    private Player owner;

    public ThornArmor() {
        super("ThornArmor", "荆棘铠甲", "布满尖刺的铠甲 (+10防御, 反伤30%)", 12);
    }

    @Override
    public void takenBy(Player player) {
        player.setDefense(player.getDefense() + 10);
        this.owner = player;
        player.addListener(this);
    }

    @Override
    public void droppedBy(Player player) {
        player.setDefense(player.getDefense() - 10);
        player.removeListener(this);
        this.owner = null;
    }

    @Override
    public void usedBy(Player player) {
    }

    @Override
    public void onHurt(Player player, AttackEvent event) {
        if (player == owner && event.getAttacker() != player) {
            int reflect = Math.max(1, (int)(event.getDamage() * 0.3));
            event.getAttacker().hurtBy(reflect);
        }
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
