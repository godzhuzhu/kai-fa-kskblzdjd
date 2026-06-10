package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class ShadowbaneBallista extends AbstractItem implements IPlayerListener {

    public ShadowbaneBallista() {
        super("ShadowbaneBallista", "暗影弩炮", "黑暗力量铸造的重弩 (攻击附加5伤害)", 6);
        setAttackRange(3);
        setAttackCooldown(1500);
        setAttackType("ranged");
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
        if (event.getAttacker() == player) {
            event.getTarget().hurtBy(5);
        }
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
