package dev.poptartking.poptartcore.navigation.persistence;

public interface Persistent {
    void bindDirtyMarker(Runnable marker);
}
