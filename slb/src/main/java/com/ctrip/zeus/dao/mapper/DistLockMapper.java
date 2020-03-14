package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.DistLock;
import com.ctrip.zeus.dao.entity.DistLockExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DistLockMapper {
    /** Non-automatic generation Code Start **/
    DistLock forUpdate(@Param("lockKey")String lockKey);
    /** Non-automatic generation Code END **/

    long countByExample(DistLockExample example);

    int deleteByExample(DistLockExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DistLock record);

    int insertSelective(DistLock record);

    DistLock selectOneByExample(DistLockExample example);

    DistLock selectOneByExampleSelective(@Param("example") DistLockExample example, @Param("selective") DistLock.Column ... selective);

    List<DistLock> selectByExampleSelective(@Param("example") DistLockExample example, @Param("selective") DistLock.Column ... selective);

    List<DistLock> selectByExample(DistLockExample example);

    DistLock selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") DistLock.Column ... selective);

    DistLock selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DistLock record, @Param("example") DistLockExample example);

    int updateByExample(@Param("record") DistLock record, @Param("example") DistLockExample example);

    int updateByPrimaryKeySelective(DistLock record);

    int updateByPrimaryKey(DistLock record);

    int upsert(DistLock record);

    int upsertSelective(DistLock record);
}