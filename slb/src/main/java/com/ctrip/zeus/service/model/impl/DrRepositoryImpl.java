package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbArchiveDrMapper;
import com.ctrip.zeus.dao.mapper.SlbDrMapper;
import com.ctrip.zeus.dao.mapper.SlbDrStatusRMapper;
import com.ctrip.zeus.dao.mapper.SlbDrVsRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Destination;
import com.ctrip.zeus.model.model.Dr;
import com.ctrip.zeus.model.model.DrTraffic;
import com.ctrip.zeus.service.model.DrRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import com.ctrip.zeus.service.model.validation.DrValidator;
import com.ctrip.zeus.service.query.DrCriteriaQuery;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component("drRepository")
public class DrRepositoryImpl implements DrRepository {

    @Resource
    private SlbDrMapper slbDrMapper;

    @Resource
    private SlbArchiveDrMapper slbArchiveDrMapper;

    @Resource
    private SlbDrStatusRMapper slbDrStatusRMapper;

    @Resource
    private SlbDrVsRMapper slbDrVsRMapper;

    @Resource
    private DrCriteriaQuery drCriteriaQuery;
    @Resource
    private DrValidator drValidator;
    @Resource
    private ValidationFacade validationFacade;

    @Resource
    private TagBox tagBox;

    @Resource
    private TagService tagService;

    private final static String GROUPID_PREFIX = "groupid_";

    @Override
    public List<Dr> list() throws Exception {
        List<SlbDr> list = slbDrMapper.selectByExample(new SlbDrExample().createCriteria().example());
        IdVersion[] currentVersion = new IdVersion[list.size()];
        for (int i = 0; i < list.size(); i++) {
            currentVersion[i] = new IdVersion(list.get(i).getId(), list.get(i).getVersion());
        }
        return list(currentVersion);
    }

    @Override
    public List<Dr> list(IdVersion[] keys) throws Exception {
        List<Dr> result = new ArrayList<>();
        if (keys == null || keys.length == 0) return result;

        String[] values = new String[keys.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = keys[i].toString();
        }

        for (SlbArchiveDr archiveDrDo : slbArchiveDrMapper.findAllByIdVersion(values)) {
            try {
                Dr dr = ContentReaders.readDrContent(archiveDrDo.getContent());
                dr.setCreatedTime(archiveDrDo.getDatachangeLasttime());
                result.add(dr);
            } catch (Exception e) {
            }
        }
        return result;
    }

    @Override
    public Dr getById(Long drId) throws Exception {
        IdVersion[] key = drCriteriaQuery.queryByIdAndMode(drId, SelectionMode.OFFLINE_FIRST);
        if (key.length == 0)
            return null;
        return getByKey(key[0]);
    }

    @Override
    public Dr getByKey(IdVersion key) throws Exception {
        List<Dr> drs = list(new IdVersion[]{key});
        if (drs == null || drs.isEmpty()) return null;
        return drs.get(0);
    }

    @Override
    public void delete(Long drId) throws Exception {
        drValidator.removable(drId);
        slbDrStatusRMapper.deleteByExample(new SlbDrStatusRExample().createCriteria().andDrIdEqualTo(drId).example());
        slbDrVsRMapper.deleteByExample(new SlbDrVsRExample().createCriteria().andDrIdEqualTo(drId).example());
        // clear dr tag of group
        tagBox.clear("dr", drId);
        slbDrMapper.deleteByPrimaryKey(drId);
        slbArchiveDrMapper.deleteByExample(new SlbArchiveDrExample().createCriteria().andDrIdEqualTo(drId).example());
    }

    @Override
    public Dr add(Dr dr) throws Exception {
        dr.setId(null);
        ValidationContext context = new ValidationContext();
        validationFacade.validateDr(dr, context);
        if (context.getErrorDrs().contains(dr.getId())) {
            throw new ValidationException(context.getDrErrorReason(dr.getId()));
        }

        SlbDr drDo = SlbDr.builder().id(null).name(dr.getName()).version(1).createdTime(new Date()).build();
        slbDrMapper.insert(drDo);
        dr.setId(drDo.getId()).setVersion(drDo.getVersion());
        maintainRelations(dr);

        return dr;
    }

    @Override
    public Dr update(Dr dr) throws Exception {
        drValidator.checkRestrictionForUpdate(dr);
        ValidationContext context = new ValidationContext();
        validationFacade.validateDr(dr, context);

        if (context.getErrorDrs().contains(dr.getId())) {
            throw new ValidationException(context.getDrErrorReason(dr.getId()));
        }

        SlbDr drDo = slbDrMapper.selectByPrimaryKey(dr.getId());
        if (drDo == null) {
            throw new ValidationException("Dr " + dr.getId() + " that you tried to update does not exists.");
        }
        updateDr(dr);
        return dr;
    }

    @Override
    public void updateActiveStatus(IdVersion[] drs) throws Exception {
        Long[] ids = new Long[drs.length];
        for (int i = 0; i < drs.length; i++) {
            ids[i] = drs[i].getId();
        }
        if (ids.length == 0) return;

        List<SlbDrStatusR> drDos = slbDrStatusRMapper.selectByExample(new SlbDrStatusRExample().createCriteria().andDrIdIn(Arrays.asList(ids)).example());
        for (SlbDrStatusR drStatusDo : drDos) {
            int i = Arrays.binarySearch(ids, drStatusDo.getDrId());
            drStatusDo.setOnlineVersion(drs[i].getVersion());
        }
        slbDrStatusRMapper.updateOnlineVersionByDr(drDos);
    }


    private void maintainRelations(Dr dr) throws Exception {
        slbArchiveDrMapper.insert(SlbArchiveDr.
                builder().
                version(dr.getVersion()).
                drId(dr.getId()).
                content(ContentWriters.writeDrContent(dr)).
                build());

        int offlineVersion = dr.getVersion() == null ? 0 : dr.getVersion();
        SlbDrStatusR data = SlbDrStatusR.builder().drId(dr.getId()).offlineVersion(offlineVersion).build();
        SlbDrStatusR drStatusR = slbDrStatusRMapper.selectOneByExample(new SlbDrStatusRExample().createCriteria().andDrIdEqualTo(dr.getId()).example());
        if (drStatusR != null) {
            data.setOnlineVersion(drStatusR.getOnlineVersion());
            slbDrStatusRMapper.updateByExampleSelective(data, new SlbDrStatusRExample().createCriteria().andDrIdEqualTo(dr.getId()).example());
        } else {
            data.setOnlineVersion(0);
            slbDrStatusRMapper.insert(data);
        }
        ArrayList<SlbDrVsR> drVsDos = new ArrayList<>();
        for (DrTraffic traffic : dr.getDrTraffics()) {
            for (Destination des : traffic.getDestinations()) {
                drVsDos.add(SlbDrVsR.builder().drId(dr.getId()).drVersion(dr.getVersion()).vsId(des.getVirtualServer().getId()).build());
            }
        }
        if(drVsDos.size()>0){
            slbDrVsRMapper.batchInsert(drVsDos);
        }

        // tagging
        List<String> existingTags = new ArrayList<>();
        for (String tag : tagService.getTags("dr", dr.getId())) {
            if (tag.startsWith(GROUPID_PREFIX) && !existingTags.contains(tag)) {
                existingTags.add(tag);
            }
        }

        for (DrTraffic traffic : dr.getDrTraffics()) {
            String tag = GROUPID_PREFIX + traffic.getGroup().getId();
            if (existingTags.contains(tag)) {
                existingTags.remove(tag);
                continue;
            }
            tagBox.tagging(tag, "dr", new Long[]{dr.getId()});
        }

        if (existingTags.size() > 0) {
            for (String tagName : existingTags) {
                tagBox.untagging(tagName, "dr", new Long[]{dr.getId()});
            }
        }
    }

    private void updateDr(Dr dr) throws Exception {
        SlbDr slbDr = SlbDr.builder().
                id(dr.getId()).
                name(dr.getName()).
                version(dr.getVersion() + 1).
                createdTime(dr.getCreatedTime() == null ? new Date() : dr.getCreatedTime()).build();

        slbDrMapper.updateByExample(slbDr, new SlbDrExample().createCriteria().andIdEqualTo(dr.getId()).example());
        dr.setId(slbDr.getId()).setVersion(slbDr.getVersion());
        maintainRelations(dr);
    }
}
