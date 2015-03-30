package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {
    public int archiveSlb(Slb slb) throws DalException;
    public int archiveApp(App app) throws DalException;

    public int deleteSlbArchive(String slbName) throws DalException;
    public int deleteAppArchive(String appName) throws DalException;

    public Slb getSlb(String name, int version) throws DalException, IOException, SAXException;
    public App getApp(String name, int version) throws DalException, IOException, SAXException;

    public Slb getMaxVersionSlb(String name) throws DalException, IOException, SAXException;
    public App getMaxVersionApp(String name) throws DalException, IOException, SAXException;

    public List<Slb> getAllSlb(String name) throws DalException, IOException, SAXException;
    public List<App> getAllApp(String name) throws DalException, IOException, SAXException;

}
