package com.ctrip.zeus.service;

import com.ctrip.zeus.dao.entity.SlbArchiveGroup;
import com.ctrip.zeus.dao.mapper.SlbArchiveGroupMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class SmartArchiveGroupMapper extends AbstractSmartMapper<SlbArchiveGroup> {

    @Resource
    private SlbArchiveGroupMapper mapper;

    public List<SlbArchiveGroup> findAllByIdVersion(List<Integer> hashes, List<String> idVersionArray) {
        return query(new SplitConfig<SlbArchiveGroup>() {
            @Override
            public List<SlbArchiveGroup> doQuery(Object... args) {
                List<Integer> hashes = (List<Integer>) args[0];
                List<String> keys = (List<String>) args[1];
                return mapper.findAllByIdVersion(hashes, keys);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                return splitHashesAndKeys(args);
            }
        }, hashes, idVersionArray);
    }

    public List<SlbArchiveGroup> findVersionizedByIds(List<Long> ids) {
        return query(new SplitConfig<SlbArchiveGroup>() {
            @Override
            public List<SlbArchiveGroup> doQuery(Object... args) {
                List<Long> ids = (List<Long>) args[0];
                return mapper.findVersionizedByIds(ids);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                List<List<Object>> res = new ArrayList<>();
                List<Long> list = (List<Long>) args[0];
                for (List<Long> sub: split(list.toArray(new Long[0]))) {
                    res.add(Collections.singletonList(sub));
                }
                return res;
            }
        }, ids);
    }

    public List<SlbArchiveGroup> findAllByVsIds(@Param("ids") List<Long> ids) {
        SplitConfig<SlbArchiveGroup> config = new SplitConfig<SlbArchiveGroup>() {
            @Override
            public List<SlbArchiveGroup> doQuery(Object... args) {
                List<Long> ids = (List<Long>) args[0];
                return mapper.findAllByVsIds(ids);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                List<Long> ids = (List<Long>) args[0];
                List<List<Object>> res = new ArrayList<>();
                for (List<Long> sub: split(ids.toArray(new Long[0]))) {
                    res.add(Collections.singletonList(sub));
                }
                return res;
            }
        };
        return query(config, ids);
    }

    private List<List<Object>> splitHashesAndKeys(Object... args) throws ArgsSplitException {
        List<List<Object>> res = new ArrayList<>();

        List<Integer> hashes = (List<Integer>)args[0];
        List<String> idVersions = (List<String>)args[1];

        List<List<Integer>> subHashes = split(hashes.toArray(new Integer[0]));
        List<List<String>> subKeys = split(idVersions.toArray(new String[0]));
        Iterator<List<Integer>> iter1 = subHashes.iterator();
        Iterator<List<String>> iter2 = subKeys.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            res.add(Arrays.asList(iter1.next(), iter2.next()));
        }

        return res;
    }
}
