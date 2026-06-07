package cn.edu.whut.sept.zuul.game.combat.event;

import cn.edu.whut.sept.zuul.game.Player;

public class FightWinEvent {

    private final Player winner;
    private final Player loser;

    public FightWinEvent(Player winner, Player loser) {
        this.winner = winner;
        this.loser = loser;
    }

    public Player getWinner() { return winner; }
    public Player getLoser() { return loser; }
}
