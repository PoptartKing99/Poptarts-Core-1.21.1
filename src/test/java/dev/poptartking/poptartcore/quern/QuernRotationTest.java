package dev.poptartking.poptartcore.quern;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QuernRotationTest {
    private static final int DURATION = 64;

    @Test
    void singleTurnCompletesInSixtyFourTicks() {
        QuernRotation rotation = new QuernRotation();
        rotation.start(DURATION);
        for (int tick = 0; tick < DURATION - 1; tick++) {
            rotation.tick();
            assertTrue(rotation.isRotating());
        }
        rotation.tick();
        assertFalse(rotation.isRotating());
        assertEquals(0, rotation.angle(1));
    }

    @Test
    void startingAgainAlwaysRestartsOneBoundedTurn() {
        for (int delay = 0; delay <= DURATION * 2; delay++) {
            QuernRotation rotation = new QuernRotation();
            rotation.start(DURATION);
            for (int tick = 0; tick < delay; tick++) {
                rotation.tick();
            }
            rotation.start(DURATION);
            finishAndCheck(rotation);
        }
    }

    @Test
    void resumeStartsAtTheSavedPoint() {
        QuernRotation rotation = new QuernRotation();
        rotation.resume(32, DURATION);
        assertEquals(180.0F, rotation.angle(1));
        finishAndCheck(rotation);
    }

    @Test
    void interpolationMovesForwardAcrossTheWrap() {
        QuernRotation rotation = new QuernRotation();
        rotation.start(DURATION);
        for (int tick = 0; tick < DURATION; tick++) {
            rotation.tick();
        }
        assertEquals(360.0F / DURATION, rotation.angle(1) - rotation.angle(0));
        assertEquals(-360.0F / DURATION / 2.0F, rotation.angle(0.5F));
    }

    private static void finishAndCheck(QuernRotation rotation) {
        int ticks = 0;
        while (rotation.isRotating() && ticks < DURATION + 1) {
            rotation.tick();
            ticks++;
        }
        assertFalse(rotation.isRotating(), "Animation must finish within one slow turn");
        assertEquals(0, rotation.angle(1), "Stone must return to its aligned angle");
    }
}
