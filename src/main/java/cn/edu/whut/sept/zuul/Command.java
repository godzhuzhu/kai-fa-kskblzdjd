package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;

public abstract class Command
{
    private String secondWord;

    public Command()
    {
        secondWord = null;
    }

    public String getSecondWord()
    {
        return secondWord;
    }

    public boolean hasSecondWord()
    {
        return secondWord != null;
    }

    public void setSecondWord(String secondWord)
    {
        this.secondWord = secondWord;
    }

    /**
     * Execute this command.
     *
     * @param game   the game instance
     * @param player the player executing the command
     * @return true if the game should exit, false otherwise
     */
    public abstract boolean execute(Game game, Player player);
}
