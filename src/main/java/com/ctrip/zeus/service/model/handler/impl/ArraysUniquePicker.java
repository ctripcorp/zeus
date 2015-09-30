package com.ctrip.zeus.service.model.handler.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhoumy on 2015/9/28.
 */
public class ArraysUniquePicker {

    // O(nlogn)
    public static void pick(long[] arr1, long[] arr2, List<Long> arr1picker, List<Long> arr2picker) {
        // DualPivotQuicksort: O(nlogn)
        Arrays.sort(arr1);
        Arrays.sort(arr2);
        int i, j;
        i = j = 0;
        // O(n)
        while (i < arr1.length && j < arr2.length) {
            if (arr1[i] == arr2[j]) {
                ++i;
                ++j;
                continue;
            } else if (arr1[i] < arr2[j]) {
                arr1picker.add(arr1[i]);
                ++i;
                continue;
            } else { // if (arr1[i] > arr2[j]) {
                arr2picker.add(arr2[j]);
                ++j;
            }
        }
        while (i < arr1.length) {
            arr1picker.add(arr1[i]);
            ++i;
        }
        while (j < arr2.length) {
            arr2picker.add(arr2[j]);
            ++j;
        }
    }

    public static void pick(String[] arr1, String[] arr2, List<String> arr1picker, List<String> arr2picker) {
        // ComparableTimSort: O(nlogn)
        Arrays.sort(arr1);
        Arrays.sort(arr2);
        int i, j;
        i = j = 0;
        while (i < arr1.length && j < arr2.length) {
            if (arr1[i].contentEquals(arr2[j])) {
                ++i;
                ++j;
                continue;
            } else if (arr1[i].compareTo(arr2[j]) < 0) {
                arr1picker.add(arr1[i]);
                ++i;
                continue;
            } else { // if (arr1[i].compareTo(arr2[j]) > 0) {
                arr2picker.add(arr2[j]);
                ++j;
            }
        }
        while (i < arr1.length) {
            arr1picker.add(arr1[i]);
            ++i;
        }
        while (j < arr2.length) {
            arr2picker.add(arr2[j]);
            ++j;
        }
    }
}
