package com.ctrip.zeus.flow.vsmigration.impl;

import com.ctrip.zeus.dao.entity.ToolsVsMigration;
import com.ctrip.zeus.dao.entity.ToolsVsMigrationExample;
import com.ctrip.zeus.dao.mapper.ToolsVsMigrationMapper;
import com.ctrip.zeus.flow.vsmigration.FlowVsMigrationService;
import com.ctrip.zeus.model.tools.VsMigration;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("flowVsMigrationService")
public class FlowFlowVsMigrationImpl implements FlowVsMigrationService {

    @Resource
    private ToolsVsMigrationMapper toolsVsMigrationMapper;

    @Override
    public VsMigration newVsMigration(VsMigration migration) throws Exception {
        migration.setStatus(true);
        ToolsVsMigration insert = C.toToolsVsMigration(migration);
        insert.setDatachangeLasttime(new Date());
        toolsVsMigrationMapper.insert(insert);
        Long id = insert.getId();
        if (id != null) {
            migration.setId(id);
        }
        return migration;
    }

    @Override
    public VsMigration updateVsMigration(VsMigration migration) throws Exception {
        ToolsVsMigration record = toolsVsMigrationMapper.selectByPrimaryKey(migration.getId());
        record.setName(migration.getName());
        record.setContent(migration.getContent().getBytes());
        record.setDatachangeLasttime(new Date());
        toolsVsMigrationMapper.updateByPrimaryKeySelective(record);
        return migration;
    }

    @Override
    public VsMigration getVsMigration(long id) throws Exception {
        ToolsVsMigration toolsVsMigration = toolsVsMigrationMapper.selectByPrimaryKey(id);
        return C.toVsMigration(toolsVsMigration);
    }

    @Override
    public List<VsMigration> getAllMigrationByStatus(boolean activated) throws Exception {
        List<VsMigration> results = new ArrayList<>();

        List<ToolsVsMigration> temp = toolsVsMigrationMapper.selectByExampleWithBLOBs(new ToolsVsMigrationExample().createCriteria().andStatusEqualTo(activated).example());

        for (ToolsVsMigration item : temp) {
            results.add(C.toVsMigration(item));
        }
        return results;
    }

    @Override
    public List<VsMigration> getAllMigration() throws Exception {
        List<VsMigration> results = new ArrayList<>();

        List<ToolsVsMigration> temp = toolsVsMigrationMapper.selectByExampleWithBLOBs(new ToolsVsMigrationExample());
        for (ToolsVsMigration item : temp) {
            results.add(C.toVsMigration(item));
        }
        return results;
    }

    @Override
    public boolean deleteVsMigration(VsMigration migration) throws Exception {
        ToolsVsMigration record = toolsVsMigrationMapper.selectByPrimaryKey(migration.getId());
        record.setStatus(false);
        record.setDatachangeLasttime(new Date());
        int rows = toolsVsMigrationMapper.updateByPrimaryKeySelective(record);
        return rows > 0;
    }

    @Override
    public boolean clearVsMigration(VsMigration migration) throws Exception {
        int row =  toolsVsMigrationMapper.deleteByPrimaryKey(migration.getId());
        return row > 0;
    }
}
