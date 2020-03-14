package com.ctrip.zeus.service.nginx.handler;

import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.service.nginx.impl.FileOpRecord;

import java.util.Set;

/**
 * Created by fanqq on 2016/3/14.
 */
public interface NginxConfOpsService {
    /**
     * write all to dist ;
     * if throw NginxProcessingException, config files on disk is incorrect.
     * else if throw other exception. config files are rolled back to the previous version
     *
     * @return Long
     * @throws Exception
     */
    Long updateAll(NginxConfEntry entry) throws Exception;

    /**
     * undo write all to dist ;
     * if throw NginxProcessingException, config files on disk is incorrect.
     *
     * @throws Exception
     */
    void undoUpdateAll(Long flag) throws Exception;

    /**
     * update nginx conf  ;
     * if throw NginxProcessingException, config files on disk is incorrect.
     * else if throw other exception. config files are rolled back to the previous version
     *
     * @throws Exception
     */
    void updateNginxConf(String nginxConf) throws Exception;

    /**
     * undo update nginx conf  ;
     * if throw NginxProcessingException, config files on disk is incorrect.
     *
     * @return void.
     * @throws Exception
     */
    void undoUpdateNginxConf() throws Exception;

    /**
     * update nginx conf  ;
     * if throw NginxProcessingException, config files on disk is incorrect.
     * else if throw other exception. config files are rolled back to the previous version
     *
     * @return Long time flag
     * @throws Exception
     */
    FileOpRecord cleanAndUpdateConf(Set<Long> cleanVsIds, Set<Long> updateVsIds, NginxConfEntry entry) throws Exception;

    /**
     * undo clean and update nginx conf  ;
     * if throw NginxProcessingException, config files on disk is incorrect.
     *
     * @return Long time flag
     * @throws Exception
     */
    void undoCleanAndUpdateConf(Set<Long> cleanVsIds, NginxConfEntry entry, FileOpRecord record) throws Exception;

    void cleanRollbackFiles();
}
