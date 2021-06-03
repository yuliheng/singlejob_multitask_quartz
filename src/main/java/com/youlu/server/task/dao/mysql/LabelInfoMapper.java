package com.youlu.server.task.dao.mysql;

import com.youlu.server.task.common.api.dto.LabelQueryVO;
import com.youlu.server.task.dao.mysql.entity.LabelFieldMappingDO;
import com.youlu.server.task.dao.mysql.entity.LabelInfoDO;
import com.youlu.server.task.dao.mysql.entity.LabelJobInfo;
import com.youlu.server.task.dao.mysql.entity.LabelJobSheduleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/15 10:03
 */
@Mapper
public interface LabelInfoMapper {
    Long saveLabelInfo(@Param("labelInfoDO")LabelInfoDO labelInfoDO);

    Long findJobScheduleByJobName(@Param("jobName") String jobName);

    List<LabelFieldMappingDO> getFieldMappingByReportName(@Param("reportName") String reportName);

    Long saveLabelJobSchedule(@Param("labelJobSheduleDO") LabelJobSheduleDO labelJobSheduleDO);

    void deleteLabelInfoById(@Param("labelId") Long labelId);

    void removeJobScheduleByLabelId(@Param("labelId") Long labelId);

    LabelJobSheduleDO findJobScheduleByLabelId(@Param("labelId") Long labelId);

    void updateLabelJobScheduleById(@Param("labelJobSheduleDO") LabelJobSheduleDO labelJobSheduleDO);

    void updateLabelJobScheduleStatusById(@Param("labelId") Long labelId,@Param("status") Integer status);

//    LabelJobInfo findLabelJobInfo(@Param("custmkCustId") String custmkCustId);

    void updateLabelJobSchedule(@Param("labelJobSheduleDO") LabelJobSheduleDO labelJobSheduleDO);

    LabelInfoDO findLabelInfoByLabelId(@Param("labelId") Long labelId);

    void updateLabelJobScheduleJobProcessStatusById(@Param("labelId") Long labelId,@Param("jobProcessStatus") Integer jobProcessStatus);

    List<LabelJobInfo> findLabelJobInfo(@Param("labelQueryVO") LabelQueryVO labelQueryVO);

    Long findLabelJobInfoTotal(@Param("labelQueryVO") LabelQueryVO labelQueryVO);
}
