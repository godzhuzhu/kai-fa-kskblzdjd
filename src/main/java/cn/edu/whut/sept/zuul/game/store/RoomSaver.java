package cn.edu.whut.sept.zuul.game.store;

import java.util.ArrayList;
import java.util.List;

public class RoomSaver {

    private String name;
    private String description;
    private boolean portal;
    private List<String> exits;
    private List<String> itemNames;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPortal() { return portal; }
    public void setPortal(boolean portal) { this.portal = portal; }
    public List<String> getExits() { return exits; }
    public void setExits(List<String> exits) { this.exits = exits; }
    public List<String> getItemNames() { return itemNames; }
    public void setItemNames(List<String> itemNames) { this.itemNames = itemNames; }
}
