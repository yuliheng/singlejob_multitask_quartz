package com.youlu.server.task.controller;

import com.alibaba.fastjson.JSON;
import com.youlu.server.task.common.api.dto.LabelParams;
import com.youlu.server.task.common.api.dto.LabelQueryVO;
import com.youlu.server.task.common.api.dto.LabelTaskDTO;
import com.youlu.server.task.common.api.dto.TreeModelDTO;
import com.youlu.server.task.controller.vo.ResultVO;
import com.youlu.server.task.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/12 17:41
 */
@RestController
@RequestMapping("/autolabel")
public class LabelController {
    @Autowired
    private LabelService labelService;

    /**
     * 客户来源列表
     *
     * @return
     */
    @PostMapping("/custsourcetree")
    public ResultVO<?> findCustSourceTree() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findCustSourceTree();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 关注项目列表
     *
     * @return
     */
    @PostMapping("/projecttree")
    public ResultVO<?> findProjectTree() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findProjectTree();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 部门列表
     *
     * @return
     */
    @PostMapping("/dpttree")
    public ResultVO<?> findDptTree() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findDptTree();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }


    /**
     * 科目列表
     *
     * @return
     */
    @PostMapping("/subjecttree")
    public ResultVO<?> findSubjectTree() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findSubjectTree();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 订单来源
     *
     * @return
     */
    @PostMapping("/ordersource")
    public ResultVO<?> findOrderSourceTree() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findOrderSourceTree();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 班级列表
     *
     * @return
     */
    @PostMapping("/classtree")
    public ResultVO<?> findClassTree(@RequestParam("params") String params) throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            Map<String,String> className = JSON.parseObject(params, Map.class);
            String classKeyword = className.get("classKeyword");

            custSourceTree = labelService.findClassTree(classKeyword);
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 班型列表
     *
     * @return
     */
    @PostMapping("/classtypetree")
    public ResultVO<?> findClassTypeTree(@RequestParam("params") String params) throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            Map<String,String> className = JSON.parseObject(params, Map.class);
            String classKeyword = className.get("classKeyword");
            custSourceTree = labelService.findClassTypeTree(classKeyword);
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 订单类型列表
     *
     * @return
     */
    @PostMapping("/ordertypetree")
    public ResultVO<?> findOrderTypeTree() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findOrderTypeTree();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }

    /**
     * 沟通意向
     *
     * @return
     */
    @PostMapping("/cominten")
    public ResultVO<?> findCominten() throws Exception {
        List<TreeModelDTO> custSourceTree = null;
        try {
            custSourceTree = labelService.findCominten();
            return ResultVO.OK(custSourceTree, "成功");
        } catch (Exception e) {
            throw new Exception("查询失败");
        }
    }


    /**
     * 创建标签及规则
     *
     * @param labelTaskDTO
     * @return
     */
    @PostMapping("/labeltaskconfig")
    public ResultVO<?> createLabelTaskConfig(@RequestParam("params") String params) throws Exception {
        LabelTaskDTO labelTaskDTO = JSON.parseObject(params, LabelTaskDTO.class);
        try {
            labelService.createLabelTaskConfig(labelTaskDTO);
            return ResultVO.SUCCESS("创建成功");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 标签信息列表
     *
     * @return
     */
    @PostMapping("/labellist")
    public ResultVO<?> findLabelInfoList(@RequestParam("params") String params) throws Exception {
        try {
            LabelQueryVO labelQueryVO = JSON.parseObject(params, LabelQueryVO.class);
            return ResultVO.OK(labelService.findLabelInfoList(labelQueryVO), "查询成功");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 标签删除
     *
     * @param labelId
     * @return
     */
    @PostMapping("/labelremove")
    public ResultVO<?> deleteLabel(@RequestParam("params") String params) throws Exception {
        try {
            LabelParams labelParams = JSON.parseObject(params, LabelParams.class);
            labelService.deleteLabelInfoById(labelParams.getLabelId(), labelParams.getCustmkLabelId());
            return ResultVO.OK("", "成功");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 立即执行
     *
     * @param labelId
     * @return
     */
    @PostMapping("/taskexecute")
    public ResultVO<?> taskExecute(@RequestParam("params") String params) throws Exception {
        try {
            LabelParams labelParams = JSON.parseObject(params, LabelParams.class);
            System.out.println("--------------labelParams:"+labelParams);
            labelService.executeLabel(labelParams.getLabelId());
            return ResultVO.OK("", "成功");
        } catch (Exception e) {
//            e.printStackTrace();
//            log.info("查询失败", e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 暂停标签任务
     *
     * @param labelId
     * @return
     */
    @PostMapping("/taskpause")
    public ResultVO<?> pauseLabelTask(@RequestParam("params") String params) throws Exception {
        try {
            LabelParams labelParams = JSON.parseObject(params, LabelParams.class);
            labelService.pauseLabelTaskById(labelParams.getLabelId());
            return ResultVO.OK("", "成功");
        } catch (Exception e) {
//            e.printStackTrace();
//            log.info("查询失败", e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 恢复标签任务
     *
     * @param labelId
     * @return
     */
    @PostMapping("/taskrecover")
    public ResultVO<?> recoverLabelTask(@RequestParam("params") String params) throws Exception {
        try {
            LabelParams labelParams = JSON.parseObject(params, LabelParams.class);
            labelService.recoverLabelTaskById(labelParams.getLabelId());
            return ResultVO.OK("", "成功");
        } catch (Exception e) {
//            e.printStackTrace();
//            log.info("查询失败", e);
            throw new Exception(e.getMessage());
        }
    }


    @PostMapping("/test")
    public ResultVO<?> test(@RequestParam("params") String params) throws Exception {
        try {
            Map labelParams = JSON.parseObject(params, Map.class);
            System.out.println(labelParams);
//            labelService.test();
            return ResultVO.OK("", "成功");
        } catch (Exception e) {
//            e.printStackTrace();
//            log.info("查询失败", e);
            throw new Exception(e.getMessage());
        }
    }
}
