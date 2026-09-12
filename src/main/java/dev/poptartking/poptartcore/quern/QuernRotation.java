package dev.poptartking.poptartcore.quern;

/** Client animation for one complete turn over the server's processing duration. */
public final class QuernRotation {
    private float angle;
    private float previousAngle;
    private float remaining;
    private float degreesPerTick;

    public void start(int durationTicks) {
        angle = 0;
        previousAngle = 0;
        remaining = 360;
        degreesPerTick = durationTicks > 0 ? 360.0F / durationTicks : 360.0F;
    }

    public void resume(int remainingTicks, int durationTicks) {
        if (remainingTicks <= 0 || durationTicks <= 0) {
            return;
        }
        degreesPerTick = 360.0F / durationTicks;
        remaining = Math.min(360.0F, remainingTicks * degreesPerTick);
        angle = 360.0F - remaining;
        previousAngle = angle;
    }

    public void tick() {
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
