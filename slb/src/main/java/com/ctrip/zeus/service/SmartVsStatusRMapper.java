package com.ctrip.zeus.service;

import com.ctrip.zeus.dao.entity.SlbVsStatusR;
import com.ctrip.zeus.dao.entity.SlbVsStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbVsStatusRMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SmartVsStatusRMapper extends AbstractSmartMapper<SlbVsStatusR> {

    @Resource
    private SlbVsStatusRMapper mapper;

    public List<SlbVsStatusR> selectByExample(SlbVsStatusRExample example) {
        SplitConfig<SlbVsStatusR> config = new SplitConfig<SlbVsStatusR>() {
            @Override
            public List<SlbVsStatusR> doQuery(Object... args) {
                return mapper.selectByExample((SlbVsStatusRExample) args[0]);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                SlbVsStatusRExample example = (SlbVsStatusRExample) args[0];

                List<Long> vsIds = null;
                for (SlbVsStatusRExample.Criteria criteria : example.getOredCriteria()) {
                    for (SlbVsStatusRExample.Criterion criterion : criteria.getAllCriteria()) {
                        if ("vs_id in".equalsIgnoreCase(criterion.getCondition())) {
                            vsIds = (List<Long>) criterion.getValue();
                            break;
                        }
                    }
                }
                if (vsIds == null) {
                    throw new ArgsSplitException("vs_id in condition not exists");
                }
                List<List<Object>> res = new ArrayList<>();
                for (List<Long> sub: split(vsIds.toArray(new Long[0]))) {
                    res.add(Arrays.asList(new SlbVsStatusRExample().or().andVsIdIn(sub).example()));
                }

                return res;
            }
        };

        return query(config, example);
    }
}
