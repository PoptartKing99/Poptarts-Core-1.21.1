package dev.poptartking.poptartcore.barrel;

import java.util.List;

record FluidBarrelBuildPattern(int size, int filledLayers) {
    static final List<FluidBarrelBuildPattern> ORDERED = List.of(
            new FluidBarrelBuildPattern(3, 2),
            new FluidBarrelBuildPattern(3, 1),
            new FluidBarrelBuildPattern(2, 1));

    int missingCount() {
        return size * size * (size - filledLayers) - 1;
    }
}
