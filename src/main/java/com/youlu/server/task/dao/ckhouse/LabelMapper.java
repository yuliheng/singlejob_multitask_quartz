package com.youlu.server.task.dao.ckhouse;

import com.youlu.server.task.dao.ckhouse.entity.DictInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/13 17:05
 */
@Mapper
public interface LabelMapper {




    List<String> queryLabelCustId(@Param("executeSql") String executeSql);


    List<DictInfoDO> findDictInfoByType(@Param("types") List<String> types);

    List<DictInfoDO> findClassByKeyword(@Param("types") List<String> types,@Param("classKeyword") String classKeyword);
}
