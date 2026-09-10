package dev.poptartking.poptartcore.quern;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QuernRotationTest {
    private static final float STEP = 22.5F;

    @Test
    void singleCrankCompletesInSixteenTicks() {
        QuernRotation rotation = new QuernRotation();
        rotation.crank();
        for (int tick = 0; tick < 15; tick++) {
            rotation.tick(STEP);
            assertTrue(rotation.isRotating());
        }
        rotation.tick(STEP);
        assertFalse(rotation.isRotating());
        assertEquals(0, rotation.angle(1));
    }

    @Test
    void earlyOnTimeAndLateUpdatesAlwaysFinishAligned() {
        for (int delay = 0; delay <= 32; delay++) {
            QuernRotation rotation = new QuernRotation();
            rotation.crank();
            for (int tick = 0; tick < delay; tick++) {
                rotation.tick(STEP);
            }
            rotation.crank();
            finishAndCheck(rotation);
        }
    }

    @Test
    void repeatedEarlyUpdatesDoNotAccumulateAnUnboundedQueue() {
        QuernRotation rotation = new QuernRotation();
        for (int tick = 0; tick < 10000; tick++) {
            rotation.crank();
            rotation.tick(STEP);
        }
        finishAndCheck(rotation);
    }

    @Test
    void interpolationMovesForwardAcrossTheWrap() {
        QuernRotation rotation = new QuernRotation();
        rotation.crank();
        for (int tick = 0; tick < 16; tick++) {
            rotation.tick(STEP);
        }
        assertEquals(STEP, rotation.angle(1) - rotation.angle(0));
        assertEquals(-11.25F, rotation.angle(0.5F));
    }

    private static void finishAndCheck(QuernRotation rotation) {
        int ticks = 0;
        while (rotation.isRotating() && ticks < 32) {
            rotation.tick(STEP);
            ticks++;
        }
        assertFalse(rotation.isRotating(), "Animation must finish within two turns");
        assertEquals(0, rotation.angle(1), "Stone must return to its aligned angle");
    }
}
