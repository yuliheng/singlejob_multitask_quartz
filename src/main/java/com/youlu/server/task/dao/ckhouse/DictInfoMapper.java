package com.youlu.server.task.dao.ckhouse;

import com.youlu.server.task.dao.ckhouse.entity.DictInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/11/29
 */
@Mapper
public interface DictInfoMapper {

    DictInfoDO getById(@Param("id") String id,
                       @Param("type") String type);

    DictInfoDO getByCode(@Param("code") String code,
                         @Param("type") String type);

    /**
     * @Description 查询字典表中某类数据，已排序
     * @author yangyang.duan
     * @date 2021/1/16
     */
    List<DictInfoDO> listByType(@Param("type") String type);

    /**
     * @Description 查询某类数据中某父Code下的所有子数据，已排序
     * @author yangyang.duan
     * @date 2021/1/16
     */
    List<DictInfoDO> listByParentCode(@Param("parentCode") String parentCode,
                                      @Param("type") String type);

    List<DictInfoDO> listByCodes(@Param("codes") List<String> codes,
                                 @Param("type") String type);

    List<String> distinctProjectName();

    List<DictInfoDO> getProjectInfoByCollegeName(@Param("collegeName") String collegeName);
}
