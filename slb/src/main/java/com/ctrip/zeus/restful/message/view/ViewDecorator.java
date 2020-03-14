package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

;

/**
 * Created by zhoumy on 2016/7/25.
 */
@Component("viewDecorator")
public class ViewDecorator {
    @Resource
    private PropertyService propertyService;
    @Resource
    private TagService tagService;

    public <T extends ExtendedView> List<T> decorate(List<T> list, String type) {
        Set<Long> itemIds = new HashSet<>();
        for (ExtendedView view : list) {
            itemIds.add(view.getId());
        }
        Map<Long, List<String>> rItemTags = null;
        Map<Long, List<Property>> rItemProperties = null;

        Long[] itemIdArray = itemIds.toArray(new Long[itemIds.size()]);
        try {
            rItemTags = tagService.getTags(type, itemIdArray);
        } catch (Exception e) {
        }
        try {
            rItemProperties = propertyService.getProperties(type, itemIdArray);
        } catch (Exception e) {
        }

        if (rItemTags != null) {
            for (ExtendedView view : list) {
                try {
                    view.setTags(rItemTags.get(view.getId()));
                } catch (Exception e) {
                }
            }
        }
        if (rItemProperties != null) {
            for (ExtendedView view : list) {
                try {
                    view.setProperties(rItemProperties.get(view.getId()));
                } catch (Exception e) {
                }
            }
        }
        return list;
    }

    public <T extends ExtendedView> void decorate(T[] list, String type) {
        Set<Long> itemIds = new HashSet<>();
        for (ExtendedView view : list) {
            itemIds.add(view.getId());
        }
        Map<Long, List<String>> rItemTags = null;
        Map<Long, List<Property>> rItemProperties = null;

        Long[] itemIdArray = itemIds.toArray(new Long[itemIds.size()]);
        try {
            rItemTags = tagService.getTags(type, itemIdArray);
        } catch (Exception e) {
        }
        try {
            rItemProperties = propertyService.getProperties(type, itemIdArray);
        } catch (Exception e) {
        }

        if (rItemTags != null) {
            for (ExtendedView view : list) {
                try {
                    view.setTags(rItemTags.get(view.getId()));
                } catch (Exception e) {
                }
            }
        }
        if (rItemProperties != null) {
            for (ExtendedView view : list) {
                try {
                    view.setProperties(rItemProperties.get(view.getId()));
                } catch (Exception e) {
                }
            }
        }
    }

    public <T extends ExtendedView> void decorate(T object, String type) {
        try {
            object.setTags(tagService.getTags(type, object.getId()));
        } catch (Exception e) {
        }
        try {
            object.setProperties(propertyService.getProperties(type, object.getId()));
        } catch (Exception e) {
        }
    }
}
