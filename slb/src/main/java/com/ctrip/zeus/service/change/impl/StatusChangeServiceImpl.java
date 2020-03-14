package com.ctrip.zeus.service.change.impl;

import com.ctrip.zeus.dao.entity.UnhealthyAlertItem;
import com.ctrip.zeus.dao.entity.UnhealthyAlertItemExample;
import com.ctrip.zeus.dao.mapper.UnhealthyAlertItemMapper;
import com.ctrip.zeus.model.alert.AlertItem;
import com.ctrip.zeus.service.change.StatusChangeService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author:ygshen
 * @date: 2/26/2018.
 */
@Service("statusChangeService")
public class StatusChangeServiceImpl implements StatusChangeService {

    @Resource
    private UnhealthyAlertItemMapper unhealthyAlertItemMapper;

    @Override
    public List<AlertItem> batchAddStatusChange(List<AlertItem> item) throws Exception {
        if (item == null || item.size() == 0) return null;

        List<UnhealthyAlertItem> records = new ArrayList<>();
        for (AlertItem alertItem : item) {
            UnhealthyAlertItem insert = C.toUnhealthyAlertItem(alertItem);
            insert.setDatachangeLasttime(new Date());
            records.add(insert);
        }
        unhealthyAlertItemMapper.batchInsert(records);
        return item;
    }

    @Override
    public List<AlertItem> getStatusChangesByTypeAndIds(Long[] ids, String type) {
        List<UnhealthyAlertItem> records = selectByIdsAndTypeAndStatusNew(ids, type, null);
        return batchToAlertItems(records);
    }

    @Override
    public List<AlertItem> getStatusChangesByTypeIdsAndStatus(Long[] ids, String type, boolean status) {
        List<UnhealthyAlertItem> records = selectByIdsAndTypeAndStatusNew(ids, type, status);
        return batchToAlertItems(records);
    }

    @Override
    public List<AlertItem> getStatusChangesByTypeAndStatus(String type, boolean status) {
        List<UnhealthyAlertItem> records = selectByIdsAndTypeAndStatusNew(null, type, status);
        return batchToAlertItems(records);
    }

    @Override
    public List<AlertItem> batchUpdateStatusChanges(List<AlertItem> changes) throws Exception {
        if (changes == null || changes.size() == 0) {
            return new ArrayList<>();
        }

        List<UnhealthyAlertItem> items = new ArrayList<>();
        for (AlertItem change : changes) {
            UnhealthyAlertItem update = C.toUnhealthyAlertItem(change);
            update.setDatachangeLasttime(new Date());
            items.add(update);
        }
        unhealthyAlertItemMapper.batchUpdate(items);

        return changes;
    }

    public List<UnhealthyAlertItem> selectByIdsAndTypeAndStatusNew(Long[] ids, String type, Boolean status) {
        UnhealthyAlertItemExample example = new UnhealthyAlertItemExample();
        UnhealthyAlertItemExample.Criteria criteria = example.createCriteria();
        if (ids != null && ids.length > 0) {
            criteria.andTargetIn(Arrays.asList(ids));
        }
        if (type != null) {
            criteria.andTypeEqualTo(type);
        }
        if (status != null) {
            criteria.andStatusEqualTo(status);
        }
        return unhealthyAlertItemMapper.selectByExampleWithBLOBs(example);
    }

    private List<AlertItem> batchToAlertItems(List<UnhealthyAlertItem> records) {
        if (records == null || records.size() == 0) {
            return new ArrayList<>();
        }
        List<AlertItem> results = new ArrayList<>(records.size());
        for (UnhealthyAlertItem record : records) {
            results.add(C.toAlertItem(record));
        }
        return results;
    }
}
