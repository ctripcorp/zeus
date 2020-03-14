package com.ctrip.zeus.service;

import com.ctrip.zeus.dao.entity.SlbArchiveVs;
import com.ctrip.zeus.dao.mapper.SlbArchiveVsMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Component
public class SmartArchiveVsMapper extends AbstractSmartMapper<SlbArchiveVs> {

    @Resource
    private SlbArchiveVsMapper mapper;

    public List<SlbArchiveVs> findAllByIdVersion(List<Integer> hashes, List<String> id_version_array) {
        return query(new SplitConfig<SlbArchiveVs>() {
            @Override
            public List<SlbArchiveVs> doQuery(Object... args) {
                List<Integer> hashes = (List<Integer>)args[0];
                List<String> idVersions = (List<String>)args[1];
                return mapper.findAllByIdVersion(hashes, idVersions);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
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
        }, hashes, id_version_array);
    }
}
