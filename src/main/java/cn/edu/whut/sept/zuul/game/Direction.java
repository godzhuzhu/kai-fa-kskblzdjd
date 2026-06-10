package cn.edu.whut.sept.zuul.game;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public String toLower() {
        return name().toLowerCase();
    }

    public Direction opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
        }
        return NORTH;
    }
}
