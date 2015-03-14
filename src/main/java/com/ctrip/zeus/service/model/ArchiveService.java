package com.ctrip.zeus.service.model;

import com.ctrip.zeus.dal.core.ArchiveAppDo;
import com.ctrip.zeus.dal.core.ArchiveSlbDo;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {
    public int archiveSlb(String name, String content) throws DalException;
    public int archiveApp(String name, String content) throws DalException;

    public String getSlb(String name, int version) throws DalException;
    public String getApp(String name, int version) throws DalException;

    public ArchiveSlbDo getMaxVersionSlb(String name) throws DalException;
    public ArchiveAppDo getMaxVersionApp(String name) throws DalException;

    public List<ArchiveSlbDo> getAllSlb(String name) throws DalException;
    public List<ArchiveAppDo> getAllApp(String name) throws DalException;

}
