package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.message.AbsMessageBridge;
import cn.edu.whut.sept.zuul.game.message.ConsoleMessageBridge;
import cn.edu.whut.sept.zuul.game.message.GlobalMessage;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import cn.edu.whut.sept.zuul.game.store.StoreManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Game {

    private Parser parser;
    private Player player;
    private List<Room> allRooms;
    private AbsMessageBridge messageBridge;
    private Map<Integer, Player> playerMap;
    private Room startingRoom;

    @Autowired
    private StoreManager storeManager;

    private static final Random RANDOM = new Random();

    public Game() {
        messageBridge = new ConsoleMessageBridge();
    }

    @PostConstruct
    private void init() {
        playerMap = new ConcurrentHashMap<>();
        createRooms();
        parser = new Parser(storeManager);
    }

    private void createRooms() {
        allRooms = new ArrayList<>();

        Room outside = new Room("outside", "大学主入口外");
        Room theater = new Room("theater", "报告厅内");
        Room pub = new Room("pub", "校园酒吧内");
        Room lab = new Room("lab", "计算机实验室内");
        Room office = new Room("office", "计算机管理办公室内");

        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        lab.setPortal(true);

        allRooms.add(outside);
        allRooms.add(theater);
        allRooms.add(pub);
        allRooms.add(lab);
        allRooms.add(office);
        for (Room room : allRooms) {
            room.addRandomItems();
        }

        startingRoom = outside;
        player = getOrCreatePlayer(0);
    }

    public Player getOrCreatePlayer(int userId) {
        return playerMap.computeIfAbsent(userId, id -> new Player(id, "Player" + id, startingRoom));
    }

    public String processCommand(Player p, String cmd) {
        Command command = parser.parseCommand(cmd);
        if (command == null) {
            String msg = "抱歉，我不理解这个命令...";
            messageBridge.send(new SinglePlayerMessage(msg), p);
            return msg;
        }
        command.execute(this, p);
        if (messageBridge instanceof ConsoleMessageBridge) {
            return ((ConsoleMessageBridge) messageBridge).getLastMessage();
        }
        return "";
    }

    public void play() {
        printWelcome();

        boolean finished = false;
        while (!finished) {
            Command command = parser.getCommand();
            if (command == null) {
                messageBridge.send(new SinglePlayerMessage("抱歉，我不理解这个命令..."), player);
            } else {
                finished = command.execute(this, player);
            }
        }

        messageBridge.send(new GlobalMessage("感谢游玩，再见！"));
    }

    private void printWelcome() {
        messageBridge.send(new GlobalMessage(""));
        messageBridge.send(new GlobalMessage("欢迎来到 Zuul 的世界！"));
        messageBridge.send(new GlobalMessage("Zuul 世界是一个全新的奇妙冒险游戏。"));
        messageBridge.send(new GlobalMessage("输入 'help' 获取帮助。"));
        messageBridge.send(new GlobalMessage(""));
        messageBridge.send(new SinglePlayerMessage(player.getCurrentRoom().getLongDescription()), player);
    }

    public Room getCurrentRoom() {
        return player.getCurrentRoom();
    }

    public void setCurrentRoom(Room room) {
        player.moveTo(room);
    }

    public Player getPlayer() {
        return player;
    }

    public AbsMessageBridge getMessageBridge() {
        return messageBridge;
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(allRooms);
    }

    public Room getStartingRoom() {
        return startingRoom;
    }

    public Map<Integer, Player> getAllPlayers() {
        return playerMap;
    }

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
