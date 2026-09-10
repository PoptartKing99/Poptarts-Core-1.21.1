package dev.poptartking.poptartcore.quern;

/** Client animation with aligned endpoints and no unbounded queue of turns. */
public final class QuernRotation {
    private float angle;
    private float previousAngle;
    private float remaining;

    public void crank() {
        // Refresh to an aligned endpoint at least one full turn ahead.
        remaining = angle == 0 ? 360 : 720 - angle;
    }

    public void tick(float degreesPerTick) {
        previousAngle = angle;
        float step = Math.min(degreesPerTick, remaining);
        angle += step;
        remaining -= step;
        if (angle >= 360) {
            angle -= 360;
            previousAngle -= 360;
        }
    }

    public float angle(float partialTick) {
        return previousAngle + (angle - previousAngle) * partialTick;
    }

    public boolean isRotating() {
        return remaining > 0;
    }
}
