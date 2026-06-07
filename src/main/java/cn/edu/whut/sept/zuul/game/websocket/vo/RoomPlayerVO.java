package cn.edu.whut.sept.zuul.game.websocket.vo;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomPlayerVO {

    private int userId;
    private String playerName;
    private int currentHealth;
    private int maxHealth;

    public static RoomPlayerVO from(Player p) {
        RoomPlayerVO vo = new RoomPlayerVO();
        vo.userId = p.getUserId();
        vo.playerName = p.getPlayerName();
        vo.currentHealth = p.getCurrentHealth();
        vo.maxHealth = p.getMaxHealth();
        return vo;
    }

    public static List<RoomPlayerVO> fromList(List<Player> players) {
        List<RoomPlayerVO> list = new ArrayList<>();
        for (Player p : players) {
            list.add(from(p));
        }
        return list;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
}
