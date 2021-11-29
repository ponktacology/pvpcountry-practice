package country.pvp.practice.command;

import country.pvp.practice.player.PlayerManager;
import country.pvp.practice.player.PracticePlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PlayerCommand {

    private final PlayerManager playerManager;

    public PracticePlayer get(Player player) {
        return playerManager.get(player);
    }
}
