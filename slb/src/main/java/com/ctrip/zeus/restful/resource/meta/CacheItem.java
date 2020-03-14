package com.ctrip.zeus.restful.resource.meta;

/**
 * @author:xingchaowang
 * @date: 2016/8/17.
 */
public class CacheItem {
    private String id;
    private String name;
    private String pinyin;
    private String chineseName;

    public CacheItem() {
    }

    public CacheItem(String id, String name, String pinyin) {
        this.id = id;
        this.name = name;
        this.pinyin = pinyin;
    }

    public CacheItem(String id, String name, String pinyin, String chineseName) {
        this.id = id;
        this.name = name;
        this.pinyin = pinyin;
        this.chineseName = chineseName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(64);
        result.append((id == null ? "" : id));
        result.append(name == null ? "" : name);
        result.append(pinyin == null ? "" : pinyin);
        result.append(chineseName == null ? "" : chineseName);
        return result.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheItem)) return false;

        CacheItem cacheItem = (CacheItem) o;

        if (id != null ? !id.equals(cacheItem.id) : cacheItem.id != null) return false;
        if (name != null ? !name.equals(cacheItem.name) : cacheItem.name != null) return false;
        if (pinyin != null ? !pinyin.equals(cacheItem.pinyin) : cacheItem.pinyin != null) return false;
        return chineseName != null ? chineseName.equals(cacheItem.chineseName) : cacheItem.chineseName == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (pinyin != null ? pinyin.hashCode() : 0);
        result = 31 * result + (chineseName != null ? chineseName.hashCode() : 0);
        return result;
    }
}
