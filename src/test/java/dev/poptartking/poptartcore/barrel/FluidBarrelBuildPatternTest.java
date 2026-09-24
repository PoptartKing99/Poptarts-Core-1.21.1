package dev.poptartking.poptartcore.barrel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class FluidBarrelBuildPatternTest {
    @Test
    void repairsExistingGiantBeforeStartingAnotherBarrel() {
        assertEquals(
                List.of(
                        new FluidBarrelBuildPattern(3, 2),
                        new FluidBarrelBuildPattern(3, 1),
                        new FluidBarrelBuildPattern(2, 1)),
                FluidBarrelBuildPattern.ORDERED);
    }

    @Test
    void chargesOnlyForMissingBarrels() {
        assertEquals(8, new FluidBarrelBuildPattern(3, 2).missingCount());
        assertEquals(17, new FluidBarrelBuildPattern(3, 1).missingCount());
        assertEquals(3, new FluidBarrelBuildPattern(2, 1).missingCount());
    }
}
