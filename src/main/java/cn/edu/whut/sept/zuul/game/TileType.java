package cn.edu.whut.sept.zuul.game;

public class TileType {
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int DOOR_UP = 2;
    public static final int DOOR_DOWN = 3;
    public static final int DOOR_LEFT = 4;
    public static final int DOOR_RIGHT = 5;

    public static boolean isDoor(int tile) {
        return tile >= DOOR_UP && tile <= DOOR_RIGHT;
    }

    public static Direction toDirection(int doorTile) {
        switch (doorTile) {
            case DOOR_UP: return Direction.NORTH;
            case DOOR_DOWN: return Direction.SOUTH;
            case DOOR_LEFT: return Direction.WEST;
            case DOOR_RIGHT: return Direction.EAST;
        }
        return null;
    }
}
