package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.CertCertificateSlbServerR;
import com.ctrip.zeus.dao.entity.CertCertificateSlbServerRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CertCertificateSlbServerRMapper {
    long countByExample(CertCertificateSlbServerRExample example);

    int deleteByExample(CertCertificateSlbServerRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CertCertificateSlbServerR record);

    int insertSelective(CertCertificateSlbServerR record);

    CertCertificateSlbServerR selectOneByExample(CertCertificateSlbServerRExample example);

    CertCertificateSlbServerR selectOneByExampleSelective(@Param("example") CertCertificateSlbServerRExample example, @Param("selective") CertCertificateSlbServerR.Column ... selective);

    List<CertCertificateSlbServerR> selectByExampleSelective(@Param("example") CertCertificateSlbServerRExample example, @Param("selective") CertCertificateSlbServerR.Column ... selective);

    List<CertCertificateSlbServerR> selectByExample(CertCertificateSlbServerRExample example);

    CertCertificateSlbServerR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") CertCertificateSlbServerR.Column ... selective);

    CertCertificateSlbServerR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CertCertificateSlbServerR record, @Param("example") CertCertificateSlbServerRExample example);

    int updateByExample(@Param("record") CertCertificateSlbServerR record, @Param("example") CertCertificateSlbServerRExample example);

    int updateByPrimaryKeySelective(CertCertificateSlbServerR record);

    int updateByPrimaryKey(CertCertificateSlbServerR record);

    int upsert(CertCertificateSlbServerR record);

    int upsertSelective(CertCertificateSlbServerR record);

    /* Method added manually */
    int insertCertSlbServerOrUpdateCert(CertCertificateSlbServerR record);

    int batchInsertIdIncluded(List<CertCertificateSlbServerR> records);
    /* Method added manually */
}