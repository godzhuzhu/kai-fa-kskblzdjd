package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.command.BackCommand;
import cn.edu.whut.sept.zuul.game.command.GoCommand;
import cn.edu.whut.sept.zuul.game.command.HelpCommand;

import java.util.HashMap;
import java.util.Iterator;

public class CommandWords
{
    private HashMap<String, Command> commands;

    public CommandWords()
    {
        commands = new HashMap<String, Command>();
        commands.put("go", new GoCommand());
        commands.put("back", new BackCommand());
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
