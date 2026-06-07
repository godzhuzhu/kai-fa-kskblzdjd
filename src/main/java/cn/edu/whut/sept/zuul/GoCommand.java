package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;

/**
 * @deprecated 已被 {@link cn.edu.whut.sept.zuul.game.command.GoCommand} 替代。
 *             保留作为迁移参考。
 */
@Deprecated
public class GoCommand extends Command
{
    public boolean execute(Game game, Player player)
    {
        if(!hasSecondWord()) {
            System.out.println("Go where?");
            return false;
        }

        String direction = getSecondWord();
        Room currentRoom = player.getCurrentRoom();

        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            System.out.println("There is no door!");
        }
        else {
            game.setCurrentRoom(nextRoom);
            System.out.println(nextRoom.getLongDescription());
        }

        return false;
    }
}
