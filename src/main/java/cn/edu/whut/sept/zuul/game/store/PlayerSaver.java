package cn.edu.whut.sept.zuul.game.store;

import java.util.ArrayList;
import java.util.List;

public class PlayerSaver {

    private int userId;
    private String playerName;
    private int currentRoomIndex;
    private List<Integer> previousRoomIndexes;
    private List<String> bagItemNames;
    private int maxCapacity;
    private int attack;
    private int defense;
    private int currentHealth;
    private int maxHealth;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getCurrentRoomIndex() { return currentRoomIndex; }
    public void setCurrentRoomIndex(int currentRoomIndex) { this.currentRoomIndex = currentRoomIndex; }
    public List<Integer> getPreviousRoomIndexes() { return previousRoomIndexes; }
    public void setPreviousRoomIndexes(List<Integer> previousRoomIndexes) { this.previousRoomIndexes = previousRoomIndexes; }
    public List<String> getBagItemNames() { return bagItemNames; }
    public void setBagItemNames(List<String> bagItemNames) { this.bagItemNames = bagItemNames; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
}
