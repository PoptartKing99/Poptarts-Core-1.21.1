package dev.poptartking.poptartcore.hammer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PersistentMiningMathTest {
    private static final float DELTA = 0.0001F;

    @Test
    void freshMiningKeepsVanillaStartTime() {
        assertEquals(80, PersistentMiningMath.resumedStart(0.0F, 0.1F, 100, 80));
    }

    @Test
    void resumedMiningAddsExactlyOneTickOfProgress() {
        int gameTicks = 100;
        float rate = 0.1F;
        int start = PersistentMiningMath.resumedStart(0.5F, rate, gameTicks, gameTicks);
        float nextFraction = rate * ((gameTicks + 1) - start + 1);

        assertEquals(0.6F, nextFraction, DELTA);
    }

    @Test
    void shiftedAreaStartsFromItsLeastDamagedBlock() {
        float groupProgress = PersistentMiningMath.leastProgress(0.9F, 0.0F);
        groupProgress = PersistentMiningMath.leastProgress(groupProgress, 0.0F);

        assertEquals(0.0F, groupProgress, DELTA);
    }

    @Test
    void returningToSameAreaResumesSharedProgress() {
        float groupProgress = PersistentMiningMath.leastProgress(0.9F, 0.9F);
        groupProgress = PersistentMiningMath.leastProgress(groupProgress, 0.9F);

        assertEquals(0.9F, groupProgress, DELTA);
    }

    @Test
    void surroundingBlockReceivesOnlyNewlyEarnedProgress() {
        assertEquals(0.1F, PersistentMiningMath.accrue(0.0F, 0.1F), DELTA);
        assertEquals(0.6F, PersistentMiningMath.accrue(0.5F, 0.1F), DELTA);
    }

    @Test
    void recordedAndAccruedProgressCannotExceedCompletion() {
        assertEquals(1.0F, PersistentMiningMath.record(0.8F, 1.2F), DELTA);
        assertEquals(1.0F, PersistentMiningMath.accrue(0.95F, 0.1F), DELTA);
    }

    @Test
    void recordingNeverMovesSavedProgressBackward() {
        assertEquals(0.8F, PersistentMiningMath.record(0.8F, 0.4F), DELTA);
    }

    @Test
    void hammerDamageDecaysMoreSlowlyThanNormalDamage() {
        assertEquals(0.45F, PersistentMiningMath.decay(0.5F, 0.1F, 1.0F, 0.05F), DELTA);
        assertEquals(0.49F, PersistentMiningMath.decay(0.5F, 0.1F, 0.2F, 0.05F), DELTA);
    }
}
