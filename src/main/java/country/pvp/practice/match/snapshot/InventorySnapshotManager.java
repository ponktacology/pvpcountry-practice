package country.pvp.practice.match.snapshot;

import com.google.common.collect.Maps;

import java.util.*;

public class InventorySnapshotManager {

    private final Map<UUID, InventorySnapshot> snapshots = Maps.newConcurrentMap();

    public void add(InventorySnapshot snapshot) {
        snapshots.put(snapshot.getId(), snapshot);
    }

    public Optional<InventorySnapshot> get(UUID id) {
        return Optional.ofNullable(snapshots.get(id));
    }

    void invalidate() {
        snapshots.entrySet().removeIf(entry -> entry.getValue().hasExpired());
    }

    public void addAll(Collection<InventorySnapshot> snapshots) {
        snapshots.forEach(this::add);
    }
}
