package com.youlu.server.task.dao;

import com.youlu.server.task.Application;
import com.youlu.server.task.dao.ckhouse.ProjectTotalMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/7
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("uat")
public class ProjectTotalMapperTest {

    @Autowired
    private ProjectTotalMapper projectTotalMapper;

    @Test
    public void testDistinctOrderBusinessType(){
        List<String> strings = projectTotalMapper.distinctOrderBusinessType();
//        String[] strings = projectTotalMapper.distinctOrderBusinessType();
        System.out.println(strings);
    }

}
