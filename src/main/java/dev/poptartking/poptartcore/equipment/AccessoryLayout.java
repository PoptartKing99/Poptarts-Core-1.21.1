package dev.poptartking.poptartcore.equipment;

public final class AccessoryLayout {
    private AccessoryLayout() {}

    public static int x(int index) {
        return switch (index) {
            case 0, 1, 3, 4 -> 95;
            case 2, 5, 6 -> 77;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    public static int y(int index) {
        return switch (index) {
            case 3, 6 -> 8;
            case 0, 2 -> 26;
            case 1, 5 -> 44;
            case 4 -> 62;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
}
