package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.service.query.command.PropQueryCommand;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.tag.impl.PropertyServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("propertyCommandService")
public class PropertyCommandService extends PropertyServiceImpl {
    public Set<Long> queryByCommand(QueryCommand command, final String type) throws Exception {
        Set<Long> result = null;

        PropQueryCommand propQuery = (PropQueryCommand) command;
        while (propQuery != null) {
            final PropQueryCommand tmp = propQuery;
            Long[] filteredPropIds = new QueryExecuter.Builder<Long>()
                    .addFilter(new FilterSet<Long>() {
                        @Override
                        public boolean shouldFilter() throws Exception {
                            return tmp.hasValue(tmp.union_prop);
                        }

                        @Override
                        public Set<Long> filter() throws Exception {
                            return unionQuery(tmp.getProperties(tmp.union_prop), type);
                        }
                    })
                    .addFilter(new FilterSet<Long>() {
                        @Override
                        public boolean shouldFilter() throws Exception {
                            return tmp.hasValue(tmp.join_prop);
                        }

                        @Override
                        public Set<Long> filter() throws Exception {
                            return joinQuery(tmp.getProperties(tmp.join_prop), type);
                        }
                    })
                    .addFilter(new FilterSet<Long>() {
                        @Override
                        public boolean shouldFilter() throws Exception {
                            return tmp.hasValue(tmp.item_type);
                        }

                        @Override
                        public Set<Long> filter() throws Exception {
                            List<String> t = new ArrayList<>();
                            for (String s : tmp.getValue(PropQueryCommand.item_type)) {
                                t.add(s.trim());
                            }
                            Set<Long> result = new HashSet<>();
                            for (String s : t) {
                                result.retainAll(queryByType(s));
                            }
                            return result;
                        }
                    }).build(Long.class).run();

            if (filteredPropIds != null) {
                if (filteredPropIds.length == 0) return new HashSet<>();

                Set<Long> tmpIds = new HashSet<>();
                for (Long i : filteredPropIds) {
                    tmpIds.add(i);
                }

                if (result == null) {
                    result = tmpIds;
                } else {
                    result.retainAll(tmpIds);
                }
            }
            propQuery = propQuery.next();
        }
        return result;
    }
}
