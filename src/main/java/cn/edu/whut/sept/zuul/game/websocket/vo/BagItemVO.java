package cn.edu.whut.sept.zuul.game.websocket.vo;

public class BagItemVO {
    private String name;
    private int weight;
    private int range;
    private String type;

    public BagItemVO() {
    }

    public BagItemVO(String name, int weight, int range, String type) {
        this.name = name;
        this.weight = weight;
        this.range = range;
        this.type = type;
    }

    public BagItemVO(String name, int weight) {
        this(name, weight, 0, "none");
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
