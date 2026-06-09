package cn.edu.whut.sept.zuul.game.websocket.vo;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import java.util.ArrayList;
import java.util.List;

public class PlayerVO {

    private int userId;
    private String playerName;
    private int attack;
    private int defense;
    private int currentHealth;
    private int maxHealth;
    private int maxCapacity;
    private int currentLoad;
    private List<BagItemVO> bag;
    private String currentRoomName;
    private int posX;
    private int posY;

    public static PlayerVO from(Player p) {
        PlayerVO vo = new PlayerVO();
        vo.userId = p.getUserId();
        vo.playerName = p.getPlayerName();
        vo.attack = p.getAttack();
        vo.defense = p.getDefense();
        vo.currentHealth = p.getCurrentHealth();
        vo.maxHealth = p.getMaxHealth();
        vo.maxCapacity = p.getMaxCapacity();
        vo.currentLoad = p.getCurrentLoad();
        vo.currentRoomName = p.getCurrentRoom().getName();
        vo.posX = p.getPosX();
        vo.posY = p.getPosY();
        vo.bag = new ArrayList<>();
        for (AbstractItem i : p.getBag()) {
            vo.bag.add(new BagItemVO(i.getName(), i.getWeight()));
        }
        return vo;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(int currentLoad) { this.currentLoad = currentLoad; }
    public List<BagItemVO> getBag() { return bag; }
    public void setBag(List<BagItemVO> bag) { this.bag = bag; }
    public String getCurrentRoomName() { return currentRoomName; }
    public void setCurrentRoomName(String currentRoomName) { this.currentRoomName = currentRoomName; }
    public int getPosX() { return posX; }
    public void setPosX(int posX) { this.posX = posX; }
    public int getPosY() { return posY; }
    public void setPosY(int posY) { this.posY = posY; }
}
