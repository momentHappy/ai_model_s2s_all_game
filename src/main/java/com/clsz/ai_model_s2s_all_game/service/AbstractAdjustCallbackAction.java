package com.clsz.ai_model_s2s_all_game.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.clsz.ai_model_s2s_all_game.entity.UserSignals;
import com.clsz.ai_model_s2s_all_game.service.impl.AdjustCallbackActionMuyouServiceImpl;
import com.clsz.ai_model_s2s_all_game.utils.CKUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractAdjustCallbackAction {

    private Logger logger = LoggerFactory.getLogger(AdjustCallbackActionMuyouServiceImpl.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String user_signals;
    private String user_job;

    private String app_name;
    private String app_id;
    private String app_token;
    private String event_token;

    protected void builders(String user_signals, String user_job, String app_name, String app_id, String app_token, String event_token) {
        this.user_signals = user_signals;
        this.user_job = user_job;
        this.app_name = app_name;
        this.app_id = app_id;
        this.app_token = app_token;
        this.event_token = event_token;
    }

    @Autowired
    CKUtils clickHouseUtils;

    /**
     * 读取文件夹，查看是否存在数据
     *
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    protected List<UserSignals> readFileExists() throws InterruptedException, IOException, ParseException {
        //等待文件生成
        for (int i = 0; i < 30; i++) {
            boolean job_done = new File(user_job).exists();
            if (job_done) {
                break;
            } else {
                logger.info(user_signals + " logger ------》  dir is not exits ---> " + i);
                Thread.sleep(10 * 1000);
            }
        }

        //判断数据文件在不在
        boolean sig_done = new File(user_signals).exists();
        if (!sig_done) {
            return null;
        }

        BufferedReader textFile = new BufferedReader(new FileReader(new File(user_signals)));

        ArrayList<UserSignals> signals = new ArrayList<>();
        String lineDta = "";
        textFile.readLine();
        while ((lineDta = textFile.readLine()) != null) {
            String[] split = lineDta.split(",", -1);
            String regist = "tracable".equals(split[2]) || "".equals(split[2]) ? new Date().getTime() + "" : dateFormat.parse(split[2]).getTime() + "";

            UserSignals build = UserSignals.builder()
                    .adid(split[0])
                    .device(split[1])
                    .regist(regist)
                    .label(split[3])
                    .predicted_prob(Float.parseFloat(split[4]))
                    .deviceid(split[5])
                    .weight(Float.parseFloat(split[6]))
                    .payamounts(Float.parseFloat(split[7]))
                    .build();
            signals.add(build);
        }

        logger.info("readFileExists ---> " + signals.size());
        textFile.close();
        return signals;
    }


    /**
     * 数据进行去重，去重后数据查询用户信息
     *
     * @param
     * @return
     */
    protected List<UserSignals> ckDuplicateRemoval(List<UserSignals> userSignals) throws SQLException {
        //1.数据去重
        String param = userSignals.stream()
                .map(UserSignals::getAdid)
                .collect(Collectors.joining("','"));
        String sql = "select adid from ai_signals_user_callback_adjust where adid in ('" + param + "') and app_id in ( " + app_name + "  ) ";

        logger.info(sql);

        ArrayList<String> result = new ArrayList<>();

        ResultSet rs = clickHouseUtils.queryResultSet(sql);
        while (rs.next()) {
            String id = rs.getString(1);
            result.add(id);
        }
        HashMap<String, UserSignals> map = new HashMap<>();
        for (UserSignals user : userSignals) {
            if (!result.contains(user.getAdid())) {
                map.put(user.getAdid(), user);
            }
        }

        //2.查询详细信息
        String param2 = map.keySet()
                .stream()
                .collect(Collectors.joining("','"));
        ArrayList<UserSignals> infos = new ArrayList<>();

        if (StringUtils.isBlank(param2)) {
            return infos;
        }

        String selectUserInfoSql = "SELECT adid, MAX(idfa), MAX(gps_adid), MAX(create_time), MAX(app_name) FROM ai_adjust_event_log WHERE adid IN ( '" + param2 + "' ) GROUP BY adid ";
        ResultSet selectUserInfoRs = clickHouseUtils.queryResultSet(selectUserInfoSql);
        while (selectUserInfoRs.next()) {
            UserSignals signals = map.get(selectUserInfoRs.getString(1));

            signals.setIdfa(selectUserInfoRs.getString(2));
            signals.setGps_adid(selectUserInfoRs.getString(3));
            signals.setCreated_at(selectUserInfoRs.getLong(4));
            signals.setApp_id(selectUserInfoRs.getString(5));
            signals.setEvent_token(event_token);
            signals.setApp_token(app_token);
            signals.setIp_address("");
            signals.setS2s(1);

            infos.add(signals);
        }

        logger.info(map.size() + "");
        logger.info("param2 ---> " + param2);
        logger.info(selectUserInfoSql);
        logger.info("duplicate removal --------> " + infos.size());
        return infos;
    }


    /**
     * 用户数据上报
     */
    protected int reportMessage(OkHttpClient client, UserSignals userInfo) throws IOException {
        String path = "https://s2s.adjust.com/event" +
                "?idfa=" + userInfo.getIdfa() +
                "&gps_adid=" + userInfo.getGps_adid() +
                "&event_token=" + userInfo.getEvent_token() +
                "&app_token=" + userInfo.getApp_token() +
                "&s2s=" + userInfo.getS2s() +
                "&created_at=" + userInfo.getCreated_at() +
                "&adid=" + userInfo.getAdid() +
                "&ip_address=" + userInfo.getIp_address();
        Request request = new Request.Builder().url(path).build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String resJson = response.body().string();

            logger.info("response ====>>>  " + resJson);

            JSONObject jsonObject = JSON.parseObject(resJson);
            if ("OK".equals(jsonObject.getString("status"))) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * CK去重备份
     *
     * @param users
     */
    protected void ckLoopbackBackup(List<UserSignals> users) {
        String sql = "insert into ai_signals_user_callback_adjust (adid, device, regist, label, predicted_prob, deviceid, weight, payamounts, app_id, update_ts) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
        int batch = clickHouseUtils.insertBatch(sql, users);
        logger.info("ckLoopbackBackup ---> " + batch);
    }


}
