package com.youlu.server.task.controller;

import com.youlu.server.task.service.BuMappingTaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/4/17
 */
@Slf4j
@RestController
@RequestMapping("/task/trigger")
public class TaskTriggerController {

    @Autowired
    private BuMappingTaskServiceImpl buMappingTaskService;

    @GetMapping("/update/buMapping")
    public void buMappingUpdateTask(){
        log.info("trigger buMappingUpdateTask success !");
        buMappingTaskService.updateBuMappingContent();
        log.info("buMappingUpdateTask execute end !");
    }
}
