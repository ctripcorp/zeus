package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.CertCertificateVsR;
import com.ctrip.zeus.dao.entity.CertCertificateVsRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CertCertificateVsRMapper {
    long countByExample(CertCertificateVsRExample example);

    int deleteByExample(CertCertificateVsRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CertCertificateVsR record);

    int insertSelective(CertCertificateVsR record);

    CertCertificateVsR selectOneByExample(CertCertificateVsRExample example);

    CertCertificateVsR selectOneByExampleSelective(@Param("example") CertCertificateVsRExample example, @Param("selective") CertCertificateVsR.Column ... selective);

    List<CertCertificateVsR> selectByExampleSelective(@Param("example") CertCertificateVsRExample example, @Param("selective") CertCertificateVsR.Column ... selective);

    List<CertCertificateVsR> selectByExample(CertCertificateVsRExample example);

    CertCertificateVsR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") CertCertificateVsR.Column ... selective);

    CertCertificateVsR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CertCertificateVsR record, @Param("example") CertCertificateVsRExample example);

    int updateByExample(@Param("record") CertCertificateVsR record, @Param("example") CertCertificateVsRExample example);

    int updateByPrimaryKeySelective(CertCertificateVsR record);

    int updateByPrimaryKey(CertCertificateVsR record);

    int upsert(CertCertificateVsR record);

    int upsertSelective(CertCertificateVsR record);

    // Method added manually start
    int insertIdIncluded(CertCertificateVsR record);

    int batchInsertIdIncluded(List<CertCertificateVsR> records);

    int batchInsert(List<CertCertificateVsR> records);
    // Method added manually end
}