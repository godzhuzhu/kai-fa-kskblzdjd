package cn.edu.whut.sept.zuul.game.websocket.vo;

public class BagItemVO {
    private String name;
    private String displayName;
    private String description;
    private int weight;
    private int range;
    private String type;
    private boolean consumable;

    public BagItemVO() {
    }

    public BagItemVO(String name, String displayName, String description, int weight, int range, String type, boolean consumable) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.weight = weight;
        this.range = range;
        this.type = type;
        this.consumable = consumable;
    }

    public BagItemVO(String name, int weight) {
        this(name, name, "", weight, 0, "none", false);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isConsumable() { return consumable; }
    public void setConsumable(boolean c) { this.consumable = c; }
}
