package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyManager;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Discription
 **/
public abstract class AbstractIllegalDataVerifier implements IllegalDataVerifier {

    private VerifyContext context;

    protected final String PROPERTY_TOKEN_SPLITTOR = ",";

    @PostConstruct
    private void init() {
        VerifyManager.addVerifier(this);
        VerifyManager.MARK_NAME_ITEM_TYPE_MAP.put(this.getMarkName(), this.getTargetItemType());
        VerifyManager.ILLEGAL_TYPE_MARK_NAME_MAP.put(this.getDisplayName(), this.getMarkName());
    }

    @Override
    public void setContext(VerifyContext context) {
        this.context = context;
    }

    @Override
    public VerifyContext getContext() {
        return context;
    }

    protected static <K, I> Map<K, List<I>> groupBy(List<I> items, Function<I, List<K>> keyOf) {
        // Pay attention that this method is different from the method MultiMaps.index(Iterable, Function(ItemType, KeyType))
        // in that this method's second parameter(keyOf) returns a list of key instead of a single key
        HashMap<K, List<I>> result = new HashMap<>();
        if (items != null) {
            for (I item : items) {
                List<K> keys = keyOf.apply(item);
                if (keys == null || keys.size() == 0) {
                    continue;
                }
                for (K key : keys) {
                    if (!result.containsKey(key)) {
                        result.put(key, new ArrayList<>());
                    }
                    result.get(key).add(item);
                }
            }
        }
        return result;
    }
}
