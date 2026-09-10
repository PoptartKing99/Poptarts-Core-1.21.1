package dev.poptartking.poptartcore.hammer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CrackRenderIdsTest {
    @Test
    void collidingPositionHashesNoLongerShareAnOverlay() {
        // Minecraft's position hash for the two positions reproduced in-game.
        assertEquals((64 + 0 * 31) * 31 + 0, (63 + 0 * 31) * 31 + 31);
        CrackRenderIds ids = new CrackRenderIds();
        assertNotEquals(ids.allocate(), ids.allocate());
    }

    @Test
    void allocationsStayUniqueAndNegativeAcrossRepeatedRemoval() {
        CrackRenderIds ids = new CrackRenderIds();
        Set<Integer> issued = new HashSet<>();
        int retained = ids.allocate();
        issued.add(retained);
        for (int index = 0; index < 100_000; index++) {
            int id = ids.allocate();
            assertTrue(id < 0);
            assertTrue(issued.add(id));
            assertNotEquals(retained, id);
            ids.release(id);
        }
    }
}
