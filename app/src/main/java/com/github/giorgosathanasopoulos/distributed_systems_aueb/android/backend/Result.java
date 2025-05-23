package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend;

public class Result<T, E> {
    private final T value;
    private final E error;

    private Result(T value, E error) {
        this.value = value;
        this.error = error;
    }

    public static <T, E> Result<T, E> ok(T value) {
        return new Result<>(value, null);
    }

    public static <T, E> Result<T, E> error(E error) {
        return new Result<>(null, error);
    }

    public boolean isOk() {
        return error == null;
    }

    public T getValue() {
        return value;
    }

    public E getError() {
        return error;
    }
}
