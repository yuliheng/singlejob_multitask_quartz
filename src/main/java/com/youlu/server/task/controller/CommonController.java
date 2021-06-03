package com.youlu.server.task.controller;

import com.youlu.server.task.dao.ckhouse.DictInfoMapper;
import com.youlu.server.task.dao.ckhouse.entity.DictInfoDO;
import com.youlu.server.task.entity.ConstantValue;
import com.youlu.server.task.controller.vo.ResultVO;
import com.youlu.server.task.util.ResultVOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/4/17
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private DictInfoMapper dictInfoMapper;

    @GetMapping("/area_select")
    public ResultVO<List<DictInfoDO>> selectArea(){
        log.info("selectArea be called !");
        List<DictInfoDO> dictInfoDOS = dictInfoMapper.listByType(ConstantValue.DICT_TYPE_AREA);
        return ResultVOUtil.success(dictInfoDOS);
    }
}







