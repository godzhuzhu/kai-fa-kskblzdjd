package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.message.AbsMessageBridge;
import cn.edu.whut.sept.zuul.game.message.ConsoleMessageBridge;
import cn.edu.whut.sept.zuul.game.message.GlobalMessage;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import jakarta.annotation.PostConstruct;
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

    private static final Random RANDOM = new Random();

    public Game() {
        messageBridge = new ConsoleMessageBridge();
    }

    @PostConstruct
    private void init() {
        playerMap = new ConcurrentHashMap<>();
        createRooms();
        parser = new Parser();
    }

    private void createRooms() {
        allRooms = new ArrayList<>();

        Room outside = new Room("outside", "outside the main entrance of the university");
        Room theater = new Room("theater", "in a lecture theater");
        Room pub = new Room("pub", "in the campus pub");
        Room lab = new Room("lab", "in a computing lab");
        Room office = new Room("office", "in the computing admin office");

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

    public void processCommand(Player p, String cmd) {
        Command command = parser.parseCommand(cmd);
        if (command == null) {
            messageBridge.send(new SinglePlayerMessage("I don't understand..."), p);
        } else {
            command.execute(this, p);
        }
    }

    public void play() {
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

    private void printWelcome() {
        messageBridge.send(new GlobalMessage(""));
        messageBridge.send(new GlobalMessage("Welcome to the World of Zuul!"));
        messageBridge.send(new GlobalMessage("World of Zuul is a new, incredibly boring adventure game."));
        messageBridge.send(new GlobalMessage("Type 'help' if you need help."));
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
