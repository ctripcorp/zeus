package com.ctrip.zeus.util;

import com.ctrip.zeus.dao.entity.CertCertificateExample;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.dao.mapper.CertCertificateMapper;
import com.ctrip.zeus.service.CertificateResourceService;
import com.ctrip.zeus.service.build.ConfigHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @Discription
 **/
@Component
public class CertSyncTool {

    @Resource
    private CertCertificateMapper certCertificateMapper;
    @Resource
    private CertificateResourceService certificateResourceService; // certificate service of cert module
    @Resource
    private CertRefactorControls certRefactorControls;
    @Resource
    private ConfigHandler configHandler;


    private Logger logger = LoggerFactory.getLogger(CertSyncTool.class);

    /*
     * @Description: sync records from old cert table to new cert table
     * add cert's domains as its tag and cid as its property
     * @return: {sync: count; update: count}
     **/
    public long sync(Integer batchSize) throws Exception {
        if (certRefactorControls.writeToNewTable() || !certRefactorControls.writeToOldTable()) {
            // sync should only be done before write-new-table switch has been turned on or
            // write-old-table switch has been turned off.
            logger.warn("Cert sync should only be done before write-new-table switch is turned on. ");
            return 0;
        }

        long syncCount = 0;
        int start = 0;
        if (batchSize == null) {
            batchSize = configHandler.getIntValue("refactor.cert.sync.batch.size", 100);
        }
        List<CertCertificateWithBLOBs> toSyncRecords;
        do {
            toSyncRecords = certCertificateMapper.selectByExampleWithBLOBs(
                    new CertCertificateExample().limit(start, batchSize).orderBy("id"));
            if (toSyncRecords.size() > 0) {
                for (CertCertificateWithBLOBs record : toSyncRecords) {
                    // skip sync if record already exist in new cert table
                    if (certificateResourceService.get(record.getId(), false) != null) {
                        continue;
                    }
                    String domain = record.getDomain();
                    String[] domains = CertUtil.splitDomain(domain);

                    certificateResourceService.sync(new ByteArrayInputStream(record.getCert()), new ByteArrayInputStream(record.getKey()), Arrays.asList(domains), record.getCid(), record.getId());
                    syncCount++;
                }
                start += toSyncRecords.size();
            }
        } while (toSyncRecords.size() > 0);

        return syncCount;
    }
}
