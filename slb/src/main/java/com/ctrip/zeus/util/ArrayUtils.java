package com.ctrip.zeus.util;

import java.util.Arrays;

public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static <T> T[] copyAndAppend(T[] array, T data) {
        T[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[newArray.length - 1] = data;
        return newArray;
    }
}
