package country.pvp.practice.arena.duplicated;

import com.google.common.collect.Sets;
import country.pvp.practice.arena.Arena;

import java.util.Set;

public class ArenaGenerator {

    public Set<DuplicatedArena> generate(Arena parent, int amount, int offset) {
        Set<DuplicatedArena> duplicatedArenas = Sets.newHashSet();

        for (int index = 0; index < amount; index++) {
            DuplicatedArena arena = DuplicatedArena.from(parent, index * offset);
            duplicatedArenas.add(arena);
        }

        return duplicatedArenas;
    }
}