package com.clsz.ai_model_s2s_all_game.config;


import com.clsz.ai_model_s2s_all_game.service.AdjustCallbackActionMuyouService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {


    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

    @Autowired
    AdjustCallbackActionMuyouService adjustCallbackActionMuyouService;


    @Scheduled(cron = "1 0/15 * * * *")
    public void executeQueryReporting() {

        log.info("The time is now {}", dateFormat.format(new Date()));
        String result = adjustCallbackActionMuyouService.startCallback(dateFormat.format(new Date()));
        log.info(result);

    }
}
