package country.pvp.practice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import country.pvp.practice.arena.*;
import country.pvp.practice.board.BoardTask;
import country.pvp.practice.board.PracticeBoard;
import country.pvp.practice.commands.*;
import country.pvp.practice.commands.provider.ArenaProvider;
import country.pvp.practice.commands.provider.LadderProvider;
import country.pvp.practice.commands.provider.PartyProvider;
import country.pvp.practice.duel.DuelRequestInvalidateTask;
import country.pvp.practice.invitation.InvitationInvalidateTask;
import country.pvp.practice.kit.editor.KitEditorListener;
import country.pvp.practice.ladder.Ladder;
import country.pvp.practice.ladder.LadderManager;
import country.pvp.practice.ladder.LadderRepository;
import country.pvp.practice.leaderboards.LeaderBoardsFetchTask;
import country.pvp.practice.listeners.*;
import country.pvp.practice.match.Match;
import country.pvp.practice.match.MatchManager;
import country.pvp.practice.match.PearlCooldownTask;
import country.pvp.practice.match.snapshot.InventorySnapshotInvalidateTask;
import country.pvp.practice.party.Party;
import country.pvp.practice.party.PartyRequestInvalidateTask;
import country.pvp.practice.player.*;
import country.pvp.practice.queue.QueueManager;
import country.pvp.practice.queue.QueueTask;
import country.pvp.practice.settings.PracticeSettings;
import country.pvp.practice.settings.PracticeSettingsCommand;
import country.pvp.practice.settings.PracticeSettingsRepository;
import country.pvp.practice.util.TaskDispatcher;
import country.pvp.practice.util.menu.MenuListener;
import lombok.RequiredArgsConstructor;
import me.vaperion.blade.Blade;
import me.vaperion.blade.command.bindings.impl.BukkitBindings;
import me.vaperion.blade.command.container.impl.BukkitCommandContainer;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Practice {

    private final Injector injector;
    private final PlayerManager playerManager;
    private final PlayerRepository playerRepository;
    private final LadderManager ladderManager;
    private final LadderRepository ladderRepository;
    private final ArenaManager arenaManager;
    private final ArenaRepository arenaRepository;
    private final QueueManager queueManager;
    private final PracticeSettings practiceSettings;
    private final PracticeSettingsRepository practiceSettingsRepository;
    private final DuplicatedArenaRepository duplicatedArenaRepository;
    private final DuplicatedArenaManager duplicatedArenaManager;
    private final MatchManager matchManager;

    private Blade blade;

    void onEnable() {
        register(ItemBarListener.class);
        register(SessionListener.class);
        register(PracticeBoard.class);
        register(LobbyListener.class);
        register(MenuListener.class);
        register(KitListener.class);
        register(MatchListener.class);
        register(QueueListener.class);
        register(PartyListener.class);
        register(KitEditorListener.class);
        register(ArenaSelectionListener.class);
        register(ArenaBlockListener.class);

        schedule(QueueTask.class, 1000L, TimeUnit.MILLISECONDS, true);
        schedule(BoardTask.class, 500L, TimeUnit.MILLISECONDS, true);
        schedule(PlayerSaveTask.class, 10L, TimeUnit.MINUTES, true);
        schedule(PearlCooldownTask.class, 100L, TimeUnit.MILLISECONDS, true);
        schedule(InventorySnapshotInvalidateTask.class, 5L, TimeUnit.SECONDS, true);
        schedule(DuelRequestInvalidateTask.class, 5L, TimeUnit.SECONDS, true);
        schedule(InvitationInvalidateTask.class, 5L, TimeUnit.SECONDS, true);
        schedule(PartyRequestInvalidateTask.class, 5L, TimeUnit.SECONDS, true);
        schedule(LeaderBoardsFetchTask.class, 15L, TimeUnit.SECONDS, true);

        if (Bukkit.getWorld("arenas") == null) {
            Bukkit.createWorld(new WorldCreator("arenas").generateStructures(false).type(WorldType.FLAT));
        }

        loadSettings();
        loadArenas();
        loadArenaDuplicates();
        loadLadders();
        initPlayerQueues();

        setupBlade();
        registerCommand(ArenaCommands.class);
        registerCommand(LadderCommands.class);
        registerCommand(PracticeSettingsCommand.class);
        registerCommand(MatchCommands.class);
        registerCommand(SnapshotCommands.class);
        registerCommand(QueueCommands.class);
        registerCommand(KitEditorCommands.class);
        registerCommand(PartyCommands.class);
        registerCommand(InvitationCommands.class);

        loadOnlinePlayers();
    }

    void onDisable() {
        for (Match match : matchManager.getAll()) {
            match.cancel("Server is restarting");
        }

        for (PlayerSession playerSession : playerManager.getAll()) {
            playerRepository.save(playerSession);
        }
    }

    private void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerSession playerSession = new PlayerSession(player);
                playerRepository.loadAsync(playerSession);
                playerManager.add(playerSession);
            } catch (Exception e) {
                player.kickPlayer("Server is restarting...");
            }
        }
    }

    private void register(Class<? extends Listener> listener) {
        Bukkit.getPluginManager().registerEvents(injector.getInstance(listener), JavaPlugin.getPlugin(PracticePlugin.class));
    }

    private void schedule(Class<? extends Runnable> runnable, long duration, TimeUnit unit, boolean async) {
        if (async) {
            TaskDispatcher.scheduleAsync(injector.getInstance(runnable), duration, unit);
        } else TaskDispatcher.scheduleSync(injector.getInstance(runnable), duration, unit);
    }

    private void setupBlade() {
        blade = Blade.of()
                .fallbackPrefix("practice")
                .overrideCommands(true)
                .bind(Arena.class, injector.getInstance(ArenaProvider.class))
                .bind(Ladder.class, injector.getInstance(LadderProvider.class))
                .bind(Party.class, injector.getInstance(PartyProvider.class))
                .bind(PlayerSession.class, injector.getInstance(PracticePlayerProvider.class))
                .containerCreator(BukkitCommandContainer.CREATOR)
                .binding(new BukkitBindings())
                .helpGenerator(new HelpGenerator())
                .build();
    }

    private void registerCommand(Class<?> command) {
        blade.register(injector.getInstance(command));
    }

    private void initPlayerQueues() {
        for (Ladder ladder : ladderManager.getAll()) {
            queueManager.initQueue(ladder);
        }
    }

    private void loadLadders() {
        ladderManager.addAll(ladderRepository.loadAll());
    }

    private void loadArenas() {
        arenaManager.addAll(arenaRepository.loadAll());
    }

    private void loadArenaDuplicates() {
        duplicatedArenaManager.addAll(duplicatedArenaRepository.loadAll());
    }

    private void loadSettings() {
        practiceSettingsRepository.load(practiceSettings);
    }
}
