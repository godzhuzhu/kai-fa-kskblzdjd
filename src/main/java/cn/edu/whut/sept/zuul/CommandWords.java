package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.command.AttackCommand;
import cn.edu.whut.sept.zuul.game.command.BackCommand;
import cn.edu.whut.sept.zuul.game.command.DropCommand;
import cn.edu.whut.sept.zuul.game.command.GoCommand;
import cn.edu.whut.sept.zuul.game.command.HelpCommand;
import cn.edu.whut.sept.zuul.game.command.LoadCommand;
import cn.edu.whut.sept.zuul.game.command.SaveCommand;
import cn.edu.whut.sept.zuul.game.command.TakeCommand;
import cn.edu.whut.sept.zuul.game.command.UseCommand;
import cn.edu.whut.sept.zuul.game.store.StoreManager;

import java.util.HashMap;
import java.util.Iterator;

public class CommandWords
{
    private HashMap<String, Command> commands;

    public CommandWords(StoreManager storeManager)
    {
        commands = new HashMap<String, Command>();
        commands.put("go", new GoCommand());
        commands.put("back", new BackCommand());
        commands.put("attack", new AttackCommand());
        commands.put("take", new TakeCommand());
        commands.put("drop", new DropCommand());
        commands.put("use", new UseCommand());
        SaveCommand save = new SaveCommand();
        save.setStoreManager(storeManager);
        commands.put("save", save);
        LoadCommand load = new LoadCommand();
        load.setStoreManager(storeManager);
        commands.put("load", load);
        commands.put("help", new HelpCommand());
        commands.put("quit", new QuitCommand());
    }

    public Command get(String word)
    {
        return (Command)commands.get(word);
    }

    public void showAll()
    {
        for(Iterator i = commands.keySet().iterator(); i.hasNext(); ) {
            System.out.print(i.next() + "  ");
        }
        System.out.println();
    }
}
