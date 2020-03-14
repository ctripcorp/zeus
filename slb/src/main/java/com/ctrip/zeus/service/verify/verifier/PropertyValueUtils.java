package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.service.verify.IdItemType;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

;

/**
 * @Discription
 **/
public class PropertyValueUtils {
    private static final String SPLITTOR = ",";

    public static String write(List<IdItemType> idItemTypes) {
        if (idItemTypes != null && idItemTypes.size() > 0) {
            return Joiner.on(SPLITTOR).join(idItemTypes.stream().map(IdItemType::toString).collect(Collectors.toList()));
        }
        return "";
    }

    public static List<IdItemType> read(String propertyValue) {
        List<IdItemType> results = new ArrayList<>();
        if (!Strings.isNullOrEmpty(propertyValue)) {
            return Splitter.on(SPLITTOR).splitToList(propertyValue).stream()
                    .map(IdItemType::parse)
                    .collect(Collectors.toList());
        }
        return results;
    }

    public static String findByName(List<Property> properties, String name) {
        Optional<Property> property = properties.stream().filter(p -> name.equalsIgnoreCase(p.getName())).findFirst();
        Property property1 = property.orElse(null);
        return property1 == null ? null : property1.getValue();
    }
}
