package dev.poptartking.poptartcore.navigation;

public record LodestoneMark(String name, String icon) {
    public static final int MAX_NAME_LENGTH = 32;
    public static final LodestoneMark UNNAMED = new LodestoneMark("", "default");

    public boolean isNamed() {
        return !this.name.isEmpty();
    }
}
