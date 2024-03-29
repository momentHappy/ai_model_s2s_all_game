package com.clsz.ai_model_s2s_all_game.service.impl;

import com.clsz.ai_model_s2s_all_game.entity.UserSignals;
import com.clsz.ai_model_s2s_all_game.service.AbstractAdjustCallbackAction;
import com.clsz.ai_model_s2s_all_game.service.AdjustCallbackActionMuyouQingyunService;
import com.clsz.ai_model_s2s_all_game.utils.HttpUtils;
import com.squareup.okhttp.OkHttpClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdjustCallbackActionMuyouQingyunServiceImpl extends AbstractAdjustCallbackAction implements AdjustCallbackActionMuyouQingyunService {


    @Override
    public String startCallback(String date) {
        String user_signals = "/root/ai_ads/result/muyou_qingyun/output/" + date + "/signals.csv";
        String user_job = "/root/ai_ads/result/muyou_qingyun/output/" + date + "/job.done";
        String app_name = "'com.xxqy.gs'";
        String app_id = "";
        String app_token = "lj7p4xbj3uv4";
        String event_token = "jvn9ak";
        builders(user_signals, user_job, app_name, app_id, app_token, event_token);

        OkHttpClient client = HttpUtils.getHttpClient();
        try {
            //1.读取文件夹，查看是否存在数据
            List<UserSignals> userSignals = readFileExists();
            if (userSignals != null && userSignals.size() != 0) {

                ArrayList<UserSignals> batchInsertUserInfos = new ArrayList<>();
                //2.携带数据与CK比较去重，再查询用户信息
                List<UserSignals> userInfos = ckDuplicateRemoval(userSignals);
                for (UserSignals userInfo : userInfos) {
                    //3.数据上报
                    int message = reportMessage(client, userInfo);
                    if (message == 1) {
                        batchInsertUserInfos.add(userInfo);
                    }
                }
                //4.上报数据存库备份
                ckLoopbackBackup(batchInsertUserInfos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;

    }
}
