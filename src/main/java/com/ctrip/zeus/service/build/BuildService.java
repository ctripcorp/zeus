package com.ctrip.zeus.service.build;

import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildService {
    void build(String name) throws DalException, IOException, SAXException;
}
