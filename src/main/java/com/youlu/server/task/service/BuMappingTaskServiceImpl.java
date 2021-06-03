package com.youlu.server.task.service;

import com.alibaba.druid.util.StringUtils;
import com.youlu.server.task.dao.ckhouse.BuMappingMapper;
import com.youlu.server.task.dao.ckhouse.DictInfoMapper;
import com.youlu.server.task.dao.ckhouse.ProjectTotalMapper;
import com.youlu.server.task.dao.ckhouse.entity.BuMappingDO;
import com.youlu.server.task.dao.ckhouse.entity.DictInfoDO;
import com.youlu.server.task.entity.ConstantValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/4/17
 */
@Component
public class BuMappingTaskServiceImpl {

    @Autowired
    private BuMappingMapper buMappingMapper;
    @Autowired
    private ProjectTotalMapper projectTotalMapper;
    @Autowired
    private DictInfoMapper dictInfoMapper;

    private Map<String, List<BuMapping>> buMappingMap = new HashMap<>();


    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updateBuMappingContent(){

        //orderBusinessType 列表
        List<String> orderBusinessTypeList = projectTotalMapper.distinctOrderBusinessType();
        //systemType 列表
        List<Integer> systemTypeList = projectTotalMapper.distinctSystemType();
        //projectName 列表
        List<String> projectNameList = dictInfoMapper.distinctProjectName();

        List<DictInfoDO> dictInfoDOS = dictInfoMapper.listByType(ConstantValue.DICT_TYPE_PROJECT);
        Map<String, String> projectInfoMap = dictInfoDOS.stream().collect(Collectors.toMap(DictInfoDO::getId, DictInfoDO::getName));

        //读取配置，解析配置, 转化为DOS
        Set<BuMappingDO> DOS = new HashSet<>();
        buMappingMap.forEach((buName,buMappingList) -> {
            buMappingList.forEach(buMapping -> {
                List<String> orderBusinessTypes = buMapping.getOrderBusinessType();
                if(orderBusinessTypes == null || orderBusinessTypes.isEmpty()){
                    orderBusinessTypes = orderBusinessTypeList;
                }
                List<Integer> systemTypes = buMapping.getSystemType();
                if(systemTypes == null || systemTypes.isEmpty()){
                    systemTypes = systemTypeList;
                }
                String collegeId = buMapping.getCollegeId();

                List<String> projectNames;
                if(StringUtils.isEmpty(collegeId)){
                    projectNames = projectNameList;
                }else {
                    String collegeName = projectInfoMap.get(collegeId);
                    List<DictInfoDO> projectInfos = dictInfoMapper.getProjectInfoByCollegeName(collegeName);
                    projectNames = projectInfos.stream().map(DictInfoDO::getName).collect(Collectors.toList());
                }
                List<Integer> finalSystemTypes = systemTypes;
                List<String> finalOrderBusinessTypes = orderBusinessTypes;
                projectNames.forEach(projectName ->{
                    finalSystemTypes.forEach(systemType ->{
                        finalOrderBusinessTypes.forEach(orderBusinessType ->{
                            BuMappingDO buMappingDO = new BuMappingDO();
                            buMappingDO.setBuName(buName);
                            buMappingDO.setSystemType(systemType);
                            buMappingDO.setOrderBusinessType(orderBusinessType);
                            buMappingDO.setProjectName(projectName);
                            DOS.add(buMappingDO);
                        });
                    });
                });
            });
        });

        //插入ClickHouse ods.bu_mapping
        buMappingMapper.insertList(new ArrayList<>(DOS));

        //ods.bu_mapping去重
        buMappingMapper.optimize();
    }


    //初始化，加载配置
    {
        //竞价四六级考研部
        buMappingMap.put("竞价四六级考研部", Arrays.asList(new BuMapping().withCollegeId("PROJECT20201218020000000001").withSystemType(Arrays.asList(0)))); //四六级考研-*-订单中心

        //市场法考事业部
        List<BuMapping> buMappingList = new ArrayList<>();
        buMappingList.add(new BuMapping().withCollegeId("PROJECT20201207010000000001").withSystemType(Arrays.asList(0)));   //法律考试-*-订单中心
        buMappingList.add(new BuMapping().withSystemType(Arrays.asList(15)));  //*-*-企业客户系统
        buMappingList.add(new BuMapping().withSystemType(Arrays.asList(14)));  //*-*-合作商系统
        buMappingMap.put("市场法考事业部", buMappingList);

        //财经事业部
        List<BuMapping> buMappingList1 = new ArrayList<>();
        buMappingList1.add(new BuMapping().withCollegeId("PROJECT20191126010000000324").withSystemType(Arrays.asList(0)));   //财税金融-*-订单中心
        buMappingList1.add(new BuMapping().withSystemType(Arrays.asList(5)));  //*-*-锅巴网
        buMappingMap.put("财经事业部", buMappingList1);

        //品牌教师部
        List<BuMapping> buMappingList2 = new ArrayList<>();
        buMappingList2.add(new BuMapping().withCollegeId("PROJECT20191126010000000333").withSystemType(Arrays.asList(0)));   //教资招教-*-订单中心
        buMappingMap.put("品牌教师部", buMappingList2);

        //医卫事业部
        List<BuMapping> buMappingList3 = new ArrayList<>();
        buMappingList3.add(new BuMapping().withCollegeId("PROJECT20191126010000000338").withSystemType(Arrays.asList(0)));   //医药卫生-*-订单中心
        buMappingMap.put("医卫事业部", buMappingList3);

        //信管公考部
        List<BuMapping> buMappingList4 = new ArrayList<>();
        buMappingList4.add(new BuMapping().withCollegeId("PROJECT20201221010000000001").withSystemType(Arrays.asList(0)));   //公务员考试-*-订单中心
        buMappingMap.put("信管公考部", buMappingList4);

        //综合事业部
        List<BuMapping> buMappingList5 = new ArrayList<>();
        buMappingList5.add(new BuMapping().withCollegeId("PROJECT20191126010000000416").withSystemType(Arrays.asList(0)));   //建筑工程-*-订单中心
        buMappingList5.add(new BuMapping().withCollegeId("PROJECT20200519010000000001").withSystemType(Arrays.asList(0)));   //消防安全-*-订单中心
        buMappingList5.add(new BuMapping().withCollegeId("PROJECT20210108020000000001").withSystemType(Arrays.asList(0)));   //经济管理-*-订单中心
        buMappingList5.add(new BuMapping().withCollegeId("PROJECT20210108020000000002").withSystemType(Arrays.asList(0)));   //康养技能-*-订单中心
        buMappingMap.put("综合事业部", buMappingList5);
    }

    class BuMapping {
        private String collegeId;
        private List<String> orderBusinessType;
        private List<Integer> systemType;

        public String getCollegeId() {
            return collegeId;
        }

        public void setCollegeId(String collegeId) {
            this.collegeId = collegeId;
        }

        public BuMapping withCollegeId(String collegeId){
            setCollegeId(collegeId);
            return this;
        }

        public List<String> getOrderBusinessType() {
            return orderBusinessType;
        }

        public void setOrderBusinessType(List<String> orderBusinessType) {
            this.orderBusinessType = orderBusinessType;
        }

        public BuMapping withOrderBusinessType(List<String> orderBusinessType){
            setOrderBusinessType(orderBusinessType);
            return this;
        }

        public List<Integer> getSystemType() {
            return systemType;
        }

        public void setSystemType(List<Integer> systemType) {
            this.systemType = systemType;
        }

        public BuMapping withSystemType(List<Integer> systemType){
            setSystemType(systemType);
            return this;
        }

    }
}
