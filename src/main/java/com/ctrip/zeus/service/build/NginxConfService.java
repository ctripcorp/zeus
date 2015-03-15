package com.ctrip.zeus.service.build;

import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface NginxConfService {
    public void build(String slbName, int version) throws DalException, IOException, SAXException;
}
