package com.youlu.server.task.dao;

import com.youlu.server.task.Application;
import com.youlu.server.task.dao.ckhouse.BuMappingMapper;
import com.youlu.server.task.dao.ckhouse.entity.BuMappingDO;
import com.youlu.server.task.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/7
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BuMappingMapperTest {

    @Autowired
    private BuMappingMapper buMappingMapper;

    @Test
    public void testOptimize(){
        buMappingMapper.optimize();
    }

    @Test
    public void testInsertList(){
        List<BuMappingDO> doList = new ArrayList<>();
        BuMappingDO buMappingDO = new BuMappingDO();
        buMappingDO.setBuName("testBu");
        buMappingDO.setProjectName("testProject");
        buMappingDO.setOrderBusinessType("O");
        buMappingDO.setSystemType(1);
        doList.add(buMappingDO);
        Long aLong = buMappingMapper.insertList(doList);
        System.out.println(aLong);
    }

}
