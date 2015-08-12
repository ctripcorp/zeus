package com.ctrip.zeus.service.activate.impl;

import com.ctrip.zeus.dal.core.ConfGroupActiveDao;
import com.ctrip.zeus.dal.core.ConfGroupActiveDo;
import com.ctrip.zeus.dal.core.ConfGroupActiveEntity;
import com.ctrip.zeus.service.activate.GroupActivateConfRewrite;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/8/12.
 */
@Component("groupActivateConfRewrite")
public class GroupActivateConfRewriteImpl implements GroupActivateConfRewrite {
    @Resource
    private ConfGroupActiveDao confGroupActiveDao;
    @Override
    public void rewriteAllGroupActivteConf() throws Exception {
        List<ConfGroupActiveDo> allGroupConf = confGroupActiveDao.findAll(ConfGroupActiveEntity.READSET_FULL);
//        List<ConfGroupActiveDo> newGroupConf = new ArrayList<>();
        for (ConfGroupActiveDo confGroupActiveDo : allGroupConf){
            String content = confGroupActiveDo.getContent();
            String newContent = content;//xxx.parser(content);
            confGroupActiveDo.setContent(newContent);
        }
        confGroupActiveDao.insert(allGroupConf.toArray(new ConfGroupActiveDo[allGroupConf.size()]));
    }
}
