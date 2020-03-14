package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.CertCertificate;
import com.ctrip.zeus.dao.entity.CertCertificateExample;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CertCertificateMapper {
    long countByExample(CertCertificateExample example);

    int deleteByExample(CertCertificateExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CertCertificateWithBLOBs record);

    int insertSelective(CertCertificateWithBLOBs record);

    CertCertificate selectOneByExample(CertCertificateExample example);

    CertCertificateWithBLOBs selectOneByExampleSelective(@Param("example") CertCertificateExample example, @Param("selective") CertCertificateWithBLOBs.Column ... selective);

    CertCertificateWithBLOBs selectOneByExampleWithBLOBs(CertCertificateExample example);

    List<CertCertificateWithBLOBs> selectByExampleSelective(@Param("example") CertCertificateExample example, @Param("selective") CertCertificateWithBLOBs.Column ... selective);

    List<CertCertificateWithBLOBs> selectByExampleWithBLOBs(CertCertificateExample example);

    List<CertCertificate> selectByExample(CertCertificateExample example);

    CertCertificateWithBLOBs selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") CertCertificateWithBLOBs.Column ... selective);

    CertCertificateWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CertCertificateWithBLOBs record, @Param("example") CertCertificateExample example);

    int updateByExampleWithBLOBs(@Param("record") CertCertificateWithBLOBs record, @Param("example") CertCertificateExample example);

    int updateByExample(@Param("record") CertCertificate record, @Param("example") CertCertificateExample example);

    int updateByPrimaryKeySelective(CertCertificateWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(CertCertificateWithBLOBs record);

    int updateByPrimaryKey(CertCertificate record);

    int upsert(CertCertificate record);

    int upsertSelective(CertCertificateWithBLOBs record);

    int upsertWithBLOBs(CertCertificateWithBLOBs record);

    /* not automatic generated */
    int updateCidByMd5(@Param("certMd5") String certMd5, @Param("cid") String cid);
    /* not automatic generated */

    /* not automatic generated */
    int insertIdIncluded(CertCertificateWithBLOBs record);

    int batchInsertIdIncluded(List<CertCertificateWithBLOBs> records);
    /* not automatic generated */
}