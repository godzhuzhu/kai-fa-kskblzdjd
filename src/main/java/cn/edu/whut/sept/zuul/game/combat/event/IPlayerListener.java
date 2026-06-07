package cn.edu.whut.sept.zuul.game.combat.event;

import cn.edu.whut.sept.zuul.game.Player;

public interface IPlayerListener {

    void onHurt(Player player, AttackEvent event);

    void onDeath(Player player, DeathEvent event);

    void onFightWin(Player player, FightWinEvent event);
}
