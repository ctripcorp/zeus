package com.ctrip.zeus.service.tools.redirect.impl;

import com.ctrip.zeus.dao.entity.ToolsVsRedirect;
import com.ctrip.zeus.dao.entity.ToolsVsRedirectExample;
import com.ctrip.zeus.dao.mapper.ToolsVsRedirectMapper;
import com.ctrip.zeus.model.tools.VsRedirect;
import com.ctrip.zeus.model.tools.VsRedirectList;
import com.ctrip.zeus.service.tools.redirect.FlowVsRedirectService;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("flowVsRedirectService")
public class FlowVsRedirectServiceImpl implements FlowVsRedirectService {

    @Resource
    private ToolsVsRedirectMapper toolsVsRedirectMapper;

    @Override
    public VsRedirect add(VsRedirect redirect) throws Exception {
        ToolsVsRedirect target = ToolsVsRedirect.builder().name(redirect.getName()).status(redirect.getStatus()).content(ObjectJsonWriter.write(redirect)).build();
        toolsVsRedirectMapper.insert(target);
        return redirect.setId(target.getId());
    }

    @Override
    public VsRedirect update(VsRedirect redirect) throws Exception {
        ToolsVsRedirect target = ToolsVsRedirect.builder().name(redirect.getName()).status(redirect.getStatus()).content(ObjectJsonWriter.write(redirect)).id(redirect.getId()).build();
        toolsVsRedirectMapper.updateByPrimaryKeyWithBLOBs(target);
        return redirect;
    }

    @Override
    public boolean delete(Long id) throws Exception {
        int i = toolsVsRedirectMapper.deleteByExample(new ToolsVsRedirectExample().createCriteria().andIdEqualTo(id).example());
        return i == 1;
    }

    @Override
    public VsRedirect get(Long id) throws Exception {
        ToolsVsRedirect toolsVsRedirect = toolsVsRedirectMapper.selectOneByExampleWithBLOBs(new ToolsVsRedirectExample().createCriteria().andIdEqualTo(id).example());
        VsRedirect result = ObjectJsonParser.parse(toolsVsRedirect.getContent(), VsRedirect.class);
        result.setId(id);
        return result;
    }

    @Override
    public VsRedirectList list() throws Exception {
        VsRedirectList result = new VsRedirectList();
        List<ToolsVsRedirect> redirects = toolsVsRedirectMapper.selectByExampleWithBLOBs(new ToolsVsRedirectExample().createCriteria().example());
        for (ToolsVsRedirect redirect : redirects) {
            VsRedirect temp = ObjectJsonParser.parse(redirect.getContent(), VsRedirect.class);
            temp.setId(redirect.getId());
            result.addVsRedirect(temp);
        }
        result.setTotal(redirects.size());
        return result;
    }
}
