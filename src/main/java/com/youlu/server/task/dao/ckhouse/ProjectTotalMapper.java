package com.youlu.server.task.dao.ckhouse;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/1/12
 */
@Mapper
public interface ProjectTotalMapper {

    List<String> distinctOrderBusinessType();

    List<Integer> distinctSystemType();

}
