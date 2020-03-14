package com.ctrip.zeus.service.verify;

import com.google.common.base.Splitter;

import java.util.List;

/**
 * @Discription
 **/
public class IdItemType {
    private final Long id;
    private final String itemType;

    private static final String TOKENIZATION_SPLITTOR = "_";

    public IdItemType(Long id, String itemType) {
        this.id = id;
        this.itemType = itemType;
    }

    public Long getId() {
        return id;
    }

    public String getItemType() {
        return itemType;
    }

    @Override
    public String toString() {
        return getItemType() + TOKENIZATION_SPLITTOR + getId();
    }

    public static IdItemType parse(String value) {
        List<String> tokens = Splitter.on(TOKENIZATION_SPLITTOR).splitToList(value);
        if (tokens.size() != 2) {
            return null;
        }
        return new IdItemType(Long.parseLong(tokens.get(1)), tokens.get(0));
    }
}
