package com.ctrip.zeus.service.commit;

import com.ctrip.zeus.model.commit.Commit;

import java.util.List;

/**
 * Created by fanqq on 2016/3/16.
 */
public interface CommitMergeService {
    /**
     * merge commits to one commit
     *
     * @return Commit
     * @throws Exception
     */
    Commit mergeCommit(List<Commit> commitList);
}
