package com.ctrip.zeus.service;

import com.ctrip.zeus.dao.entity.SlbGroupStatusR;
import com.ctrip.zeus.dao.entity.SlbGroupStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbGroupStatusRMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SmartGroupStatusRMapper extends AbstractSmartMapper<SlbGroupStatusR> {

    @Resource
    private SlbGroupStatusRMapper mapper;

    public List<SlbGroupStatusR> selectByExample(SlbGroupStatusRExample example) {
        SplitConfig<SlbGroupStatusR> config = new SplitConfig<SlbGroupStatusR>() {
            @Override
            public List<SlbGroupStatusR> doQuery(Object... args) {
                return mapper.selectByExample((SlbGroupStatusRExample)args[0]);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                SlbGroupStatusRExample example = (SlbGroupStatusRExample) args[0];

                List<Long> groupIds = null;
                for (SlbGroupStatusRExample.Criteria criteria : example.getOredCriteria()) {
                    for (SlbGroupStatusRExample.Criterion criterion : criteria.getAllCriteria()) {
                        if ("group_id in".equalsIgnoreCase(criterion.getCondition())) {
                            groupIds = (List<Long>) criterion.getValue();
                            break;
                        }
                    }
                }
                if (groupIds == null) {
                    throw new ArgsSplitException("group_id in condition not exists");
                }
                List<List<Object>> res = new ArrayList<>();
                for (List<Long> sub: split(groupIds.toArray(new Long[0]))) {
                    res.add(Arrays.asList(new SlbGroupStatusRExample().or().andGroupIdIn(sub).example()));
                }

                return res;
            }
        };
        return query(config, example);
    }
}
