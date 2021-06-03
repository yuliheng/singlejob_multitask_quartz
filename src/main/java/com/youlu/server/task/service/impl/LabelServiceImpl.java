package com.youlu.server.task.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.youlu.server.task.common.api.dto.*;
import com.youlu.server.task.common.constant.HeaderEnum;
import com.youlu.server.task.common.quarts.job.LabelRulesJob;
import com.youlu.server.task.config.usermkconfig.UsermkLabelBatchAddConfig;
import com.youlu.server.task.config.usermkconfig.UsermkLabelCreateConfig;
import com.youlu.server.task.config.usermkconfig.UsermkLabelQueryConfig;
import com.youlu.server.task.config.usermkconfig.UsermkLabelRemoveConfig;
import com.youlu.server.task.dao.ckhouse.LabelMapper;
import com.youlu.server.task.dao.ckhouse.entity.DictInfoDO;
import com.youlu.server.task.dao.mysql.LabelInfoMapper;
import com.youlu.server.task.dao.mysql.entity.LabelFieldMappingDO;
import com.youlu.server.task.dao.mysql.entity.LabelInfoDO;
import com.youlu.server.task.dao.mysql.entity.LabelJobInfo;
import com.youlu.server.task.dao.mysql.entity.LabelJobSheduleDO;
import com.youlu.server.task.entity.ListResult;
import com.youlu.server.task.service.LabelService;
import com.youlu.server.task.util.DateUtil;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.clickhouse.util.apache.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/13 17:04
 */
@Service
public class LabelServiceImpl implements LabelService {

    private static ExecutorService executors = Executors.newFixedThreadPool(10);
    private static int batchSize = 5000; //每个线程每批处理数据量；

    private static Map<String, String> dictMap = new HashMap<>();

    static {
        dictMap.put("SA", "营销");
        dictMap.put("FI", "财务");
        dictMap.put("SE", "服务");
        dictMap.put("HR", "人力");
        dictMap.put("2", "已完成");
        dictMap.put("1", "处理中");
        dictMap.put("0", "未处理");
        dictMap.put("-1", "失败");
    }


    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private UsermkLabelBatchAddConfig usermkLabelBatchAddConfig;

    @Autowired
    private UsermkLabelCreateConfig usermkLabelCreateConfig;

    @Autowired
    private UsermkLabelQueryConfig usermkLabelQueryConfig;

    @Autowired
    private UsermkLabelRemoveConfig usermkLabelRemoveConfig;


    @Override
    public List<TreeModelDTO> findCustSourceTree() {
        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("NN", "T"));
        List<TreeModelDTO> treeModelDTOS = buildModeTree(dictInfos, "", 10);
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findProjectTree() {
        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("P"));
        List<TreeModelDTO> treeModelDTOS = buildModeTree(dictInfos, "", 10);
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findDptTree() {
//        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("A", "S", "D"));
        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("BdDpt"));

        List<TreeModelDTO> treeModelDTOS = buildModeTree(dictInfos, "", 10);
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findSubjectTree() {
        List<DictInfoDO> projectDictInfos = labelMapper.findDictInfoByType(Arrays.asList("P"));

        List<TreeModelDTO> projectTreeModelDTOS = buildModeTree(projectDictInfos, "", 10);
        List<DictInfoDO> subjectDictInfos = labelMapper.findDictInfoByType(Arrays.asList("SN"));

        projectTreeModelDTOS.stream().forEach(projectTreeModelDTO -> {
            List<TreeModelDTO> children = projectTreeModelDTO.getChildren();
            children.stream().forEach(child -> {
                String projectId = child.getId();
                child.setChildren(buildModeTree(subjectDictInfos, projectId));
            });
        });
        return projectTreeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findOrderSourceTree() {
        List<DictInfoDO> dictInfoDOS = labelMapper.findDictInfoByType(Arrays.asList("orderSource"));

        List<TreeModelDTO> treeModelDTOS = buildModeTree(dictInfoDOS, "");

//        List<TreeModelDTO> treeModelDTOS = dictInfoDOS.stream().map(dictInfo -> {
//                    TreeModelDTO treeModelDTO = new TreeModelDTO();
//                    treeModelDTO.setId(dictInfo.getId());
//                    treeModelDTO.setCode(dictInfo.getId());
//                    treeModelDTO.setValue(dictInfo.getName());
//                    return treeModelDTO;
//                }
//        ).collect(Collectors.toList());
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findClassTree(String classKeyword) {
//        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("BN"));
        List<DictInfoDO> dictInfos = labelMapper.findClassByKeyword(Arrays.asList("BN"), classKeyword);
        List<TreeModelDTO> treeModelDTOS = dictInfos.stream().map(dictInfo -> {
                    TreeModelDTO treeModelDTO = new TreeModelDTO();
                    treeModelDTO.setId(dictInfo.getId());
                    treeModelDTO.setCode(dictInfo.getId());
                    treeModelDTO.setValue(dictInfo.getName());
                    return treeModelDTO;
                }
        ).collect(Collectors.toList());
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findClassTypeTree(String classKeyword) {
//        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("BX"));
        List<DictInfoDO> dictInfos = labelMapper.findClassByKeyword(Arrays.asList("BX"), classKeyword);

        List<TreeModelDTO> treeModelDTOS = dictInfos.stream().map(dictInfo -> {
                    TreeModelDTO treeModelDTO = new TreeModelDTO();
                    treeModelDTO.setId(dictInfo.getId());
                    treeModelDTO.setCode(dictInfo.getId());
                    treeModelDTO.setValue(dictInfo.getName());
                    return treeModelDTO;
                }
        ).collect(Collectors.toList());
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findOrderTypeTree() {
        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("L"));
        List<TreeModelDTO> treeModelDTOS = dictInfos.stream().map(dictInfo -> {
                    TreeModelDTO treeModelDTO = new TreeModelDTO();
                    treeModelDTO.setId(dictInfo.getId());
                    treeModelDTO.setCode(dictInfo.getId());
                    treeModelDTO.setValue(dictInfo.getName());
                    return treeModelDTO;
                }
        ).collect(Collectors.toList());
        return treeModelDTOS;
    }

    @Override
    public List<TreeModelDTO> findCominten() {
        List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("YX"));
        ArrayList<TreeModelDTO> treeModelDTOS = new ArrayList<>();
        dictInfos.stream().forEach(dictInfoDO -> {
            TreeModelDTO treeModelDTO = new TreeModelDTO();
            treeModelDTOS.add(treeModelDTO);
            treeModelDTO.setId(dictInfoDO.getId());
            treeModelDTO.setCode(dictInfoDO.getId());
            treeModelDTO.setValue(dictInfoDO.getName());
        });
        return treeModelDTOS;
    }

    /**
     * usermk.labelquery.accept=text/plain
     * usermk.labelquery.authorization=gateway USER20181023010000005660&admin&YOULU&SCHOOL20190411010000000019
     * usermk.labelquery.Content-Type=application/json-patch+json
     * usermk.labelquery.checkCode=
     * usermk.labelquery.url=http://servicedev.zywinner.com/marketing/api/label/list
     *
     * @throws Exception
     */
    @Override
    public void test() throws Exception {
        LabelQueryDTO labelQueryDTO = new LabelQueryDTO();
        labelQueryDTO.setLabelCategory("SA");
        labelQueryDTO.setLabelName("testsYLH");
        labelQueryDTO.setLabelMode("H");
        labelQueryDTO.setSchoolIdList(new ArrayList<String>());
        //TODO 获取标签创建人id
        labelQueryDTO.setLabelCreator("USER20200807220000000006");

        System.out.println("labelQueryDTO:" + labelQueryDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderEnum.ACCEPT.getValue(), "text/plain");
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), "gateway USER20200807220000000006&YL033282&YOULU&SCHOOL20190411010000000019");
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), "application/json-patch+json");
        HttpEntity<LabelQueryDTO> labelQueryDTOHttpEntity = new HttpEntity<>(labelQueryDTO, headers);
        JSONObject responseBody = restTemplate.postForEntity("http://servicedev.zywinner.com/marketing/api/label/list", labelQueryDTOHttpEntity, JSONObject.class).getBody();
        String queryResponseCode = responseBody.getString("code");
        if (!"0000".equals(queryResponseCode)) { // 响应码0000表示成功
            throw new Exception(responseBody.getString("msg"));
        }
    }


    /**
     * 逻辑：
     * 1、任务名称相同不允许添加；
     * 2、同分校下标签名称相同不允许添加，即客户营销远程接口异常不允许添加；
     * 3、客户营销保存标签接口，失败则直接返回；
     * 4、客户营销标签查询接口获取标签对应id和标签名称；
     * 5、标签规则SQL解析；
     * 6、保存标签信息及任务信息到本地存储；
     * 7、仅执行一次的任务，直接调用执行；
     * 8、需要定时执行的标签任务创建定时执行；
     */
    @Override
    public void createLabelTaskConfig(LabelTaskDTO labelTaskDTO) throws Exception {


        //1、任务名称相同不允许添加；
        Long count = labelInfoMapper.findJobScheduleByJobName(labelTaskDTO.getTaskName());
        if (count > 0) {
            throw new Exception("任务名已存在，请重新设置！");
        }


        LabelAddDTO labelAddDTO = new LabelAddDTO();
        labelAddDTO.setLabelCategory(labelTaskDTO.getLabelCategory());
        labelAddDTO.setLabelName(labelTaskDTO.getLabelName());
        labelAddDTO.setLabelMode("H");
        List<String> labelScope = labelTaskDTO.getLabelScope();
        if (labelTaskDTO.getLabelScope() == null || labelTaskDTO.getLabelScope().size()==0) {
            List<DictInfoDO> dictInfos = labelMapper.findDictInfoByType(Arrays.asList("BdDpt"));
            labelScope = dictInfos.stream().map(dictInfoDO -> dictInfoDO.getId()).distinct().collect(Collectors.toList());
        }
        labelAddDTO.setLabelscopesDptids(labelScope);

        System.out.println(labelScope);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderEnum.ACCEPT.getValue(), usermkLabelCreateConfig.getAccept());

//        String gateway = "gateway USER20181023010000005660&admin&YOULU&SCHOOL20190411010000000019";
        String gateway = labelTaskDTO.getLoginUserId();

        String userId = gateway.substring(gateway.indexOf(" ") + 1, gateway.indexOf("&"));

//        headers.add(HeaderEnum.AUTHORIZATION.getValue(), usermkLabelCreateConfig.getAuthorization());
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), gateway);
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), usermkLabelCreateConfig.getContentType());
        headers.add(HeaderEnum.CHECKCODE.getValue(), usermkLabelCreateConfig.getCheckCode());
        HttpEntity<LabelAddDTO> requestEntity = new HttpEntity<>(labelAddDTO, headers);
        JSONObject responseBody = restTemplate.postForEntity(usermkLabelCreateConfig.getUrl(), requestEntity, JSONObject.class).getBody();
        String code = responseBody.getString("code");
        if (!"0000".equals(code)) { // 响应码0000表示成功
            throw new Exception(responseBody.getString("msg"));
        }

        /**
         * 添加完标签后需要调用接口获取新增的标签id;
         */

        LabelQueryDTO labelQueryDTO = new LabelQueryDTO();
        labelQueryDTO.setLabelCategory(labelTaskDTO.getLabelCategory());
        labelQueryDTO.setLabelName(labelTaskDTO.getLabelName());
        labelQueryDTO.setLabelMode("H");
        labelQueryDTO.setSchoolIdList(new ArrayList<String>());
        //TODO 获取标签创建人id
        labelQueryDTO.setLabelCreator(userId);

        System.out.println("labelQueryDTO:" + labelQueryDTO);

        headers = new HttpHeaders();
        headers.add(HeaderEnum.ACCEPT.getValue(), usermkLabelQueryConfig.getAccept());
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), gateway);
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), usermkLabelQueryConfig.getContentType());
        HttpEntity<LabelQueryDTO> labelQueryDTOHttpEntity = new HttpEntity<>(labelQueryDTO, headers);
        responseBody = restTemplate.postForEntity(usermkLabelQueryConfig.getUrl(), labelQueryDTOHttpEntity, JSONObject.class).getBody();
        String queryResponseCode = responseBody.getString("code");
        if (!"0000".equals(queryResponseCode)) { // 响应码0000表示成功
            throw new Exception(responseBody.getString("msg"));
        }

        JSONObject responseData = responseBody.getJSONObject("data");
        JSONArray dataArr = responseData.getJSONArray("data");
        String remoteInterfaceLabelId = null;
        String remoteInterfaceLabelName = null;
        if (dataArr.size() > 0) {
            /**
             * 客户营销查询接口是模糊匹配标签名称，所以可能会返回多条匹配到的标签信息；而这里要的是我自己新建的标签id信息，还需要进一步过滤；
             */
            for (int i = 0; i < dataArr.size(); i++) {
                JSONObject dataInfos = dataArr.getJSONObject(i);
                String labelName = dataInfos.getString("labelName");
                if (labelTaskDTO.getLabelName().equals(labelName)) {
                    remoteInterfaceLabelId = dataInfos.getString("labelId");
                    remoteInterfaceLabelName = labelName;
                    break;
                }
            }
        }
        if (remoteInterfaceLabelId == null) {
            throw new Exception("远程查询标签接口无法获取新建的标签ID！");
        }

        /**
         * 标签规则解析
         */
        //本地标签规则存储；
        LabelInfoDO labelInfoDO = new LabelInfoDO();
        labelInfoDO.setCustmkLabelId(remoteInterfaceLabelId);
        labelInfoDO.setLabelCategory(labelTaskDTO.getLabelCategory());
        labelInfoDO.setLabelName(labelTaskDTO.getLabelName());
        labelInfoDO.setLabelMode(labelTaskDTO.getLabelCreateType());
        labelInfoDO.setLabelCondition(JSONObject.toJSONString(labelTaskDTO.getLabelRules()));
        labelInfoDO.setLabelCreator(userId);
        labelInfoDO.setLabelModifier(userId);
        labelInfoDO.setLabelDelstatus(1);
        labelInfoDO.setLabelAavlstatus(1);
        //插入数据成功后，主键保存在LabelInfoDO对象的id里，返回值知只是保存成功的数据量；
        labelInfoMapper.saveLabelInfo(labelInfoDO);


        //设置JOB，及标签job保存；

        String labelSQL = this.parserLableRule2SQL(labelTaskDTO.getLabelRules());

        System.out.println("labelSQL:" + labelSQL);
        //TODO
//        labelSQL = "select distinct custId from dws_label_auto_info limit 100";

        /**
         * 为了对应job和trigger，这里设置相同后缀，只通过前缀区分job和trigger
         * 为了任务暂停，取消，修改需要，要保存对应job和trigger name；
         */

        String identity = String.format("%s_%s", DateUtil.dtf.format(LocalDateTime.now()), UUID.randomUUID().toString().replace("-", ""));
        String jobName = "job_" + identity;
        String triggerName = "trigger_" + identity;


        boolean flag = false;
        if ("ONCE".equals(labelTaskDTO.getCronExpression())) {
            flag = true;
        }

        //获取cron expression配置信息
        List<LabelFieldMappingDO> labelFieldMappingDOs = labelInfoMapper.getFieldMappingByReportName("cron_expression");
        String cronExpression = null;
        for (LabelFieldMappingDO labelFieldMappingDO : labelFieldMappingDOs) {
            if (labelFieldMappingDO.getSrcField().equals(labelTaskDTO.getCronExpression())) {
                cronExpression = labelFieldMappingDO.getTargetField();
                break;
            }
        }
        if (StringUtils.isBlank(cronExpression)) {
            throw new Exception("请设置任务调度cron !");
        }


        /**
         * 新建任务时，job_schedule的jobParameter暂时不设置，
         * 因为这个参数主要是为给定时任务使用，定时任务里需要用到这条job_shedule的主键id信息，用于在定时任务执行时更新任务状态；
         * 定时任务重新启动即为删除原任务，重新新建一个任务；这时候就需要用到原来job_schedule中的这个任务的参数信息；所以要将原定时任务中的参数保存下来；
         */
        LabelJobSheduleDO labelJobSheduleDO = new LabelJobSheduleDO();
        labelJobSheduleDO.setLabelId(labelInfoDO.getId());
        labelJobSheduleDO.setJobName(labelTaskDTO.getTaskName());
        labelJobSheduleDO.setJobCronExpression(cronExpression);

        //如果仅执行一次；则没有job表没有对应的qrtz_job_name和qrtz_trigger_name；
        if (!flag) {
            labelJobSheduleDO.setQrtzJobName(jobName);
            labelJobSheduleDO.setQrtzTriggerName(triggerName);
        }
        labelJobSheduleDO.setJobProcessStatus(0); //初始值为未处理，执行过程中会修改jobprocessz状态值；
        labelJobSheduleDO.setJobCreator(userId);
        labelJobSheduleDO.setJobModifier(userId);
        labelJobSheduleDO.setJobStatus(1);
        //保存标签job信息到mysql，返回主键信息；
        labelInfoMapper.saveLabelJobSchedule(labelJobSheduleDO);

        /**
         * 封装job schedule需要的参数；
         */
        JobScheduleDTO jobScheduleDTO = new JobScheduleDTO();
        jobScheduleDTO.setLabelSQL(labelSQL);
        jobScheduleDTO.setJobScheduleId(String.valueOf(labelJobSheduleDO.getId()));
        jobScheduleDTO.setGateway(gateway);
        jobScheduleDTO.setRemoteInterfaceLabelId(remoteInterfaceLabelId);
        jobScheduleDTO.setRemoteInterfaceLabelName(remoteInterfaceLabelName);
        //仅执行一次时，启动定时调度任务；
        if (!flag) {
            this.schedulerAdd(cronExpression, jobScheduleDTO, jobName, triggerName);
        } else {
            List<LabelDTO> labelDTOs = new ArrayList<>();
            LabelDTO labelDTO = new LabelDTO();
            labelDTOs.add(labelDTO);
            labelDTO.setId(remoteInterfaceLabelId);
            labelDTO.setName(remoteInterfaceLabelName);

            LabelJobSheduleDO finalLabelJobSheduleDO = labelJobSheduleDO;
            String finalLabelSQL = labelSQL;
            new Thread(() -> {
                try {
                    System.out.println("------------------------异步处理用户标签信息！---------start--------" + LocalDateTime.now());
                    labelCustIdProcess(String.valueOf(finalLabelJobSheduleDO.getId()), finalLabelSQL, labelDTOs, gateway);
                    System.out.println("------------------------异步处理用户标签信息！--------end---------" + LocalDateTime.now());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

//            labelCustIdProcess(String.valueOf(labelJobSheduleDO.getId()), labelSQL, labelDTOs, gateway);
        }
        //定时任务添加成功后，保存定时任务需要的参数信息；
//        labelJobSheduleDO = new LabelJobSheduleDO();
        String jobScheduleParam = JSONObject.toJSONString(jobScheduleDTO);

        labelJobSheduleDO.setJobParameter(jobScheduleParam);
        //更新jobshedule的params字段信息
        labelInfoMapper.updateLabelJobScheduleById(labelJobSheduleDO);
    }


    /**
     * 获取客户营销远程标签列表信息，只允许查看自己创建的标签信息；
     * <p>
     * 由于客户营销系统上线早，所以用户标签数量会比这边系统数据多，但是展示时是以这边系统标签为主进行分页展示；
     * 所以：
     * 这边系统按照查询出分页数据；再一条一条去客户营销系统匹配；客户营销标签列表接口不支持按照标签id获取标签信息，只能按照标签名称模糊匹配；
     *
     * @return
     */
    @Override
    public ListResult<?> findLabelInfoList(LabelQueryVO labelQueryVO) throws Exception {


        Integer pageIndex = labelQueryVO.getPageIndex();
        Integer pageSize = labelQueryVO.getPageSize();

        pageSize = (pageIndex - 1) * pageSize;
        labelQueryVO.setPageIndex(pageSize);

        String gateway = labelQueryVO.getLabelCreator();
        String userId = gateway.substring(gateway.indexOf(" ") + 1, gateway.indexOf("&"));

        labelQueryVO.setLabelCreator(userId);

        List<LabelJobInfo> labelJobInfo = labelInfoMapper.findLabelJobInfo(labelQueryVO);
        Long total = labelInfoMapper.findLabelJobInfoTotal(labelQueryVO);

        /**
         * 查询客户营销系统标签信息；
         */


        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderEnum.ACCEPT.getValue(), usermkLabelQueryConfig.getAccept());
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), gateway);
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), usermkLabelQueryConfig.getContentType());


        LabelRemoteQueryDTO labelRemoteQueryDTO;
        List<LabelInfoDTO> labelInfoDTOS = new ArrayList<>();

        for (LabelJobInfo jobInfo : labelJobInfo) {

//            boolean flag = false;
            String labelId = jobInfo.getLabelId();
            String custmkLabelId = jobInfo.getCustmkLabelId();
            String labelName = jobInfo.getLabelName();
            String jobProcessStatus = jobInfo.getJobProcessStatus();

            //封装查询客户营销标签列表远程接口对象；
            labelRemoteQueryDTO = new LabelRemoteQueryDTO();
            labelRemoteQueryDTO.setLabelCreator(userId);
            labelRemoteQueryDTO.setLabelCreatordatestart(labelQueryVO.getLabelCreatordatestart());
            labelRemoteQueryDTO.setLabelCreatordateend(labelQueryVO.getLabelCreatordateend());
            labelRemoteQueryDTO.setLabelName(labelName);

            HttpEntity<LabelRemoteQueryDTO> labelRemoteQueryDTOHttpEntity = new HttpEntity<>(labelRemoteQueryDTO, headers);
            JSONObject responseBody = restTemplate.postForEntity(usermkLabelQueryConfig.getUrl(), labelRemoteQueryDTOHttpEntity, JSONObject.class).getBody();
            String queryResponseCode = responseBody.getString("code");
            if (!"0000".equals(queryResponseCode)) { // 响应码0000表示成功
                throw new Exception("客户营销远程接口异常：" + responseBody.getString("msg"));
            }
            JSONObject responseData = responseBody.getJSONObject("data");
            JSONArray dataArr = responseData.getJSONArray("data");

            LabelInfoDTO labelInfoDTO;
            for (int i = 0; i < dataArr.size(); i++) {
                CustmkLabelInfoDTO custmkLabelInfo = JSONObject.parseObject(JSON.toJSONString(dataArr.get(i)), new TypeReference<CustmkLabelInfoDTO>() {
                });
                String interfaceCustmkLabelId = custmkLabelInfo.getLabelId();
                if (custmkLabelId.equals(interfaceCustmkLabelId)) {
                    labelInfoDTO = new LabelInfoDTO();
                    labelInfoDTO.setLabelId(labelId);
                    labelInfoDTO.setCustmkLabelId(custmkLabelInfo.getLabelId());
                    labelInfoDTO.setLabelName(custmkLabelInfo.getLabelName());
                    labelInfoDTO.setLabelCategory(dictMap.getOrDefault(custmkLabelInfo.getLabelCategory(), "未知分类"));
                    //labelInfoDTO.setLabelCreateType(custmkLabelInfo.getLabelMode());
                    labelInfoDTO.setLabelCreateType("手工");
                    labelInfoDTO.setLabelScopes(custmkLabelInfo.getLabelScopes());
                    labelInfoDTO.setLabelCreator(custmkLabelInfo.getLabelCreatorName());
                    labelInfoDTO.setLabelCreateDate(custmkLabelInfo.getLabelCreateddate());
                    labelInfoDTO.setTaskExecuteStatus(dictMap.get(jobProcessStatus));
                    labelInfoDTOS.add(labelInfoDTO);
//                    flag = true;
                    break;
                }
            }
//测试环境下：手动删除掉当前系统标签和客户营销标签不匹配的测试标签数据；
//            if (!flag) {
//                labelInfoMapper.deleteLabelInfoById(Long.parseLong(labelId));
//                System.out.println("---------删除："+labelId);
//            }
        }
        //这里不能直接使用接口响应的total，因为客户营销远程接口查询的是这个userid所有的标签，而这里的结果应该是从这个自动标签项目上线开始创建的标签数量；
        return ListResult.build(labelInfoDTOS, total);
    }

    /**
     * 1、删除本地标签信息；
     * 2、删除客户营销标签对应的job信息；
     * 3、删除job定时调度配置
     *
     * @param labelId
     * @throws Exception
     */
    @Override
    public void deleteLabelInfoById(String labelId, String custmkLabelId) throws Exception {


        //删除客户营销标签信息；

        JSONObject labelIdObj = new JSONObject();
        List<String> custmkLabelIds = new ArrayList<>();
        custmkLabelIds.add(custmkLabelId);
        labelIdObj.put("labelIds", custmkLabelIds);
        HttpHeaders headers = new HttpHeaders();

        headers.add(HeaderEnum.ACCEPT.getValue(), usermkLabelRemoveConfig.getAccept());
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), usermkLabelRemoveConfig.getAuthorization());
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), usermkLabelRemoveConfig.getContentType());
        headers.add(HeaderEnum.CHECKCODE.getValue(), usermkLabelRemoveConfig.getCheckCode());
        HttpEntity<JSONObject> labelQueryDTOHttpEntity = new HttpEntity<>(labelIdObj, headers);

        /**
         * 客户营销标签删除接口使用的是delete方式，并且有响应数据，所以使用restTemplate.exchange()方法；
         */
        JSONObject responseBody = restTemplate.exchange(usermkLabelRemoveConfig.getUrl(), HttpMethod.DELETE, labelQueryDTOHttpEntity, JSONObject.class).getBody();
        String queryResponseCode = responseBody.getString("code");
        if (!"0000".equals(queryResponseCode)) { // 响应码0000表示成功
            throw new Exception("标签删除失败,原因：" + responseBody.getString("msg"));
        }

        //删除本地标签信息
        labelInfoMapper.deleteLabelInfoById(Long.valueOf(labelId));

        //删除job定时调度配置
        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.valueOf(labelId));
        String jobCronExpression = labelJobSheduleDO.getJobCronExpression();
        //只执行一次的任务不需要删除定时任务
        if (!"ONCE".equals(jobCronExpression)) {
            String qrtzJobName = labelJobSheduleDO.getQrtzJobName();
            String qrtzTriggerName = labelJobSheduleDO.getQrtzTriggerName();
            //删除对应定时任务
            schedulerDelete(qrtzJobName, qrtzTriggerName);
        }

        //删除job_schedule信息；
        labelInfoMapper.removeJobScheduleByLabelId(Long.valueOf(labelId));
    }

    /**
     * 暂停定时任务，
     * 1、修改任务状态为暂停；
     * 2、删除定时任务；
     *
     * @param labelId
     * @throws Exception
     */
    @Override
    public void pauseLabelTaskById(String labelId) throws Exception {
        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.valueOf(labelId));
        if ("ONCE".equals(labelJobSheduleDO.getJobCronExpression())) {
            throw new Exception("只执行一次的任务不需要暂停");
        }
        schedulerDelete(labelJobSheduleDO.getQrtzJobName(), labelJobSheduleDO.getQrtzTriggerName());
        labelInfoMapper.updateLabelJobScheduleStatusById(Long.valueOf(labelJobSheduleDO.getId()), 0);
    }

    /**
     * 定时任务恢复：
     * 1、先删除原定时任务；
     * 2、根据原任务参数重新创建任务；
     * 3、修改job_schedule状态；
     *
     * @param labelId
     * @throws Exception
     */
    @Override
    public void recoverLabelTaskById(String labelId) throws Exception {


        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.valueOf(labelId));
        if (1 == labelJobSheduleDO.getJobStatus()) {
            throw new Exception("标签任务正常，不必恢复任务！");
        }
        if ("ONCE".equals(labelJobSheduleDO.getJobCronExpression())) {
            throw new Exception("只执行一次的任务已执行后不能恢复");
        }

        schedulerDelete(labelJobSheduleDO.getQrtzJobName(), labelJobSheduleDO.getQrtzTriggerName());


        String identity = String.format("%s_%s", DateUtil.dtf.format(LocalDateTime.now()), UUID.randomUUID().toString().replace("-", ""));
        String jobName = "job_" + identity;
        String triggerName = "trigger_" + identity;

        String jobParameter = labelJobSheduleDO.getJobParameter();
        String jobCronExpression = labelJobSheduleDO.getJobCronExpression();
        JobScheduleDTO jobScheduleDTO = JSONObject.parseObject(jobParameter, JobScheduleDTO.class);
        this.schedulerAdd(jobCronExpression, jobScheduleDTO, jobName, triggerName);


        labelJobSheduleDO.setQrtzJobName(jobName);
        labelJobSheduleDO.setQrtzTriggerName(triggerName);
        labelJobSheduleDO.setJobStatus(1);
        labelInfoMapper.updateLabelJobSchedule(labelJobSheduleDO);
    }


    @Override
    public void executeLabel(String labelId) throws Exception {

        System.out.println("-------------labelId:" + labelId);
        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.parseLong(labelId));
        Integer jobProcessStatus = labelJobSheduleDO.getJobProcessStatus();
        if (1 == jobProcessStatus) {
            throw new Exception("任务处理中，请稍后再执行！");
        } else if (-1 == jobProcessStatus) {
            throw new Exception("该标签任务已失败，请联系管理员处理！");
        }

        LabelInfoDO labelInfoDO = labelInfoMapper.findLabelInfoByLabelId(labelJobSheduleDO.getLabelId());


        String custmkLabelId = labelInfoDO.getCustmkLabelId();
        String labelName = labelInfoDO.getLabelName();
        List<Object> labelDTOs = new ArrayList<>();
        LabelDTO labelDTO = new LabelDTO();
        labelDTOs.add(labelDTO);
        labelDTO.setId(custmkLabelId);
        labelDTO.setName(labelName);

        JobScheduleDTO jobScheduleDTO = JSONObject.parseObject(labelJobSheduleDO.getJobParameter(), JobScheduleDTO.class);
        String jobScheduleId = jobScheduleDTO.getJobScheduleId();
        String labelSQL = jobScheduleDTO.getLabelSQL();
        String gateway = jobScheduleDTO.getGateway();
        new Thread(() -> {
            try {
                labelCustIdProcess(jobScheduleId, labelSQL, labelDTOs, gateway);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    public Boolean labelCustIdProcess(String jobScheduleId, String labelSQL, List labelDTOs, String gateway) throws
            Exception {
        List<String> custIds = labelMapper.queryLabelCustId(labelSQL);
        try {
            System.out.println(String.format("----------jobScheduleId:%s-----------custIds.size():%s------------labelSQL:%s", jobScheduleId, custIds.size(), labelSQL));
            modifyJobProcessStatus(jobScheduleId, "1"); //更新状态为处理中；
            if (custIds.size() != 0) {
                saveCustLabel(custIds, labelDTOs, gateway);
            }
            modifyJobProcessStatus(jobScheduleId, "2"); //更新状态为已完成；
            return true;
        } catch (Exception e) {
            modifyJobProcessStatus(jobScheduleId, "-1"); //更新状态为已完成；
            throw new Exception("处理失败！");
        }
    }

    public void modifyJobProcessStatus(String jobId, String jobProcessStatus) {
        labelInfoMapper.updateLabelJobScheduleJobProcessStatusById(Long.parseLong(jobId), Integer.valueOf(jobProcessStatus));
    }


    public void saveCustLabel(List<String> custIds, List labels, String gateway) throws Exception {
        int size = custIds.size();
        int fromIndex = 0;
        List<Future<Boolean>> futurns = new ArrayList();
        while (fromIndex < size) {
            int toIndex = fromIndex + batchSize > size ? size : fromIndex + batchSize;
            List<String> subCustIds = custIds.subList(fromIndex, toIndex);
            LabelCustBatchAddDTO labelCustBatchAddDTO = new LabelCustBatchAddDTO();
            labelCustBatchAddDTO.setData(subCustIds);
            labelCustBatchAddDTO.setDataScope("S");
            labelCustBatchAddDTO.setLabelList(labels);
            String url = "http://service.zywinner.com/marketing/api/custlabel/batchaddlabel";
            Future<Boolean> submit = executors.submit(() -> new LabelRulesJob().remoteSaveCustLableInterface(url, gateway, labelCustBatchAddDTO));
            futurns.add(submit);
            fromIndex = toIndex;
        }
        for (Future<Boolean> futurn : futurns) {
            Boolean aBoolean = futurn.get();
            if (!aBoolean) {
                throw new Exception("------批量添加 部分 客户标签失败");
            }
        }
    }


    public String parserLableRule2SQL(List<LabelConditionDTO> labelRule) throws Exception {
        List<LabelFieldMappingDO> labelFieldMappingDOs = labelInfoMapper.getFieldMappingByReportName("label_auto");
        Map<String, String> fieldMap = new HashMap<>();
        labelFieldMappingDOs.stream().forEach(labelFieldMappingDO -> fieldMap.put(labelFieldMappingDO.getSrcField(), labelFieldMappingDO.getTargetField()));
        StringBuilder querySQLStr = new StringBuilder("SELECT distinct custId from ods.dws_label_auto_info where 1=1 ");
        for (LabelConditionDTO labelConditionDTO : labelRule) {
            StringBuffer conditionFieldAndValues = new StringBuffer();
            String targetField = fieldMap.getOrDefault(labelConditionDTO.getName(), null);
            String relation = labelConditionDTO.getRelation();
            List<String> conditionValues;
            List<String> dateFields = Arrays.asList("custCreateDate", "orderCreateDate", "receiptCreateDate", "classCourseStartDate", "commuTime");
            Boolean flag = false;
            //时间串 startTime~endTime
            if (dateFields.contains(labelConditionDTO.getName())) {
                conditionValues = Arrays.asList(labelConditionDTO.getValue().split("-"));
                if (conditionValues.size() < 2) {
                    throw new Exception(" 时间条件，请务必传入-分割的两个时间，如 20210501~20210601");
                }
                flag = true;
            } else {
                //其他非时间串，多个值时 使用&拼接；
                conditionValues = Arrays.asList(labelConditionDTO.getValue().split("&"));
            }
            if (StringUtils.isBlank(targetField) || StringUtils.isBlank(relation) || conditionValues.size() == 0) {
                throw new RuntimeException(String.format("条件异常，请检查！, targetField:%s,relation:%s,conditionValues.size():%s", targetField, relation, conditionValues.size()));
            }
            String valueStrs = null;
            if ("IN".equals(relation.trim().toUpperCase()) || "NOT IN".equals(relation.trim().toUpperCase())) {
                valueStrs = conditionValues.stream().collect(Collectors.joining("','", "('", "')"));
                conditionFieldAndValues.append(" AND ").append(targetField).append(" ").append(relation).append(" ").append(valueStrs);
            } else if ("=".equals(relation.trim())) {
                //时候格式条件特殊处理；
                if (flag) {
//                    conditionFieldAndValues.append(" AND ").append(" toYYYYMM(").append(targetField).append(") ").append(" >= ").append(conditionValues.get(0)).append(" AND ").append("toYYYYMM(").append(targetField).append(") ").append(" <= ").append(conditionValues.get(1));
                    conditionFieldAndValues.append(" AND ").append(targetField).append(" >= '").append(conditionValues.get(0)).append("' AND ").append(targetField).append(" <= '").append(conditionValues.get(1)).append("' ");
                } else {
                    //沟通内容使用模糊查询
                    if ("commContent".equals(targetField)) {
                        conditionFieldAndValues.append(" AND ").append(targetField).append(" like '%").append(conditionValues.get(0)).append("%' ");
                    } else {
                        conditionFieldAndValues.append(" AND ").append(targetField).append(" ").append(relation).append(" '").append(conditionValues.get(0)).append("' ");
                    }
                }
            }
            if (conditionFieldAndValues.length() > 0) {
                querySQLStr.append(conditionFieldAndValues);
            }
        }
        return querySQLStr.toString();
    }


    /**
     * 这里只使用一个job做任务调度，通过传入不同的执行labelSQL和cronExpression，
     * 由于重复的jobIdentity 或者是重复的triggerIdentity是无法添加调度；
     * 所以设置随机的identity来区别不同的job及trigger;
     *
     * @param cronExpression
     * @param jobScheduleDTO
     * @param jobIdentity
     * @param triggerIdentity
     * @throws Exception
     */
    private void schedulerAdd(String cronExpression, JobScheduleDTO jobScheduleDTO, String jobIdentity, String
            triggerIdentity) throws Exception {
        //调度器启动
        scheduler.start();
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobScheduleDTO", jobScheduleDTO);
        JobDetail jobDetail = JobBuilder.newJob(getClass(LabelRulesJob.class.getName()).getClass()).withIdentity(jobIdentity)
                .usingJobData(jobDataMap)
                .build();
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerIdentity).withSchedule(cronScheduleBuilder).build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static Job getClass(String className) throws Exception {
        Class<?> aClass = Class.forName(className);
        return (Job) aClass.newInstance();
    }

    /**
     * 删除定时任务
     *
     * @param jobName
     * @param triggerName
     * @throws Exception
     */
    private void schedulerDelete(String jobName, String triggerName) throws Exception {
        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(triggerName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
            scheduler.deleteJob(JobKey.jobKey(jobName));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(String.format(" 定时任务删除失败：jobName:%s--------triggerName:%s", jobName, triggerName));
        }
    }


    public List<TreeModelDTO> buildModeTree(List<DictInfoDO> dictInfos, String code, Integer codeLength) {
        List<TreeModelDTO> treeModelDTOS = null;
        TreeModelDTO treeModelDTO = null;
        for (DictInfoDO dictInfo : dictInfos) {
            if (dictInfo.getCode().length() == codeLength) {
                if (codeLength == 10) {
                    treeModelDTO = new TreeModelDTO();
                    treeModelDTO.setId(dictInfo.getId());
                    treeModelDTO.setCode(dictInfo.getCode());
                    treeModelDTO.setValue(dictInfo.getName());
                } else {
                    if (code.equals(dictInfo.getCode().substring(0, codeLength - 10))) {
                        treeModelDTO = new TreeModelDTO();
                        treeModelDTO.setId(dictInfo.getId());
                        treeModelDTO.setCode(dictInfo.getCode());
                        treeModelDTO.setValue(dictInfo.getName());
                    }
                }
                int childCodeLength = codeLength + 10;
                if (treeModelDTO != null) {
                    if (treeModelDTOS == null) {
                        treeModelDTOS = new ArrayList<>();
                    }
                    List<TreeModelDTO> childrenTree = buildModeTree(dictInfos, dictInfo.getCode(), childCodeLength);
                    treeModelDTO.setChildren(childrenTree);
                    treeModelDTOS.add(treeModelDTO);
                    treeModelDTO = null;
                }
            }
        }
        return treeModelDTOS;
    }

    public static List<TreeModelDTO> buildModeTree(List<DictInfoDO> dictInfos, String parentCode) {
        List<TreeModelDTO> treeModelDTOS = null;
        for (DictInfoDO dictInfo : dictInfos) {
            if (parentCode.equals(dictInfo.getParentCode())) {
                if (treeModelDTOS == null) {
                    treeModelDTOS = new ArrayList<>();
                }
                TreeModelDTO treeModelDTO = new TreeModelDTO();
                treeModelDTO.setId(dictInfo.getId());
                treeModelDTO.setCode(dictInfo.getCode());
                treeModelDTO.setValue(dictInfo.getName());
                List<TreeModelDTO> treeModelDTOS1 = buildModeTree(dictInfos, dictInfo.getId());
                treeModelDTO.setChildren(treeModelDTOS1);
                treeModelDTOS.add(treeModelDTO);
            }
        }
        return treeModelDTOS;
    }

    public static void main(String[] args) {
        String str= "{\n" +
                "  \"PlatformAppOptions\": [\n" +
                "    {\n" +
                "      \"PlatformCode\": \"OP\",\n" +
                "      \"PlatformName\": \"运营平台\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"运营平台网站\",\n" +
                "          \"Code\": \"OP.WEB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"OA\",\n" +
                "      \"PlatformName\": \"运营平台移动端\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"运营平台App\",\n" +
                "          \"Code\": \"OP.APP\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"YP\",\n" +
                "      \"PlatformName\": \"优路教育官网\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"优路教育官网\",\n" +
                "          \"Code\": \"PORTAL.WEB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"YA\",\n" +
                "      \"PlatformName\": \"优路学员移动端\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"优路教育App\",\n" +
                "          \"Code\": \"PORTAL.APP\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路教师APP\",\n" +
                "          \"Code\": \"YOULU.TEACHER.APP\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"TM\",\n" +
                "      \"PlatformName\": \"天猫\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"原天猫优路教育旗舰店\",\n" +
                "          \"Code\": \"TMALL.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"天猫优路教育旗舰店\",\n" +
                "          \"Code\": \"YOULU.TMALL.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"天猫环球卓越旗舰店\",\n" +
                "          \"Code\": \"GEEDU.TMALL.MAIN\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"JD\",\n" +
                "      \"PlatformName\": \"京东\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"原京东优路教育旗舰店\",\n" +
                "          \"Code\": \"JD.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"京东优路教育旗舰店\",\n" +
                "          \"Code\": \"YOULU.JD.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"京东优路官方旗舰店\",\n" +
                "          \"Code\": \"YOULU.JD.OFFICIAL\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"京东优路图书专营店\",\n" +
                "          \"Code\": \"YOULU.JD.BOOK\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"DA\",\n" +
                "      \"PlatformName\": \"合作代理平台\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"分销联盟-电商\",\n" +
                "          \"Code\": \"CAP.DA.ECOMMERCE\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"EC\",\n" +
                "      \"PlatformName\": \"企业客户\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"企业客户Web\",\n" +
                "          \"Code\": \"EC.WEB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"WMP\",\n" +
                "      \"PlatformName\": \"微信小程序\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"优路备考\",\n" +
                "          \"Code\": \"YOULU.WX.ZONGHEBEIKAO\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路医考\",\n" +
                "          \"Code\": \"YOULU.WX.YIWEI\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"法考小程序\",\n" +
                "          \"Code\": \"YOULU.WX.FAKAO\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"教师小程序\",\n" +
                "          \"Code\": \"YOULU.WX.JIAOSHI\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"XET\",\n" +
                "      \"PlatformName\": \"小鹅通\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"北京环球优路\",\n" +
                "          \"Code\": \"XET.HQYL\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路考教师\",\n" +
                "          \"Code\": \"XET.YLTEAC\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路教育\",\n" +
                "          \"Code\": \"XET.YLEDU\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路法考\",\n" +
                "          \"Code\": \"XET.YLFK\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路医卫学院\",\n" +
                "          \"Code\": \"XET.YIWEICOLLEGE\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"优路考研\",\n" +
                "          \"Code\": \"XET.YLKAOYAN\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"YOUZAN\",\n" +
                "      \"PlatformName\": \"有赞\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"优路医卫学院\",\n" +
                "          \"Code\": \"YOUZAN.YWSYB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"MC\",\n" +
                "      \"PlatformName\": \"营销中心\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"拼团\",\n" +
                "          \"Code\": \"MC.GROUPBUYING\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        System.out.println(JSONObject.toJSON(str));
    }
}
