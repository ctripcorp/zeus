package com.ctrip.zeus.service;

import com.ctrip.zeus.dao.entity.SlbGroupVsR;
import com.ctrip.zeus.dao.entity.SlbGroupVsRExample;
import com.ctrip.zeus.dao.mapper.SlbGroupVsRMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SmartGroupVsRMapper extends AbstractSmartMapper<SlbGroupVsR> {

    @Resource
    private SlbGroupVsRMapper mapper;

    public List<SlbGroupVsR> selectByExample(SlbGroupVsRExample example) {
        return query(new SplitConfig<SlbGroupVsR>() {
            @Override
            public List<SlbGroupVsR> doQuery(Object... args) {
                return mapper.selectByExample((SlbGroupVsRExample) args[0]);
            }

            @Override
            public List<List<Object>> splitArgs(Object... args) throws ArgsSplitException {
                SlbGroupVsRExample example = (SlbGroupVsRExample) args[0];

                List<Long> vsIds = null;
                for (SlbGroupVsRExample.Criteria criteria : example.getOredCriteria()) {
                    for (SlbGroupVsRExample.Criterion criterion : criteria.getAllCriteria()) {
                        if ("vs_id in".equalsIgnoreCase(criterion.getCondition())) {
                            vsIds = (List<Long>) criterion.getValue();
                            break;
                        }
                    }
                }
                if (vsIds == null) {
                    throw new ArgsSplitException("No vs_id conditions exists in example");
                }
                List<List<Object>> res = new ArrayList<>();
                for (List<Long> sub: split(vsIds.toArray(new Long[0]))) {
                    res.add(Collections.singletonList(new SlbGroupVsRExample().or().andVsIdIn(sub).example()));
                }

                return res;
            }
        }, example);
    }
}
