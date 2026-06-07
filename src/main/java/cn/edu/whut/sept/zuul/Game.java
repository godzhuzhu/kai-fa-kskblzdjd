package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.message.AbsMessageBridge;
import cn.edu.whut.sept.zuul.game.message.ConsoleMessageBridge;
import cn.edu.whut.sept.zuul.game.message.GlobalMessage;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 该类是"World-of-Zuul"应用程序的主类。
 * 《World of Zuul》是一款简单的文本冒险游戏。用户可以在一些房间组成的迷宫中探险。
 *
 * <p>Game 类实例创建并初始化所有其他类：创建所有房间并连接成迷宫，
 * 创建解析器接收用户输入，管理玩家状态。</p>
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 2.0
 */
public class Game
{
    private Parser parser;
    private Player player;
    private List<Room> allRooms;
    private AbsMessageBridge messageBridge;

    private static final Random RANDOM = new Random();

    public Game()
    {
        messageBridge = new ConsoleMessageBridge();
        createRooms();
        parser = new Parser();
    }

    /**
     * 创建所有房间并建立出口连接。
     * 同时标记传送房间、生成随机物品、初始化玩家。
     */
    private void createRooms()
    {
        allRooms = new ArrayList<>();

        // Create rooms with name + description
        Room outside = new Room("outside", "outside the main entrance of the university");
        Room theater = new Room("theater", "in a lecture theater");
        Room pub = new Room("pub", "in the campus pub");
        Room lab = new Room("lab", "in a computing lab");
        Room office = new Room("office", "in the computing admin office");

        // Initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        // Mark lab as a portal room (Issue #7)
        lab.setPortal(true);

        // Add random items to rooms (Issue #4)
        allRooms.add(outside);
        allRooms.add(theater);
        allRooms.add(pub);
        allRooms.add(lab);
        allRooms.add(office);
        for (Room room : allRooms) {
            room.addRandomItems();
        }

        // Create player starting outside
        player = new Player(0, "Player", outside);
    }

    /**
     * 游戏主循环：输出欢迎信息，反复读取命令并执行，直到游戏结束。
     */
    public void play()
    {
        printWelcome();

        boolean finished = false;
        while (!finished) {
            Command command = parser.getCommand();
            if (command == null) {
                messageBridge.send(new SinglePlayerMessage("I don't understand..."), player);
            } else {
                finished = command.execute(this, player);
            }
        }

        messageBridge.send(new GlobalMessage("Thank you for playing.  Good bye."));
    }

    /**
     * 输出欢迎信息和起始房间描述。
     */
    private void printWelcome()
    {
        messageBridge.send(new GlobalMessage(""));
        messageBridge.send(new GlobalMessage("Welcome to the World of Zuul!"));
        messageBridge.send(new GlobalMessage("World of Zuul is a new, incredibly boring adventure game."));
        messageBridge.send(new GlobalMessage("Type 'help' if you need help."));
        messageBridge.send(new GlobalMessage(""));
        messageBridge.send(new SinglePlayerMessage(player.getCurrentRoom().getLongDescription()), player);
    }

    // ==================== Room / Player 访问 ====================

    /** @return 玩家当前所在房间 */
    public Room getCurrentRoom() {
        return player.getCurrentRoom();
    }

    /** @param room 设置玩家所在房间（会记录历史） */
    public void setCurrentRoom(Room room) {
        player.moveTo(room);
    }

    /** @return 当前玩家 */
    public Player getPlayer() {
        return player;
    }

    // ==================== 消息系统 ====================

    /** @return 消息桥接实例（供所有 Command 使用） */
    public AbsMessageBridge getMessageBridge() {
        return messageBridge;
    }

    // ==================== 传送相关 (Issue #7) ====================

    /** @return 所有房间列表的副本 */
    public List<Room> getAllRooms() {
        return new ArrayList<>(allRooms);
    }

    /**
     * 从所有房间中随机返回一个非传送房间。
     *
     * @return 随机非传送房间，如果没有则返回 null
     */
    public Room getRandomRoom() {
        List<Room> nonPortal = new ArrayList<>();
        for (Room room : allRooms) {
            if (!room.isPortal()) {
                nonPortal.add(room);
            }
        }
        if (nonPortal.isEmpty()) {
            return null;
        }
        return nonPortal.get(RANDOM.nextInt(nonPortal.size()));
    }
}
