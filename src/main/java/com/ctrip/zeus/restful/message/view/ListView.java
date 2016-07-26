package com.ctrip.zeus.restful.message.view;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public class ListView<T extends ExtendedView> {
    private List<T> list = new ArrayList<>();

    public int getTotal() {
        return list.size();
    }

    @JsonIgnore
    public List<T> getList() {
        return list;
    }

    public void add(T o) {
        list.add(o);
    }
}