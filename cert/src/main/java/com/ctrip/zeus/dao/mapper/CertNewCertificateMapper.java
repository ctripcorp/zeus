package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.CertNewCertificate;
import com.ctrip.zeus.dao.entity.CertNewCertificateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CertNewCertificateMapper {
    long countByExample(CertNewCertificateExample example);

    int deleteByExample(CertNewCertificateExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CertNewCertificate record);

    int insertSelective(CertNewCertificate record);

    CertNewCertificate selectOneByExample(CertNewCertificateExample example);

    CertNewCertificate selectOneByExampleSelective(@Param("example") CertNewCertificateExample example, @Param("selective") CertNewCertificate.Column ... selective);

    CertNewCertificate selectOneByExampleWithBLOBs(CertNewCertificateExample example);

    List<CertNewCertificate> selectByExampleSelective(@Param("example") CertNewCertificateExample example, @Param("selective") CertNewCertificate.Column ... selective);

    List<CertNewCertificate> selectByExampleWithBLOBs(CertNewCertificateExample example);

    List<CertNewCertificate> selectByExample(CertNewCertificateExample example);

    CertNewCertificate selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") CertNewCertificate.Column ... selective);

    CertNewCertificate selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CertNewCertificate record, @Param("example") CertNewCertificateExample example);

    int updateByExampleWithBLOBs(@Param("record") CertNewCertificate record, @Param("example") CertNewCertificateExample example);

    int updateByExample(@Param("record") CertNewCertificate record, @Param("example") CertNewCertificateExample example);

    int updateByPrimaryKeySelective(CertNewCertificate record);

    int updateByPrimaryKeyWithBLOBs(CertNewCertificate record);

    int updateByPrimaryKey(CertNewCertificate record);

    int upsert(CertNewCertificate record);

    int upsertSelective(CertNewCertificate record);

    int upsertWithBLOBs(CertNewCertificate record);

    /* manually added */
    int insertWithId(CertNewCertificate record);
    /* manually added */
}