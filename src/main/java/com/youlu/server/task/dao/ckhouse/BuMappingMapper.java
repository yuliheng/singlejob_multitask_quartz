package com.youlu.server.task.dao.ckhouse;

import com.youlu.server.task.dao.ckhouse.entity.BuMappingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/1/12
 */
@Mapper
public interface BuMappingMapper {

    Long insertList(@Param("list") List<BuMappingDO> list);

    void optimize();
}
