package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils;

import java.util.Optional;

public class DoubleUtils {
    public static Optional<Double> tryParseDouble(String p_input) {
        try {
            return Optional.of(Double.parseDouble(p_input));
        } catch (NumberFormatException|NullPointerException e) {
            return Optional.empty();
        }
    }
}
