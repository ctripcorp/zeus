package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Archive;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {

    Archive getSlbArchive(Long slbId, int version) throws Exception;

    Archive getGroupArchive(Long groupId, int version) throws Exception;

    Archive getVsArchive(Long vsId, int version) throws Exception;
}
