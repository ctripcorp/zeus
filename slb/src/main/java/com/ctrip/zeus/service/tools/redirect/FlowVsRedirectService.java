package com.ctrip.zeus.service.tools.redirect;

import com.ctrip.zeus.model.tools.VsRedirect;
import com.ctrip.zeus.model.tools.VsRedirectList;

public interface FlowVsRedirectService {
    VsRedirect add(VsRedirect redirect) throws Exception;

    VsRedirect update(VsRedirect redirect) throws Exception;

    boolean delete(Long id) throws Exception;

    VsRedirect get(Long id) throws Exception;

    VsRedirectList list() throws Exception;
}
