package com.ctrip.zeus.service.commit.impl;

import com.ctrip.zeus.dao.entity.SlbBuildCommit;
import com.ctrip.zeus.dao.entity.SlbBuildCommitExample;
import com.ctrip.zeus.dao.mapper.SlbBuildCommitMapper;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ygshen on 2016/3/15.
 */
@Component("commitService")
public class CommitServiceImpl implements CommitService {
    @Resource
    private SlbBuildCommitMapper slbBuildCommitMapper;

    @Override
    public Commit add(Commit commit) throws Exception {
        SlbBuildCommit commitDo = C.toSlbBuildCommitDo(commit);
        commitDo.setDatachangeLasttime(new Date());

        if (commit.getId() == null || commit.getId() == 0L) {
            slbBuildCommitMapper.insert(commitDo);
            commit.setId(commitDo.getId());
        } else {
            slbBuildCommitMapper.insertIncludeId(commitDo);
        }

        return commit;
    }

    @Override
    public boolean removeCommit(long slbId, long version) throws Exception {
        slbBuildCommitMapper.deleteByExample(new SlbBuildCommitExample().
                createCriteria().
                andSlbIdEqualTo(slbId).
                andVersionEqualTo(version).
                example());
        return true;
    }

    @Override
    public List<Commit> getCommitList(long slbId, long fromVersion, long toVersion) throws Exception {
        List<Commit> result = new ArrayList<>();

        List<SlbBuildCommit> commits = slbBuildCommitMapper.selectByExample(new SlbBuildCommitExample().
                createCriteria().
                andSlbIdEqualTo(slbId).
                andVersionBetween(fromVersion, toVersion).
                andVersionNotEqualTo(fromVersion).
                example());

        if (commits == null) return result;

        for (SlbBuildCommit commit : commits) {
            result.add(C.slbBuildCommitDoToCommit(commit));
        }

        return result;
    }

    @Override
    public List<Commit> getAllCommitList(long slbId) throws Exception {
        List<Commit> result = new ArrayList<>();

        List<SlbBuildCommit> commits = slbBuildCommitMapper.selectByExample(new SlbBuildCommitExample().
                createCriteria().
                andSlbIdEqualTo(slbId).
                example());

        if (commits == null) return result;

        for (SlbBuildCommit commit : commits) {
            result.add(C.slbBuildCommitDoToCommit(commit));
        }

        return result;
    }
}
