package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.model.Dr;
import com.ctrip.zeus.service.Repository;

import java.util.List;

public interface DrRepository extends Repository {
    List<Dr> list() throws Exception;

    List<Dr> list(IdVersion[] key) throws Exception;

    Dr getById(Long drId) throws Exception;

    Dr getByKey(IdVersion key) throws Exception;

    void delete(Long drId) throws Exception;

    Dr add(Dr dr) throws Exception;

    Dr update(Dr dr) throws Exception;

    void updateActiveStatus(IdVersion[] drs) throws Exception;
}
