package com.ctrip.zeus.service.tagging;

import com.ctrip.zeus.model.model.Group;

/**
 * A service used to centralize all the automatic property and tag manipulation operations.
 *
 * @author dongyq
 */
public interface EntityTaggingService {

    void tagGroupAdvancedFeatures(Long groupId) throws Exception;

    void tagGroupAdvancedFeatures(Group group)throws Exception;
}
