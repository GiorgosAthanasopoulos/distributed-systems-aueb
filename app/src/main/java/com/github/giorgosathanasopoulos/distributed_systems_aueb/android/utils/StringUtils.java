package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils;

public class StringUtils {
    public static int countCharInStr(String s, char c) {
        int count = 0;
        for (char _c : s.toCharArray())
            if (c == _c)
                count++;
        return count;
    }
}
