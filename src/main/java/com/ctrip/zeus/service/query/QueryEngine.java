package com.ctrip.zeus.service.query;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.command.GroupQueryCommand;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.SlbQueryCommand;
import com.ctrip.zeus.service.query.command.VsQueryCommand;
import com.google.common.base.Joiner;

import java.util.*;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class QueryEngine {
    private final Queue<String[]> params;
    private final String resource;
    private final SelectionMode mode;

    private final GroupQueryCommand groupQueryCommand = new GroupQueryCommand();
    private final VsQueryCommand vsQueryCommand = new VsQueryCommand();
    private final SlbQueryCommand slbQueryCommand = new SlbQueryCommand();

    private final QueryCommand[] sequenceController = new QueryCommand[3];

    public QueryEngine(Queue<String[]> params, String resource, SelectionMode mode) {
        this.params = params;
        this.resource = resource;
        this.mode = mode;
        switch (resource) {
            case "group":
                sequenceController[0] = groupQueryCommand;
                sequenceController[1] = vsQueryCommand;
                sequenceController[2] = slbQueryCommand;
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
        if (!skipable && curr.size() > 0) {
            throw new ValidationException("Unsupported params " + Joiner.on(",").join(curr));
        }
    }

    public IdVersion[] run(CriteriaQueryFactory criteriaQueryFactory) throws Exception {
        String[] traverseSequence = new String[]{sequenceController[1].getType(),
                sequenceController[2].getType(), sequenceController[0].getType()};
        return traverseQuery(traverseSequence, 0, criteriaQueryFactory);
    }

    private IdVersion[] traverseQuery(String[] queryCommands, int idx, CriteriaQueryFactory criteriaQueryFactory) throws Exception {
        if (idx > queryCommands.length - 1) return new IdVersion[0];

        IdVersion[] result;
        String queryCommandType = queryCommands[idx];
        CriteriaQuery q = criteriaQueryFactory.getCriteriaQuery(queryCommandType);

        switch (queryCommandType) {
            case "group": {
                result = q.queryByCommand(groupQueryCommand, mode);
                if (!"group".equals(resource)) {
                    vsQueryCommand.addAtIndex(vsQueryCommand.group_search_key, nextSearchKey(result));
                }
                break;
            }
            case "vs": {
                result = q.queryByCommand(vsQueryCommand, mode);
                if (!"vs".equals(resource)) {
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
                if (!"slb".equals(resource)) {
                    Long[] slbId = new Long[result.length];
                    for (int i = 0; i < result.length; i++) {
                        slbId[i] = result[i].getId();
                    }
                    vsQueryCommand.addAtIndex(vsQueryCommand.slb_id, Joiner.on(",").join(slbId));
                }
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
