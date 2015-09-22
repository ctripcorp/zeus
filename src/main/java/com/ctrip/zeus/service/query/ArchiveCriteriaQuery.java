package com.ctrip.zeus.service.query;

/**
 * Created by zhoumy on 2015/9/22.
 */
public interface ArchiveCriteriaQuery {

    Long[] queryLastestArchives(Long[] targetIds) throws Exception;
}
