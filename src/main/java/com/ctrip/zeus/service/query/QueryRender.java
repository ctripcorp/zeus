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
public class QueryRender {
    private final Queue<String[]> params;
    private final String resource;
    private final SelectionMode mode;

    private final GroupQueryCommand groupQueryCommand = new GroupQueryCommand();
    private final VsQueryCommand vsQueryCommand = new VsQueryCommand();
    private final SlbQueryCommand slbQueryCommand = new SlbQueryCommand();

    private final QueryCommand[] sequenceController = new QueryCommand[3];

    public QueryRender(Queue<String[]> params, String resource, SelectionMode mode) {
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

    public void render() throws ValidationException {
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
    }

    public IdVersion[] execute(CriteriaQueryFactory criteriaQueryFactory) throws Exception {
        IdVersion[] tmp;
        QueryCommand c = sequenceController[1];
        tmp = execute(c, criteriaQueryFactory);
        if (tmp.length == 0) return tmp;
        c = sequenceController[2];
        tmp = execute(c, criteriaQueryFactory);
        if (tmp.length == 0) return tmp;
        c = sequenceController[3];
        return execute(c, criteriaQueryFactory);
    }

    private IdVersion[] execute(QueryCommand c, CriteriaQueryFactory criteriaQueryFactory) throws Exception {
        IdVersion[] tmp;
        CriteriaQuery q = criteriaQueryFactory.getCriteriaQuery(c.getType());

        switch (c.getType()) {
            case "group": {
                tmp = q.queryByCommand(groupQueryCommand, mode);
                if (!"group".equals(resource)) {
                    vsQueryCommand.addAtIndex(vsQueryCommand.group_search_key, nextSearchKey(tmp));
                }
                break;
            }
            case "vs": {
                tmp = q.queryByCommand(vsQueryCommand, mode);
                if (!"vs".equals(resource)) {
                    slbQueryCommand.addAtIndex(slbQueryCommand.vs_search_key, nextSearchKey(tmp));

                    Long[] vsId = new Long[tmp.length];
                    for (int i = 0; i < tmp.length; i++) {
                        vsId[i] = tmp[i].getId();
                    }
                    groupQueryCommand.addAtIndex(groupQueryCommand.vs_id, Joiner.on(",").join(vsId));
                }
                break;
            }
            case "slb": {
                tmp = q.queryByCommand(slbQueryCommand, mode);
                if (!"slb".equals(resource)) {
                    Long[] slbId = new Long[tmp.length];
                    for (int i = 0; i < tmp.length; i++) {
                        slbId[i] = tmp[i].getId();
                    }
                    vsQueryCommand.addAtIndex(vsQueryCommand.slb_id, Joiner.on(",").join(slbId));
                }
            }
            default:
                throw new ValidationException("Unknown query command is created. Type " + c.getType() + " is not supported.");
        }
        return tmp;
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
