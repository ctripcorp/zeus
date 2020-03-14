package com.ctrip.zeus.service.verify;

import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import org.jvnet.hk2.component.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Discription
 **/
@Component("verifyResultHandler")
public class VerifyResultHandler {

    @Resource
    private TagBox tagBox;

    @Resource
    private TagService tagService;

    @Resource
    private PropertyBox propertyBox;

    @Resource
    private PropertyService propertyService;

    private final Logger logger = LoggerFactory.getLogger(VerifyResultHandler.class);

    public void handle(List<VerifyResult> results) throws Exception {
        // results: could be taggingResults or PropertyResults
        // itemType could be any
        List<VerifyTaggingResult> taggingResults = new LinkedList<>();
        List<VerifyPropertyResult> propertyResults = new LinkedList<>();
        for (VerifyResult result : results) {
            if (result instanceof VerifyTaggingResult) {
                taggingResults.add((VerifyTaggingResult) result);
            } else if (result instanceof VerifyPropertyResult) {
                propertyResults.add((VerifyPropertyResult) result);
            }
        }
        handleTaggingResults(taggingResults);
        handlePropertyResults(propertyResults);
    }

    private void handlePropertyResults(List<VerifyPropertyResult> propertyResults) throws Exception {
        if (propertyResults != null) {
            // Group results by propertyName + itemType
            MultiMap<PropertyKey, VerifyPropertyResult> resultMap = new MultiMap<>();
            for (VerifyPropertyResult result : propertyResults) {
                if (result == null) {
                    continue;
                }
                PropertyKey key = new PropertyKey(result.getpName(), result.getTargetItemType());
                resultMap.add(key, result);
            }

            for (PropertyKey key : resultMap.keySet()) {
                List<VerifyPropertyResult> results = resultMap.get(key);
                Set<Long> upsertItemIds = new HashSet<>(results.size());

                Map<Long, VerifyPropertyResult> targetItemIdResultMap = new HashMap<>();
                for (VerifyPropertyResult result : results) {
                    Long targetItemId = getTargetItemId(result);
                    targetItemIdResultMap.put(targetItemId, result);
                    if (targetItemId != null) {
                        upsertItemIds.add(targetItemId);
                    }
                }

                Set<Long> existedItemIds = propertyService.queryTargets(key.getPropertyName(), key.getItemType());
                existedItemIds.removeAll(upsertItemIds);

                // remove property from items whose id in existItemIds
                for (Long id : existedItemIds) {
                    propertyBox.clear(key.getPropertyName(), key.getItemType(), id);
                }
                // upsert property to items whose id in upsertItemIds
                for (Long id : upsertItemIds) {
                    VerifyPropertyResult result = targetItemIdResultMap.get(id);
                    propertyBox.set(key.getPropertyName(), result.getpValue(), key.getItemType(), id);
                }
            }
        }
    }

    /*
     * @Description return THE ONLY item id that needs to be added property
     * @return: return least item id when result contains multiple item ids, else return the only one id
     **/
    private Long getTargetItemId(VerifyPropertyResult propertyResult) {
        List<Long> candidates = propertyResult.getTargetItemIds();
        if (candidates != null) {
            Long[] array = candidates.toArray(new Long[0]);
            if (candidates.size() == 1) {
                return candidates.get(0);
            } else if (candidates.size() > 1) {
                logger.warn("Multiple item id exists in VerifyPropertyResult");
                Arrays.sort(array);
                return array[0];
            }
        }
        return null;
    }

    private void handleTaggingResults(List<VerifyTaggingResult> results) throws Exception {
        Map<TaggingKey, List<Long>> taggingItemIdsMap = new HashMap<>();
        // Group item ids by tag name and item type
        for (VerifyTaggingResult result : results) {
            if (result == null) {
                continue;
            }
            TaggingKey taggingKey = new TaggingKey(result);
            if (!taggingItemIdsMap.containsKey(taggingKey)) {
                taggingItemIdsMap.put(taggingKey, new ArrayList<>());
            }
            taggingItemIdsMap.get(taggingKey).addAll(result.getTargetItemIds());
        }

        for (Map.Entry<TaggingKey, List<Long>> entry : taggingItemIdsMap.entrySet()) {
            String tagName = entry.getKey().getTagName();
            String itemType = entry.getKey().getItemType();
            Set<Long> taggingItemIds = new HashSet<>(entry.getValue());
            Set<Long> taggedItemIds = new HashSet<>(tagService.query(tagName, itemType));
            Set<Long> intersection = new HashSet<>(taggingItemIds);
            intersection.retainAll(taggedItemIds);
            taggingItemIds.removeAll(intersection);
            taggedItemIds.removeAll(intersection);

            // add tags to corresponding item
            // and remove tags from items that not in results
            if (taggedItemIds.size() > 0) {
                tagBox.untagging(tagName, itemType, taggedItemIds.toArray(new Long[taggedItemIds.size()]));
            }
            if (taggingItemIds.size() > 0) {
                tagBox.tagging(tagName, itemType, taggingItemIds.toArray(new Long[taggingItemIds.size()]));
            }
        }
    }

    private class TaggingKey {
        private final String tagName;
        private final String itemType;

        public TaggingKey(String tagName, String itemType) {
            this.tagName = tagName;
            this.itemType = itemType;
        }

        public TaggingKey(VerifyTaggingResult result) {
            this(result.getTagName(), result.getTargetItemType());
        }

        public String getTagName() {
            return tagName;
        }

        public String getItemType() {
            return itemType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TaggingKey)) return false;
            TaggingKey that = (TaggingKey) o;
            return Objects.equals(getTagName(), that.getTagName()) &&
                    Objects.equals(getItemType(), that.getItemType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTagName(), getItemType());
        }
    }

    private class PropertyKey {
        private final String propertyName;
        private final String itemType;

        public PropertyKey(String propertyName, String itemType) {
            this.propertyName = propertyName;
            this.itemType = itemType;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getItemType() {
            return itemType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PropertyKey)) return false;
            PropertyKey that = (PropertyKey) o;
            return Objects.equals(getPropertyName(), that.getPropertyName()) &&
                    Objects.equals(getItemType(), that.getItemType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPropertyName(), getItemType());
        }
    }
}
