package com.ctrip.zeus.service.Activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.Activate.ActiveConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/3/30.
 */
@Component("activeConfService")
public class ActiveConfServiceImpl implements ActiveConfService {
    @Resource
    private ConfAppActiveDao confAppActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;

    private Logger logger = LoggerFactory.getLogger(ActiveConfServiceImpl.class);

    @Override
    public List<String> getConfAppActiveContentByAppNames(String[] appnames) throws Exception {


        List<ConfAppActiveDo> l = confAppActiveDao.findAllByNames(appnames, ConfAppActiveEntity.READSET_FULL);

        List<String> res = new ArrayList<>();

        if (l==null)
        {
            logger.warn("No ConfAppActive for apps:"+appnames.toString());
            return res;
        }

        for (ConfAppActiveDo a : l)
        {
            res.add(a.getContent());
        }
        return res;
    }

    @Override
    public String getConfSlbActiveContentBySlbNames(String slbname) throws Exception {
         ConfSlbActiveDo d = confSlbActiveDao.findByName(slbname, ConfSlbActiveEntity.READSET_FULL);
        if (d==null)
        {
            logger.warn("No conf slb active for Slbname: "+slbname);
            return null;
        }

        return d.getContent();
    }
}
