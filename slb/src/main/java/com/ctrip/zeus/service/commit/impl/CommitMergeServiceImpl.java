package com.ctrip.zeus.service.commit.impl;

import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.service.commit.CommitMergeService;
import com.ctrip.zeus.service.commit.util.CommitType;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2016/3/16.
 */
@Service("commitMergeService")
public class CommitMergeServiceImpl implements CommitMergeService {

    @Override
    public Commit mergeCommit(List<Commit> commitList) {
        if (commitList.size() == 0) return null;
        Commit result = new Commit();
        commitList.sort((o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            return (int) (o1.getVersion() - o2.getVersion());
        });
        Set<Long> cleanVsIds = new HashSet<>();
        Set<Long> vsIds = new HashSet<>();
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
            // Case: FullUpdate. Always Set To Result.
            // Case: Current Result Is DYUPS Or SOFT_RELOAD, Type Should Be Upgraded While Commit Type Is RELOAD Or SOFT_RELOAD.
            if (result.getType() == null || commit.getType().equalsIgnoreCase(CommitType.COMMIT_TYPE_FULL_UPDATE)) {
                result.setType(commit.getType());
            } else if ((result.getType().equals(CommitType.COMMIT_TYPE_DYUPS) || result.getType().equals(CommitType.COMMIT_TYPE_SOFT_RELOAD))
                    && (commit.getType().equals(CommitType.COMMIT_TYPE_SOFT_RELOAD) || commit.getType().equals(CommitType.COMMIT_TYPE_RELOAD))) {
                result.setType(commit.getType());
            }
            //6. clean vs ids and vs ids
            for (Long clean : commit.getCleanvsIds()) {
                if (vsIds.contains(clean)) {
                    vsIds.remove(clean);
                }
            }
            for (Long vs : commit.getVsIds()) {
                if (cleanVsIds.contains(vs)) {
                    cleanVsIds.remove(vs);
                }
            }
            vsIds.addAll(commit.getVsIds());
            cleanVsIds.addAll(commit.getCleanvsIds());
        }
        result.getVsIds().addAll(vsIds);
        result.getCleanvsIds().addAll(cleanVsIds);
        return result;
    }
}
