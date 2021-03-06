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
    private static int batchSize = 5000; //????????????????????????????????????

    private static Map<String, String> dictMap = new HashMap<>();

    static {
        dictMap.put("SA", "??????");
        dictMap.put("FI", "??????");
        dictMap.put("SE", "??????");
        dictMap.put("HR", "??????");
        dictMap.put("2", "?????????");
        dictMap.put("1", "?????????");
        dictMap.put("0", "?????????");
        dictMap.put("-1", "??????");
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
        //TODO ?????????????????????id
        labelQueryDTO.setLabelCreator("USER20200807220000000006");

        System.out.println("labelQueryDTO:" + labelQueryDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderEnum.ACCEPT.getValue(), "text/plain");
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), "gateway USER20200807220000000006&YL033282&YOULU&SCHOOL20190411010000000019");
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), "application/json-patch+json");
        HttpEntity<LabelQueryDTO> labelQueryDTOHttpEntity = new HttpEntity<>(labelQueryDTO, headers);
        JSONObject responseBody = restTemplate.postForEntity("http://servicedev.zywinner.com/marketing/api/label/list", labelQueryDTOHttpEntity, JSONObject.class).getBody();
        String queryResponseCode = responseBody.getString("code");
        if (!"0000".equals(queryResponseCode)) { // ?????????0000????????????
            throw new Exception(responseBody.getString("msg"));
        }
    }


    /**
     * ?????????
     * 1???????????????????????????????????????
     * 2??????????????????????????????????????????????????????????????????????????????????????????????????????
     * 3????????????????????????????????????????????????????????????
     * 4???????????????????????????????????????????????????id??????????????????
     * 5???????????????SQL?????????
     * 6??????????????????????????????????????????????????????
     * 7???????????????????????????????????????????????????
     * 8?????????????????????????????????????????????????????????
     */
    @Override
    public void createLabelTaskConfig(LabelTaskDTO labelTaskDTO) throws Exception {


        //1???????????????????????????????????????
        Long count = labelInfoMapper.findJobScheduleByJobName(labelTaskDTO.getTaskName());
        if (count > 0) {
            throw new Exception("???????????????????????????????????????");
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
        if (!"0000".equals(code)) { // ?????????0000????????????
            throw new Exception(responseBody.getString("msg"));
        }

        /**
         * ?????????????????????????????????????????????????????????id;
         */

        LabelQueryDTO labelQueryDTO = new LabelQueryDTO();
        labelQueryDTO.setLabelCategory(labelTaskDTO.getLabelCategory());
        labelQueryDTO.setLabelName(labelTaskDTO.getLabelName());
        labelQueryDTO.setLabelMode("H");
        labelQueryDTO.setSchoolIdList(new ArrayList<String>());
        //TODO ?????????????????????id
        labelQueryDTO.setLabelCreator(userId);

        System.out.println("labelQueryDTO:" + labelQueryDTO);

        headers = new HttpHeaders();
        headers.add(HeaderEnum.ACCEPT.getValue(), usermkLabelQueryConfig.getAccept());
        headers.add(HeaderEnum.AUTHORIZATION.getValue(), gateway);
        headers.add(HeaderEnum.CONTENTTYPE.getValue(), usermkLabelQueryConfig.getContentType());
        HttpEntity<LabelQueryDTO> labelQueryDTOHttpEntity = new HttpEntity<>(labelQueryDTO, headers);
        responseBody = restTemplate.postForEntity(usermkLabelQueryConfig.getUrl(), labelQueryDTOHttpEntity, JSONObject.class).getBody();
        String queryResponseCode = responseBody.getString("code");
        if (!"0000".equals(queryResponseCode)) { // ?????????0000????????????
            throw new Exception(responseBody.getString("msg"));
        }

        JSONObject responseData = responseBody.getJSONObject("data");
        JSONArray dataArr = responseData.getJSONArray("data");
        String remoteInterfaceLabelId = null;
        String remoteInterfaceLabelName = null;
        if (dataArr.size() > 0) {
            /**
             * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????id????????????????????????????????????
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
            throw new Exception("???????????????????????????????????????????????????ID???");
        }

        /**
         * ??????????????????
         */
        //???????????????????????????
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
        //???????????????????????????????????????LabelInfoDO?????????id???????????????????????????????????????????????????
        labelInfoMapper.saveLabelInfo(labelInfoDO);


        //??????JOB????????????job?????????

        String labelSQL = this.parserLableRule2SQL(labelTaskDTO.getLabelRules());

        System.out.println("labelSQL:" + labelSQL);
        //TODO
//        labelSQL = "select distinct custId from dws_label_auto_info limit 100";

        /**
         * ????????????job???trigger???????????????????????????????????????????????????job???trigger
         * ????????????????????????????????????????????????????????????job???trigger name???
         */

        String identity = String.format("%s_%s", DateUtil.dtf.format(LocalDateTime.now()), UUID.randomUUID().toString().replace("-", ""));
        String jobName = "job_" + identity;
        String triggerName = "trigger_" + identity;


        boolean flag = false;
        if ("ONCE".equals(labelTaskDTO.getCronExpression())) {
            flag = true;
        }

        //??????cron expression????????????
        List<LabelFieldMappingDO> labelFieldMappingDOs = labelInfoMapper.getFieldMappingByReportName("cron_expression");
        String cronExpression = null;
        for (LabelFieldMappingDO labelFieldMappingDO : labelFieldMappingDOs) {
            if (labelFieldMappingDO.getSrcField().equals(labelTaskDTO.getCronExpression())) {
                cronExpression = labelFieldMappingDO.getTargetField();
                break;
            }
        }
        if (StringUtils.isBlank(cronExpression)) {
            throw new Exception("?????????????????????cron !");
        }


        /**
         * ??????????????????job_schedule???jobParameter??????????????????
         * ???????????????????????????????????????????????????????????????????????????????????????job_shedule?????????id????????????????????????????????????????????????????????????
         * ?????????????????????????????????????????????????????????????????????????????????????????????????????????job_schedule??????????????????????????????????????????????????????????????????????????????????????????
         */
        LabelJobSheduleDO labelJobSheduleDO = new LabelJobSheduleDO();
        labelJobSheduleDO.setLabelId(labelInfoDO.getId());
        labelJobSheduleDO.setJobName(labelTaskDTO.getTaskName());
        labelJobSheduleDO.setJobCronExpression(cronExpression);

        //?????????????????????????????????job??????????????????qrtz_job_name???qrtz_trigger_name???
        if (!flag) {
            labelJobSheduleDO.setQrtzJobName(jobName);
            labelJobSheduleDO.setQrtzTriggerName(triggerName);
        }
        labelJobSheduleDO.setJobProcessStatus(0); //????????????????????????????????????????????????jobprocessz????????????
        labelJobSheduleDO.setJobCreator(userId);
        labelJobSheduleDO.setJobModifier(userId);
        labelJobSheduleDO.setJobStatus(1);
        //????????????job?????????mysql????????????????????????
        labelInfoMapper.saveLabelJobSchedule(labelJobSheduleDO);

        /**
         * ??????job schedule??????????????????
         */
        JobScheduleDTO jobScheduleDTO = new JobScheduleDTO();
        jobScheduleDTO.setLabelSQL(labelSQL);
        jobScheduleDTO.setJobScheduleId(String.valueOf(labelJobSheduleDO.getId()));
        jobScheduleDTO.setGateway(gateway);
        jobScheduleDTO.setRemoteInterfaceLabelId(remoteInterfaceLabelId);
        jobScheduleDTO.setRemoteInterfaceLabelName(remoteInterfaceLabelName);
        //????????????????????????????????????????????????
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
                    System.out.println("------------------------?????????????????????????????????---------start--------" + LocalDateTime.now());
                    labelCustIdProcess(String.valueOf(finalLabelJobSheduleDO.getId()), finalLabelSQL, labelDTOs, gateway);
                    System.out.println("------------------------?????????????????????????????????--------end---------" + LocalDateTime.now());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

//            labelCustIdProcess(String.valueOf(labelJobSheduleDO.getId()), labelSQL, labelDTOs, gateway);
        }
        //????????????????????????????????????????????????????????????????????????
//        labelJobSheduleDO = new LabelJobSheduleDO();
        String jobScheduleParam = JSONObject.toJSONString(jobScheduleDTO);

        labelJobSheduleDO.setJobParameter(jobScheduleParam);
        //??????jobshedule???params????????????
        labelInfoMapper.updateLabelJobScheduleById(labelJobSheduleDO);
    }


    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     * <p>
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ?????????
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????id????????????????????????????????????????????????????????????
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
         * ???????????????????????????????????????
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

            //?????????????????????????????????????????????????????????
            labelRemoteQueryDTO = new LabelRemoteQueryDTO();
            labelRemoteQueryDTO.setLabelCreator(userId);
            labelRemoteQueryDTO.setLabelCreatordatestart(labelQueryVO.getLabelCreatordatestart());
            labelRemoteQueryDTO.setLabelCreatordateend(labelQueryVO.getLabelCreatordateend());
            labelRemoteQueryDTO.setLabelName(labelName);

            HttpEntity<LabelRemoteQueryDTO> labelRemoteQueryDTOHttpEntity = new HttpEntity<>(labelRemoteQueryDTO, headers);
            JSONObject responseBody = restTemplate.postForEntity(usermkLabelQueryConfig.getUrl(), labelRemoteQueryDTOHttpEntity, JSONObject.class).getBody();
            String queryResponseCode = responseBody.getString("code");
            if (!"0000".equals(queryResponseCode)) { // ?????????0000????????????
                throw new Exception("?????????????????????????????????" + responseBody.getString("msg"));
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
                    labelInfoDTO.setLabelCategory(dictMap.getOrDefault(custmkLabelInfo.getLabelCategory(), "????????????"));
                    //labelInfoDTO.setLabelCreateType(custmkLabelInfo.getLabelMode());
                    labelInfoDTO.setLabelCreateType("??????");
                    labelInfoDTO.setLabelScopes(custmkLabelInfo.getLabelScopes());
                    labelInfoDTO.setLabelCreator(custmkLabelInfo.getLabelCreatorName());
                    labelInfoDTO.setLabelCreateDate(custmkLabelInfo.getLabelCreateddate());
                    labelInfoDTO.setTaskExecuteStatus(dictMap.get(jobProcessStatus));
                    labelInfoDTOS.add(labelInfoDTO);
//                    flag = true;
                    break;
                }
            }
//?????????????????????????????????????????????????????????????????????????????????????????????????????????
//            if (!flag) {
//                labelInfoMapper.deleteLabelInfoById(Long.parseLong(labelId));
//                System.out.println("---------?????????"+labelId);
//            }
        }
        //???????????????????????????????????????total???????????????????????????????????????????????????userid????????????????????????????????????????????????????????????????????????????????????????????????????????????
        return ListResult.build(labelInfoDTOS, total);
    }

    /**
     * 1??????????????????????????????
     * 2????????????????????????????????????job?????????
     * 3?????????job??????????????????
     *
     * @param labelId
     * @throws Exception
     */
    @Override
    public void deleteLabelInfoById(String labelId, String custmkLabelId) throws Exception {


        //?????????????????????????????????

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
         * ??????????????????????????????????????????delete?????????????????????????????????????????????restTemplate.exchange()?????????
         */
        JSONObject responseBody = restTemplate.exchange(usermkLabelRemoveConfig.getUrl(), HttpMethod.DELETE, labelQueryDTOHttpEntity, JSONObject.class).getBody();
        String queryResponseCode = responseBody.getString("code");
        if (!"0000".equals(queryResponseCode)) { // ?????????0000????????????
            throw new Exception("??????????????????,?????????" + responseBody.getString("msg"));
        }

        //????????????????????????
        labelInfoMapper.deleteLabelInfoById(Long.valueOf(labelId));

        //??????job??????????????????
        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.valueOf(labelId));
        String jobCronExpression = labelJobSheduleDO.getJobCronExpression();
        //???????????????????????????????????????????????????
        if (!"ONCE".equals(jobCronExpression)) {
            String qrtzJobName = labelJobSheduleDO.getQrtzJobName();
            String qrtzTriggerName = labelJobSheduleDO.getQrtzTriggerName();
            //????????????????????????
            schedulerDelete(qrtzJobName, qrtzTriggerName);
        }

        //??????job_schedule?????????
        labelInfoMapper.removeJobScheduleByLabelId(Long.valueOf(labelId));
    }

    /**
     * ?????????????????????
     * 1?????????????????????????????????
     * 2????????????????????????
     *
     * @param labelId
     * @throws Exception
     */
    @Override
    public void pauseLabelTaskById(String labelId) throws Exception {
        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.valueOf(labelId));
        if ("ONCE".equals(labelJobSheduleDO.getJobCronExpression())) {
            throw new Exception("???????????????????????????????????????");
        }
        schedulerDelete(labelJobSheduleDO.getQrtzJobName(), labelJobSheduleDO.getQrtzTriggerName());
        labelInfoMapper.updateLabelJobScheduleStatusById(Long.valueOf(labelJobSheduleDO.getId()), 0);
    }

    /**
     * ?????????????????????
     * 1??????????????????????????????
     * 2?????????????????????????????????????????????
     * 3?????????job_schedule?????????
     *
     * @param labelId
     * @throws Exception
     */
    @Override
    public void recoverLabelTaskById(String labelId) throws Exception {


        LabelJobSheduleDO labelJobSheduleDO = labelInfoMapper.findJobScheduleByLabelId(Long.valueOf(labelId));
        if (1 == labelJobSheduleDO.getJobStatus()) {
            throw new Exception("??????????????????????????????????????????");
        }
        if ("ONCE".equals(labelJobSheduleDO.getJobCronExpression())) {
            throw new Exception("????????????????????????????????????????????????");
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
            throw new Exception("???????????????????????????????????????");
        } else if (-1 == jobProcessStatus) {
            throw new Exception("??????????????????????????????????????????????????????");
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
            modifyJobProcessStatus(jobScheduleId, "1"); //???????????????????????????
            if (custIds.size() != 0) {
                saveCustLabel(custIds, labelDTOs, gateway);
            }
            modifyJobProcessStatus(jobScheduleId, "2"); //???????????????????????????
            return true;
        } catch (Exception e) {
            modifyJobProcessStatus(jobScheduleId, "-1"); //???????????????????????????
            throw new Exception("???????????????");
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
                throw new Exception("------???????????? ?????? ??????????????????");
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
            //????????? startTime~endTime
            if (dateFields.contains(labelConditionDTO.getName())) {
                conditionValues = Arrays.asList(labelConditionDTO.getValue().split("-"));
                if (conditionValues.size() < 2) {
                    throw new Exception(" ??????????????????????????????-??????????????????????????? 20210501~20210601");
                }
                flag = true;
            } else {
                //????????????????????????????????? ??????&?????????
                conditionValues = Arrays.asList(labelConditionDTO.getValue().split("&"));
            }
            if (StringUtils.isBlank(targetField) || StringUtils.isBlank(relation) || conditionValues.size() == 0) {
                throw new RuntimeException(String.format("???????????????????????????, targetField:%s,relation:%s,conditionValues.size():%s", targetField, relation, conditionValues.size()));
            }
            String valueStrs = null;
            if ("IN".equals(relation.trim().toUpperCase()) || "NOT IN".equals(relation.trim().toUpperCase())) {
                valueStrs = conditionValues.stream().collect(Collectors.joining("','", "('", "')"));
                conditionFieldAndValues.append(" AND ").append(targetField).append(" ").append(relation).append(" ").append(valueStrs);
            } else if ("=".equals(relation.trim())) {
                //?????????????????????????????????
                if (flag) {
//                    conditionFieldAndValues.append(" AND ").append(" toYYYYMM(").append(targetField).append(") ").append(" >= ").append(conditionValues.get(0)).append(" AND ").append("toYYYYMM(").append(targetField).append(") ").append(" <= ").append(conditionValues.get(1));
                    conditionFieldAndValues.append(" AND ").append(targetField).append(" >= '").append(conditionValues.get(0)).append("' AND ").append(targetField).append(" <= '").append(conditionValues.get(1)).append("' ");
                } else {
                    //??????????????????????????????
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
     * ?????????????????????job?????????????????????????????????????????????labelSQL???cronExpression???
     * ???????????????jobIdentity ??????????????????triggerIdentity????????????????????????
     * ?????????????????????identity??????????????????job???trigger;
     *
     * @param cronExpression
     * @param jobScheduleDTO
     * @param jobIdentity
     * @param triggerIdentity
     * @throws Exception
     */
    private void schedulerAdd(String cronExpression, JobScheduleDTO jobScheduleDTO, String jobIdentity, String
            triggerIdentity) throws Exception {
        //???????????????
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
     * ??????????????????
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
            throw new Exception(String.format(" ???????????????????????????jobName:%s--------triggerName:%s", jobName, triggerName));
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
                "      \"PlatformName\": \"????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????\",\n" +
                "          \"Code\": \"OP.WEB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"OA\",\n" +
                "      \"PlatformName\": \"?????????????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"????????????App\",\n" +
                "          \"Code\": \"OP.APP\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"YP\",\n" +
                "      \"PlatformName\": \"??????????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????\",\n" +
                "          \"Code\": \"PORTAL.WEB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"YA\",\n" +
                "      \"PlatformName\": \"?????????????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"????????????App\",\n" +
                "          \"Code\": \"PORTAL.APP\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"????????????APP\",\n" +
                "          \"Code\": \"YOULU.TEACHER.APP\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"TM\",\n" +
                "      \"PlatformName\": \"??????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????????????????\",\n" +
                "          \"Code\": \"TMALL.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????????????????\",\n" +
                "          \"Code\": \"YOULU.TMALL.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????????????????\",\n" +
                "          \"Code\": \"GEEDU.TMALL.MAIN\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"JD\",\n" +
                "      \"PlatformName\": \"??????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????????????????\",\n" +
                "          \"Code\": \"JD.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????????????????\",\n" +
                "          \"Code\": \"YOULU.JD.MAIN\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????????????????\",\n" +
                "          \"Code\": \"YOULU.JD.OFFICIAL\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????????????????\",\n" +
                "          \"Code\": \"YOULU.JD.BOOK\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"DA\",\n" +
                "      \"PlatformName\": \"??????????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"????????????-??????\",\n" +
                "          \"Code\": \"CAP.DA.ECOMMERCE\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"EC\",\n" +
                "      \"PlatformName\": \"????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"????????????Web\",\n" +
                "          \"Code\": \"EC.WEB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"WMP\",\n" +
                "      \"PlatformName\": \"???????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"????????????\",\n" +
                "          \"Code\": \"YOULU.WX.ZONGHEBEIKAO\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"????????????\",\n" +
                "          \"Code\": \"YOULU.WX.YIWEI\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????\",\n" +
                "          \"Code\": \"YOULU.WX.FAKAO\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????\",\n" +
                "          \"Code\": \"YOULU.WX.JIAOSHI\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"XET\",\n" +
                "      \"PlatformName\": \"?????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????\",\n" +
                "          \"Code\": \"XET.HQYL\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"???????????????\",\n" +
                "          \"Code\": \"XET.YLTEAC\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"????????????\",\n" +
                "          \"Code\": \"XET.YLEDU\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"????????????\",\n" +
                "          \"Code\": \"XET.YLFK\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????\",\n" +
                "          \"Code\": \"XET.YIWEICOLLEGE\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"????????????\",\n" +
                "          \"Code\": \"XET.YLKAOYAN\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"YOUZAN\",\n" +
                "      \"PlatformName\": \"??????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????????????????\",\n" +
                "          \"Code\": \"YOUZAN.YWSYB\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"PlatformCode\": \"MC\",\n" +
                "      \"PlatformName\": \"????????????\",\n" +
                "      \"PlatformApps\": [\n" +
                "        {\n" +
                "          \"Name\": \"??????\",\n" +
                "          \"Code\": \"MC.GROUPBUYING\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        System.out.println(JSONObject.toJSON(str));
    }
}
