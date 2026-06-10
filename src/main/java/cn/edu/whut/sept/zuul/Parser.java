package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.store.StoreManager;

import java.util.Scanner;

public class Parser
{
    private CommandWords commands;
    private Scanner reader;

    public Parser(StoreManager storeManager)
    {
        commands = new CommandWords(storeManager);
        reader = new Scanner(System.in);
    }

    public Parser()
    {
        commands = new CommandWords(null);
        reader = new Scanner(System.in);
    }

    public Command getCommand()
    {
        String inputLine;   // will hold the full input line
        String word1 = null;
        String word2 = null;

        System.out.print("> ");     // print prompt

        inputLine = reader.nextLine();

        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();      // get first word
            if(tokenizer.hasNext()) {
                word2 = tokenizer.next();      // get second word
            }
        }

        return wrapCommand(commands.get(word1), word2);
    }

    public Command parseCommand(String inputLine) {
        String word1 = null;
        String word2 = null;

        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();
            if(tokenizer.hasNext()) {
                word2 = tokenizer.next();
            }
        }

        return wrapCommand(commands.get(word1), word2);
    }

    private static Command wrapCommand(Command base, String secondWord) {
        if (base == null) return null;
        return new Command() {
            @Override
            public boolean execute(Game game, cn.edu.whut.sept.zuul.game.Player player) {
                base.setSecondWord(secondWord);
                return base.execute(game, player);
            }

            @Override
            public String getSecondWord() { return secondWord; }
            @Override
            public boolean hasSecondWord() { return secondWord != null; }
        };
    }

    public void showCommands()
    {
        commands.showAll();
    }
}

