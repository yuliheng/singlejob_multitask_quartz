package com.youlu.server.task.dao;

import com.youlu.server.task.Application;
import com.youlu.server.task.service.BuMappingTaskServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/7
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BuMappingTaskServiceTest {

    @Autowired
    private BuMappingTaskServiceImpl buMappingTaskServiceImpl;

    @Test
    public void testUpdateBuMappingContent(){

        buMappingTaskServiceImpl.updateBuMappingContent();
    }

}
