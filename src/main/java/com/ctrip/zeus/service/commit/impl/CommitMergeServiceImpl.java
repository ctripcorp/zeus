package com.ctrip.zeus.service.commit.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.service.commit.CommitMergeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fanqq on 2016/3/16.
 */
@Service("commitMergeService")
public class CommitMergeServiceImpl implements CommitMergeService {

    private final String COMMIT_TYPE_RELOAD = "Reload";
    private final String COMMIT_TYPE_DYUPS = "Dyups";

    @Override
    public Commit mergeCommit(List<Commit> commitList) throws Exception {
        if (commitList.size() == 0) return null;
        Commit result = new Commit();
        Collections.sort(commitList, new Comparator<Commit>() {
            @Override
            public int compare(Commit o1, Commit o2) {
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return -1;
                if (o2 == null) return 1;
                return (int) (o1.getVersion() - o2.getVersion());
            }
        });
        for (Commit commit : commitList) {
            //1. slbId
            result.setSlbId(commit.getSlbId());
            //2. version
            result.setVersion(commit.getVersion());
            //3. group ids
            result.getGroupIds().addAll(commit.getGroupIds());
            //4. task ids
            result.getTaskIds().addAll(commit.getTaskIds());
            //5. type
            if (result.getType() == null){
                result.setType(commit.getType());
            }else if (result.getType().equals(COMMIT_TYPE_DYUPS) && commit.getType().equals(COMMIT_TYPE_RELOAD)){
                result.setType(commit.getType());
            }
            //6. clean vs ids and vs ids
            for (Long clean : commit.getCleanvsIds()){
                if (result.getVsIds().contains(clean)){
                    result.getVsIds().remove(clean);
                }
            }
            for (Long vs : commit.getVsIds()){
                if (result.getCleanvsIds().contains(vs)){
                    result.getCleanvsIds().remove(vs);
                }
            }
        }
        return result;
    }
}
