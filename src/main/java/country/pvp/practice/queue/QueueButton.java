package country.pvp.practice.queue;

import country.pvp.practice.itembar.ItemBuilder;
import country.pvp.practice.ladder.Ladder;
import country.pvp.practice.menu.Button;
import country.pvp.practice.team.PlayerTeam;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class QueueButton extends Button {

    private final PlayerTeam team;
    private final SoloQueue queue;
    private final QueueManager queueManager;

    @Override
    public ItemStack getButtonItem(Player player) {
        Ladder ladder = queue.getLadder();
        return new ItemBuilder(ladder.getIcon())
                .name(ladder.getDisplayName())
                .lore("", "In Fights: n/a",
                        "In Queue: " + queue.size())
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        queue.addToQueue(team);
    }
}