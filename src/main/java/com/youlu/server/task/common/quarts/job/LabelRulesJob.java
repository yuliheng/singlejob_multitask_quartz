package com.youlu.server.task.common.quarts.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youlu.server.task.common.api.dto.JobScheduleDTO;
import com.youlu.server.task.common.api.dto.LabelCustBatchAddDTO;
import com.youlu.server.task.common.api.dto.LabelDTO;
import com.youlu.server.task.common.constant.HeaderEnum;
import com.youlu.server.task.common.quarts.defdatasource.SelfDataSource;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/15 16:28
 */
@Slf4j
@Data
@DisallowConcurrentExecution
public class LabelRulesJob implements Job {

    private static ExecutorService executors = Executors.newFixedThreadPool(10);


    private static int threshold = 1000000; //允许list存储的数据容量；

    private static int batchSize = 5000; //每个线程每批处理数据量；

    private static DataSource ch_datasource = SelfDataSource.init().getCataSourceCH();

    private static DataSource mysql_datasource = SelfDataSource.init().getCataSourceMYSQL();

    /**
     * 逻辑：
     * 1、设置该labelid对应job状态为处理中；     2：已完成；1：处理中；0：未处理；-1：失败)
     * 2、开始处理cust 标签；
     * 3、处理完，设置标签对应状态为已完成，否则为失败；
     *
     * @param jobExecutionContext
     */
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        /**
         * 获取job 设置时 的key传递的值
         *  jobDataMap.put("jobScheduleDTO", jobScheduleDTO);
         */
        JobDataMap mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
        JobScheduleDTO jobScheduleDTO = (JobScheduleDTO) mergedJobDataMap.get("jobScheduleDTO");
        String labelSQL = jobScheduleDTO.getLabelSQL();
        String jobScheduleId = jobScheduleDTO.getJobScheduleId();
        String gateway = jobScheduleDTO.getGateway();
        String remoteInterfaceLabelId = jobScheduleDTO.getRemoteInterfaceLabelId();
        String remoteInterfaceLabelName = jobScheduleDTO.getRemoteInterfaceLabelName();

        System.out.println("----------labelSQL:" + labelSQL);
        System.out.println("----------jobScheduleId:" + jobScheduleId);
        System.out.println("----------remoteInterfaceLabelId:" + remoteInterfaceLabelId);
        System.out.println("----------remoteInterfaceLabelName:" + remoteInterfaceLabelName);


        if (ch_datasource == null || mysql_datasource == null) {
            throw new SQLException("job任务无法获取数据库连接池");
        }
        //将该任务对应的状态设置为处理中
        modifyJobProcessStatus(jobScheduleId, "1");
        /**
         *  根据任务SQL查询clickhouse获取对应的custId信息
         *  当查询数据量达到threshold 时，开始执行多线程调用客户营销远程客户标签接口写入数据，每个线程batchSize数据量；
         *  最后未达到threashold的数据量，执行上一步操作；
         */


        List<LabelDTO> labelDTOS = new ArrayList<>();
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setId(remoteInterfaceLabelId);
        labelDTO.setName(remoteInterfaceLabelName);
        labelDTOS.add(labelDTO);

        try (Connection ckCon = ch_datasource.getConnection();
             PreparedStatement preparedStatement = ckCon.prepareStatement(labelSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            List<String> custIds = new ArrayList<>();
            while (resultSet.next()) {
                String custId = resultSet.getString(1);
                custIds.add(custId);
                int size = custIds.size();
                if (size >= threshold) {
                    saveCustLabel(custIds, labelDTOS,gateway);
                }
            }
            saveCustLabel(custIds, labelDTOS,gateway);
        } catch (Exception e) {
            modifyJobProcessStatus(jobScheduleId, "-1");
            e.printStackTrace();
        }
        modifyJobProcessStatus(jobScheduleId, "2");
        System.out.println("------------------处理完成"+new Date());
    }

    /**
     * @param custIds
     */
    public void saveCustLabel(List<String> custIds, List labels,String gateway) throws Exception {
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
            Future<Boolean> submit = executors.submit(() -> remoteSaveCustLableInterface(url,gateway,labelCustBatchAddDTO));
            futurns.add(submit);
            fromIndex = toIndex;
        }
        for (Future<Boolean> futurn : futurns) {
            Boolean aBoolean = futurn.get();
            if (!aBoolean) {
                throw new Exception("------批量添加 部分 客户标签失败");
            }
        }
        custIds.clear();
    }

    //bean转Map
    public static Map bean2Map(LabelCustBatchAddDTO labelCustBatchAddDTO) {
        String s = JSON.toJSONString(labelCustBatchAddDTO);
        JSONObject jsonObject = JSONObject.parseObject(s);
        Map<String, Object> map = (Map<String, Object>) jsonObject;
        return map;
    }

    /**
     * 客户营销批量添加客户标签 远程接口
     * @param url
     * @param labelCustBatchAddDTO
     * @return
     * @throws Exception
     */
    public Boolean remoteSaveCustLableInterface(String url,String gateway, LabelCustBatchAddDTO labelCustBatchAddDTO) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete();
        HttpPost post = new HttpPost(url);
        StringEntity paramEntity = new StringEntity(JSONObject.toJSONString(labelCustBatchAddDTO), "UTF-8");
        paramEntity.setContentType("application/json");
        post.setEntity(paramEntity);
        post.setHeader(HeaderEnum.ACCEPT.getValue(), "*/*");
        post.setHeader(HeaderEnum.AUTHORIZATION.getValue(), gateway);
        post.setHeader(HeaderEnum.CONTENTTYPE.getValue(), "application/json");
        post.setHeader(HeaderEnum.CHECKCODE.getValue(), "CT/Customer/Cmd/BatchAddLable");
        response = httpClient.execute(post);
        int code = response.getStatusLine().getStatusCode();
        if (code == 200) {
            HttpEntity entity = response.getEntity();
            String responseStr = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            System.out.println("----------jsonObject:----" + jsonObject);
            String dataCode = jsonObject.getString("code");
            if (!"0000".equals(dataCode)) {
//                throw new Exception("------批量添加客户标签失败");
                return false;
            }
        } else {
//            throw new Exception("------批量添加客户标签请求失败");
            return false;
        }
        return true;
    }


    public void modifyJobProcessStatus(String jobScheduleId, String status) throws Exception {

        try (Connection con = mysql_datasource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement("UPDATE label_auto_job_schedule SET job_process_status = ? WHERE id= ?")) {
            preparedStatement.setObject(1, Long.parseLong(status));
            preparedStatement.setObject(2, Long.parseLong(jobScheduleId));
            int updataRow = preparedStatement.executeUpdate();
            if (updataRow == 0) {
                throw new Exception("-------job: " + jobScheduleId + ":状态:{ " + status + " }更新失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
