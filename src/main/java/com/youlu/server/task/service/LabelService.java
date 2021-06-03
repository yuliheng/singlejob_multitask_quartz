package com.youlu.server.task.service;

import com.youlu.server.task.common.api.dto.LabelQueryVO;
import com.youlu.server.task.common.api.dto.LabelTaskDTO;
import com.youlu.server.task.common.api.dto.TreeModelDTO;
import com.youlu.server.task.entity.ListResult;

import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/12 17:44
 */
public interface LabelService {

    List<TreeModelDTO> findCustSourceTree();

    List<TreeModelDTO> findProjectTree();

    List<TreeModelDTO> findDptTree();

    List<TreeModelDTO> findSubjectTree();

    List<TreeModelDTO> findOrderSourceTree();

    List<TreeModelDTO> findClassTree(String classKeyword);

    List<TreeModelDTO> findClassTypeTree(String classKeyword);

    List<TreeModelDTO> findOrderTypeTree();

    void createLabelTaskConfig(LabelTaskDTO labelTaskDTO) throws Exception;

    ListResult<?> findLabelInfoList(LabelQueryVO labelQueryVO) throws Exception;

    void deleteLabelInfoById(String labelId, String custmkLabelId) throws Exception;

    void pauseLabelTaskById(String labelId) throws Exception;

    void recoverLabelTaskById(String labelId) throws Exception;

    void executeLabel(String labelId) throws Exception;

    List<TreeModelDTO> findCominten();


    void test() throws Exception;
}
