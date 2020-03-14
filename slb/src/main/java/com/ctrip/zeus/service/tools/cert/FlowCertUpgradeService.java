package com.ctrip.zeus.service.tools.cert;


import com.ctrip.zeus.model.tools.CertUpgrade;

import java.util.List;

public interface FlowCertUpgradeService {
    CertUpgrade create(CertUpgrade certUpgrade) throws Exception;

    CertUpgrade get(Long id) throws Exception;

    CertUpgrade update(CertUpgrade certUpgrade) throws Exception;

    List<CertUpgrade> list() throws Exception;
}
