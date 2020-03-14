package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.TagQueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.tag.impl.TagServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("tagCommandService")
public class TagCommandService extends TagServiceImpl {
    public Set<Long> queryByCommand(final QueryCommand command, final String type) throws Exception {
        final TagQueryCommand tagQuery = (TagQueryCommand) command;
        Long[] tagIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tagQuery.hasValue(tagQuery.union_tag);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        List<String> tn = new ArrayList<>();
                        for (String s : tagQuery.getValue(tagQuery.union_tag)) {
                            tn.add(s.trim());
                        }
                        return unionQuery(tn, type);
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return command.hasValue(tagQuery.join_tag);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        List<String> tn = new ArrayList<>();
                        for (String s : tagQuery.getValue(tagQuery.join_tag)) {
                            tn.add(s.trim());
                        }
                        return joinQuery(tn, type);
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tagQuery.hasValue(tagQuery.item_type);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        String[] tmpt = tagQuery.getValue(tagQuery.item_type);
                        if (tmpt.length > 1)
                            throw new ValidationException("Query tags does not support multiple types.");
                        return queryByType(tmpt[0]);
                    }
                }).build(Long.class).run();

        if (tagIds == null) return null;
        if (tagIds.length == 0) return new HashSet<>();

        Set<Long> result = new HashSet<>();
        for (Long i : tagIds) {
            result.add(i);
        }
        return result;
    }
}
