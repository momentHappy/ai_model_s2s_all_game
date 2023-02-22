package com.clsz.ai_model_s2s_all_game.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class ClickHousePool {

    //    clickhouse配置
    private static final String clickhouse_driverName = "ru.yandex.clickhouse.ClickHouseDriver";
    private static final String clickhouse_url = "jdbc:clickhouse://13.213.128.17:8123/anfanapi?socket_timeout=300000";
    private static final String clickhouse_user = "bigdata";
    private static final String clickhouse_password = "novasmobi";

    private int maxConnections = 10; // 空闲池，最大连接数
    private int initConnections = 5;// 初始化连接数
    private int maxActiveConnections = 100;// 最大允许的连接数，和数据库对应
    private long connectionTimeOut = 1000 * 60 * 20;// 连接超时时间，默认20分钟

    //线程安全集合
    private List<Connection> freeConnectPool = new Vector<>();
    private List<Connection> activeConnectPool = new Vector<>();
    //计算最大连接数
    int count = 0;

    public ClickHousePool() {
        //初始化连接池
        initPool();
    }

    private void initPool() {
        for (int i = 0; i < initConnections; i++) {
            //新建连接
            Connection connection = createConnection();
            if (null != connection) {
                freeConnectPool.add(connection);
                count++;
            }
        }
    }

    private Connection createConnection() {
        /*
         * 注册jdbc驱动
         * */
        Connection connection = null;
        try {
            Class.forName(clickhouse_driverName);
            connection = DriverManager.getConnection(clickhouse_url, clickhouse_user, clickhouse_password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public synchronized Connection getConnection() {
        //空闲线程存在空闲连接
        Connection connection = null;
        if (freeConnectPool.size() > 0) {
            connection = freeConnectPool.remove(0);
            activeConnectPool.add(connection);
        } else {
            //判断当前线程连接数量是否达到最大值
            if (count < maxActiveConnections) {
                connection = createConnection();
                activeConnectPool.add(connection);
                count++;
            } else {
                try {
                    wait(connectionTimeOut);//活动线程已满等待解封
                    connection = getConnection();//递归调用
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return connection;
    }

    public synchronized void releaseConnection(Connection connection) {
        //如果空闲线程数已满，证明连接足够多
        if (freeConnectPool.size() < maxConnections) {
            freeConnectPool.add(connection);
        } else {
            try {
                //此时维持线程只有10个
                connection.close();
                activeConnectPool.remove(connection);
                count--;
                notifyAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
