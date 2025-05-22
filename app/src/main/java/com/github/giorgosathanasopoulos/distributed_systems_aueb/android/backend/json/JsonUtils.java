package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json;

import android.os.Build;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.logger.Logger;

public class JsonUtils {

    private static String s_Error = "";

    private static final Gson c_Gson = new Gson();

    public static String toJson(Object p_Obj) {
        return c_Gson.toJson(p_Obj);
    }

    public static <T> Optional<T> fromJson(String p_Json, Class<T> p_Class) {
        try {
            return Optional.of(c_Gson.fromJson(p_Json, p_Class));
        } catch (JsonSyntaxException e) {
            s_Error = e.getLocalizedMessage();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Logger.error("JsonUtils::fromJson failed to parse json (" + p_Json + "): " + s_Error);
            }
            return Optional.empty();
        }
    }

    public static String getError() {
        return s_Error;
    }
}
