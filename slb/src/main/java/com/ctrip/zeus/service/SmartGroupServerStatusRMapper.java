package com.ctrip.zeus.service;

import com.ctrip.zeus.dao.entity.SlbGroupServerStatus;
import com.ctrip.zeus.dao.entity.SlbGroupServerStatusExample;
import com.ctrip.zeus.dao.mapper.SlbGroupServerStatusMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SmartGroupServerStatusRMapper extends AbstractSmartMapper<SlbGroupServerStatus> {

    @Resource
    private SlbGroupServerStatusMapper mapper;

    public List<SlbGroupServerStatus> selectByExample(SlbGroupServerStatusExample example) {
        SplitConfig<SlbGroupServerStatus> config = new SplitConfig<SlbGroupServerStatus>() {
            @Override
            public List<SlbGroupServerStatus> doQuery(Object... args) {
                return mapper.selectByExample((SlbGroupServerStatusExample) args[0]);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                SlbGroupServerStatusExample example = (SlbGroupServerStatusExample) args[0];

                List<Long> groupIds = null;
                for (SlbGroupServerStatusExample.Criteria criteria : example.getOredCriteria()) {
                    for (SlbGroupServerStatusExample.Criterion criterion : criteria.getAllCriteria()) {
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
                    res.add(Arrays.asList(new SlbGroupServerStatusExample().or().andGroupIdIn(sub).example()));
                }

                return res;
            }
        };
        return query(config, example);
    }
}
