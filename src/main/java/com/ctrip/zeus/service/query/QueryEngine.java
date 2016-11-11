package com.ctrip.zeus.service.query;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.command.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class QueryEngine {
    private final Queue<String[]> params;
    private final String resource;
    private String alias;
    private final SelectionMode mode;

    private Integer offset;
    private Integer limit;
    private String order;
    private String sortProperty;

    private final GroupQueryCommand groupQueryCommand = new GroupQueryCommand();
    private final VsQueryCommand vsQueryCommand = new VsQueryCommand();
    private final SlbQueryCommand slbQueryCommand = new SlbQueryCommand();
    private final TagQueryCommand tagCommand = new TagQueryCommand();
    private final PropQueryCommand propertyCommand = new PropQueryCommand();

    private final QueryCommand[] sequenceController = new QueryCommand[5];

    public QueryEngine(Queue<String[]> params, String resource, SelectionMode mode) {
        this.params = params;
        this.resource = resource.equals("vgroup") ? "group" : resource;
        this.alias = resource.equals("vgroup") ? resource : null;
        this.mode = mode;
        switch (resource) {
            case "group":
            case "vgroup":
                sequenceController[0] = groupQueryCommand;
                sequenceController[1] = slbQueryCommand;
                sequenceController[2] = vsQueryCommand;
                break;
            case "vs":
                sequenceController[0] = vsQueryCommand;
                sequenceController[1] = groupQueryCommand;
                sequenceController[2] = slbQueryCommand;
                break;
            case "slb":
                sequenceController[0] = slbQueryCommand;
                sequenceController[1] = groupQueryCommand;
                sequenceController[2] = vsQueryCommand;
                break;
        }
        sequenceController[3] = tagCommand;
        sequenceController[4] = propertyCommand;
    }

    public void init(boolean skipable) throws ValidationException {
        Queue<String[]> curr = new LinkedList<>(params);
        Queue<String[]> next = new LinkedList<>();
        for (QueryCommand c : sequenceController) {
            while (!curr.isEmpty()) {
                String[] e = curr.poll();
                if (!c.add(e[0], e[1])) {
                    next.add(e);
                }
            }
            Queue<String[]> tmp = curr;
            curr = next;
            next = tmp;
        }

        for (String[] e : curr) {
            switch (e[0]) {
                case "order":
                    order = e[1];
                    break;
                case "limit":
                    limit = Integer.parseInt(e[1]);
                    break;
                case "offset":
                    offset = Integer.parseInt(e[1]);
                    break;
                case "sort":
                    sortProperty = e[1];
                    break;
                default:
                    break;
            }
        }

        if (!skipable && curr.size() > 0) {
            throw new ValidationException("Unsupported params " + Joiner.on(",").join(curr));
        }
    }

    public boolean sortRequired() {
        return order != null || sortProperty != null;
    }

    public int getOffset() {
        return offset == null ? 0 : offset;
    }

    public int getLimit(int max) {
        return limit == null ? ((offset == null) ? max : max - offset) : (limit > max ? max : limit);
    }

    public Boolean isAsc() {
        return !"desc".equals(order);
    }

    public String getSortProperty() {
        return sortProperty == null ? "id" : sortProperty;
    }

    public IdVersion[] run(CriteriaQueryFactory criteriaQueryFactory) throws Exception {
        // filter by tags and props
        Set<Long> pre = criteriaQueryFactory.getTagService().queryByCommand(tagCommand, resource);
        Set<Long> propItems = criteriaQueryFactory.getPropertyService().queryByCommand(propertyCommand, resource);
        if (pre == null) {
            pre = propItems;
        } else {
            if (propItems != null) pre.retainAll(propItems);
        }

        if (pre != null && pre.size() == 0) return new IdVersion[0];

        QueryCommand c = sequenceController[0];
        if (pre != null) {
            // 0 is the index of id
            if (c.hasValue(0)) {
                Set<Long> orig = new HashSet<>();
                for (String s : c.getValue(0)) {
                    orig.add(Long.parseLong(s));
                }
                pre.retainAll(orig);
            }
            c.addAtIndex(0, Joiner.on(",").join(pre));
        }

        // filter by criteria queries
        String[] traverseSequence = new String[]{sequenceController[1].getType(),
                sequenceController[2].getType(), sequenceController[0].getType()};
        IdVersion[] result = traverseQuery(traverseSequence, 0, criteriaQueryFactory);
        if (resource.equals("group")) {
            GroupCriteriaQuery q = (GroupCriteriaQuery) criteriaQueryFactory.getCriteriaQuery("group");

            Set<IdVersion> tmp;
            if (alias == null) {
                tmp = result == null ? q.queryAll(mode) : Sets.newHashSet(result);
                if (tmp.size() > 0) {
                    tmp.removeAll(q.queryAllVGroups(mode));
                }
            } else {
                if (result != null) {
                    tmp = Sets.newHashSet(result);
                    if (tmp.size() > 0) {
                        tmp.retainAll(q.queryAllVGroups(mode));
                    }
                } else {
                    tmp = q.queryAllVGroups(mode);
                }
            }
            result = tmp.toArray(new IdVersion[tmp.size()]);
        } else if (result == null) {
            Set<IdVersion> tmp = criteriaQueryFactory.getCriteriaQuery(traverseSequence[2]).queryAll(mode);
            result = tmp.toArray(new IdVersion[tmp.size()]);
        }
        return result;
    }

    private IdVersion[] traverseQuery(String[] queryCommands, int idx, CriteriaQueryFactory criteriaQueryFactory) throws Exception {
        if (idx > queryCommands.length - 1) return new IdVersion[0];

        IdVersion[] result;
        String queryCommandType = queryCommands[idx];
        CriteriaQuery q = criteriaQueryFactory.getCriteriaQuery(queryCommandType);

        switch (queryCommandType) {
            case "group": {
                result = q.queryByCommand(groupQueryCommand, mode);
                if (!"group".equals(resource) && result != null && result.length > 0) {
                    vsQueryCommand.addAtIndex(vsQueryCommand.group_search_key, nextSearchKey(result));
                }
                break;
            }
            case "vs": {
                result = q.queryByCommand(vsQueryCommand, mode);
                if (!"vs".equals(resource) && result != null && result.length > 0) {
                    slbQueryCommand.addAtIndex(slbQueryCommand.vs_search_key, nextSearchKey(result));

                    Long[] vsId = new Long[result.length];
                    for (int i = 0; i < result.length; i++) {
                        vsId[i] = result[i].getId();
                    }
                    groupQueryCommand.addAtIndex(groupQueryCommand.vs_id, Joiner.on(",").join(vsId));
                }
                break;
            }
            case "slb": {
                result = q.queryByCommand(slbQueryCommand, mode);
                if (!"slb".equals(resource) && result != null && result.length > 0) {
                    Long[] slbId = new Long[result.length];
                    for (int i = 0; i < result.length; i++) {
                        slbId[i] = result[i].getId();
                    }
                    vsQueryCommand.addAtIndex(vsQueryCommand.slb_id, Joiner.on(",").join(slbId));
                }
                break;
            }
            default:
                throw new ValidationException("Unknown query command is created. Type " + queryCommandType + " is not supported.");
        }
        if (queryCommandType.equals(resource)) {
            return result;
        } else if (result != null && result.length == 0) {
            return new IdVersion[0];
        } else {
            return traverseQuery(queryCommands, idx + 1, criteriaQueryFactory);
        }
    }

    private static String nextSearchKey(IdVersion[] searchKeys) {
        StringBuilder v = new StringBuilder();
        for (int i = 0; i < searchKeys.length; i++) {
            if (i == 0) {
                v.append(searchKeys[i].getId() + "_" + searchKeys[i].getVersion());
            } else {
                v.append(",").append(searchKeys[i].getId() + "_" + searchKeys[i].getVersion());
            }
        }
        return v.toString();
    }
}
