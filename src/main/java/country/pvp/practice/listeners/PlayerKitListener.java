package country.pvp.practice.listeners;

import com.google.inject.Inject;
import country.pvp.practice.match.Match;
import country.pvp.practice.message.Messager;
import country.pvp.practice.message.Messages;
import country.pvp.practice.player.PlayerListener;
import country.pvp.practice.player.PlayerManager;
import country.pvp.practice.player.PlayerSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class PlayerKitListener extends PlayerListener {

    @Inject
    public PlayerKitListener(PlayerManager playerManager) {
        super(playerManager);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void clickEvent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK && action != Action.PHYSICAL) return;

        PlayerSession playerSession = get(event);
        if (!playerSession.isInMatch()) return;

        Match match = playerSession.getCurrentMatch();

        playerSession.getMatchingKit(match.getLadder(), item).ifPresent(it -> {
            Messager.message(playerSession, Messages.MATCH_PLAYER_EQUIP_KIT.match("{kit}", it.getName()));
            it.apply(playerSession);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void dropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        PlayerSession playerSession = get(event);

        if (!playerSession.isInMatch()) return;

        Match match = playerSession.getCurrentMatch();

        playerSession.getMatchingKit(match.getLadder(), item).ifPresent(it -> event.setCancelled(true));
    }

    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        PlayerSession playerSession = get((Player) event.getWhoClicked());
        if (!playerSession.isInMatch()) return;

        Match match = playerSession.getCurrentMatch();

        PlayerInventory playerInventory = event.getWhoClicked().getInventory();

        if (event.getClick().isKeyboardClick()) {
            ItemStack hotbatItem = playerInventory.getItem(event.getHotbarButton());

            if (hotbatItem == null) return;

            playerSession.getMatchingKit(match.getLadder(), hotbatItem).ifPresent(it -> event.setCancelled(true));
            return;
        }

        playerSession.getMatchingKit(match.getLadder(), item).ifPresent(it -> event.setCancelled(true));
    }

    @EventHandler
    public void deathEvent(PlayerDeathEvent event) {
        PlayerSession playerSession = get(event.getEntity());
        if (!playerSession.isInMatch()) return;

        Match match = playerSession.getCurrentMatch();

        System.out.println("DEATH !");
        event.getDrops().removeIf(it -> playerSession.getMatchingKit(match.getLadder(), it).isPresent());
    }
}