package com.ctrip.zeus.service.tools.cert.impl;

import com.ctrip.zeus.dao.entity.ToolsCertUpgrade;
import com.ctrip.zeus.dao.entity.ToolsCertUpgradeExample;
import com.ctrip.zeus.dao.mapper.ToolsCertUpgradeMapper;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.tools.CertUpgrade;
import com.ctrip.zeus.service.tools.cert.FlowCertUpgradeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("certUpgradeService")
public class FlowCertUpgradeServiceImpl implements FlowCertUpgradeService {
    @Resource
    private ToolsCertUpgradeMapper toolsCertUpgradeMapper;

    @Override
    public CertUpgrade create(CertUpgrade certUpgrade) throws Exception {
        if (certUpgrade == null) throw new ValidationException("Cert upgrade shall not be null");

        ToolsCertUpgrade entity = ToolsCertUpgrade.builder().name2(certUpgrade.getName()).status2(certUpgrade.getStatus()).content(certUpgrade.getContent().getBytes("UTF-8")).build();
        toolsCertUpgradeMapper.insert(entity);

        certUpgrade.setId(entity.getId());
        return certUpgrade;
    }

    @Override
    public CertUpgrade get(Long id) throws Exception {
        if (id == null || id <= 0) throw new ValidationException("id is required and shall be greater than 0");

        ToolsCertUpgrade toolsCertUpgrade = toolsCertUpgradeMapper.selectByPrimaryKey(id);
        if (toolsCertUpgrade == null)
            throw new NotFoundException("Could not find certificate upgrade process with id: " + id);
        return new CertUpgrade().setStatus(toolsCertUpgrade.getStatus2()).setId(toolsCertUpgrade.getId()).setName(toolsCertUpgrade.getName2()).setContent(new String(toolsCertUpgrade.getContent()));
    }

    @Override
    public CertUpgrade update(CertUpgrade certUpgrade) throws Exception {
        if (certUpgrade == null || certUpgrade.getId() == null || certUpgrade.getId() <= 0)
            throw new ValidationException("Upgrade entity shall be valid");

        ToolsCertUpgrade existed = toolsCertUpgradeMapper.selectByPrimaryKey(certUpgrade.getId());
        if (existed == null) throw new ValidationException("Upgrade entity could not be found");

        toolsCertUpgradeMapper.updateByPrimaryKeyWithBLOBs(ToolsCertUpgrade.builder().name2(existed.getName2()).id(certUpgrade.getId()).status2(certUpgrade.getStatus()).content(certUpgrade.getContent().getBytes("UTF-8")).build());
        return certUpgrade;
    }

    @Override
    public List<CertUpgrade> list() throws Exception {
        List<ToolsCertUpgrade> upgrades = toolsCertUpgradeMapper.selectByExampleWithBLOBs(new ToolsCertUpgradeExample());
        List<CertUpgrade> results = new ArrayList<>();
        for (ToolsCertUpgrade toolsCertUpgrade : upgrades) {
            results.add(new CertUpgrade().setStatus(toolsCertUpgrade.getStatus2()).setId(toolsCertUpgrade.getId()).setName(toolsCertUpgrade.getName2()).setContent(new String(toolsCertUpgrade.getContent())));
        }
        return results;
    }
}
