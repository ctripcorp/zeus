package com.ctrip.zeus.service.commit;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.commit.entity.CommitList;

/**
 * Created by ygshen on 2016/3/15.
 * Commit operation interface definition
 */
public interface CommitService {

    // Add a new commit and return the commit id
    public long add(Commit commit) throws Exception;

    // Remove the commit by slbId and version
    public boolean removeCommit(long slbId,long version) throws Exception;

    // Get all the commits
    public CommitList getAllCommitList(long slbId) throws Exception;

    // get all commits between (fromVersion,toVersion]
    public CommitList getCommitList(long slbId, long fromVersion, long toVersion) throws Exception;
}
