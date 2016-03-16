package com.ctrip.zeus.service.commit.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.dal.core.CommitDao;
import com.ctrip.zeus.dal.core.CommitDo;
import com.ctrip.zeus.dal.core.CommitEntity;
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
    public CommitDao commitDao;

    @Override
    public Commit add(Commit commit) throws Exception {
        CommitDo commitDto = C.toCommitDo(commit);
        commitDto.setDataChangeLastTime(new Date());
        commitDao.insert(commitDto);
        return commit;
    }

    @Override
    public boolean removeCommit(long slbId, long version) throws Exception {
        CommitDo commitDo = new CommitDo();
        commitDo.setSlbId(slbId).setVersion(version);

        commitDao.deleteBySlbIdVersion(commitDo);
        return true;
    }

    @Override
    public List<Commit> getCommitList(long slbId, long fromVersion, long toVersion) throws Exception {
        List<CommitDo> commits = commitDao.findAllByVersionRange(slbId, fromVersion, toVersion, CommitEntity.READSET_FULL);

        List<Commit> result = new ArrayList<>();
        if (commits == null) return result;

        for (CommitDo commit : commits) {
            result.add(C.toCommit(commit));
        }

        return result;
    }

    @Override
    public List<Commit> getAllCommitList(long slbId) throws Exception {
        List<Commit> result = new ArrayList<>();

        List<CommitDo> commits = commitDao.findAllBySlbId(slbId, CommitEntity.READSET_FULL);
        if (commits == null) return result;

        for (CommitDo commit : commits) {
            result.add(C.toCommit(commit));

        }
        return result;
    }
}
