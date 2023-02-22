package com.clsz.ai_model_s2s_all_game.utils;


import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private final static int READ_TIMEOUT = 1000;
    private final static int CONNECT_TIMEOUT = 600;
    private final static int WRITE_TIMEOUT = 600;

    public static OkHttpClient getHttpClient() {

        OkHttpClient client = new OkHttpClient();
        //读取超时
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        //连接超时
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        //写入超时
        client.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        //自定义连接池最大空闲连接数和等待时间大小，否则默认最大5个空闲连接
        client.setConnectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES));

        return client;
    }




}
