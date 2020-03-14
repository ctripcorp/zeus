package com.ctrip.zeus.service.commit;

import com.ctrip.zeus.model.commit.Commit;

import java.util.List;

/**
 * Created by ygshen on 2016/3/15.
 * Commit operation interface definition
 */
public interface CommitService {

    // Add a new commit and return the commit id
    Commit add(Commit commit) throws Exception;

    // Remove the commit by slbId and version
    boolean removeCommit(long slbId, long version) throws Exception;

    // Get all the commits
    List<Commit> getAllCommitList(long slbId) throws Exception;

    // get all commits between (fromVersion,toVersion]
    List<Commit> getCommitList(long slbId, long fromVersion, long toVersion) throws Exception;
}
