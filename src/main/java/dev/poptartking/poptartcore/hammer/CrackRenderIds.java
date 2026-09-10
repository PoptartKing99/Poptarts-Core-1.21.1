package dev.poptartking.poptartcore.hammer;

import java.util.HashSet;
import java.util.Set;

/** Server-thread-owned IDs for one level's persistent crack overlays. */
final class CrackRenderIds {
    private final Set<Integer> active = new HashSet<>();
    private int next = -1;

    int allocate() {
        int start = next;
        do {
            int candidate = next;
            // Negative IDs avoid ordinary entity breaker IDs. Skip active IDs on wraparound.
            next = next == Integer.MIN_VALUE ? -1 : next - 1;
            if (active.add(candidate)) {
                return candidate;
            }
        } while (next != start);
        throw new IllegalStateException("No persistent crack render IDs available");
    }

    void release(int id) {
        active.remove(id);
    }
}
