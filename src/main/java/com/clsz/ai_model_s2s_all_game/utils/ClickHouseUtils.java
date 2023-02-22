package com.clsz.ai_model_s2s_all_game.utils;

import com.clsz.ai_model_s2s_all_game.entity.UserSignals;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHousePreparedStatement;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Component
public class ClickHouseUtils implements CKUtils {

    private ClickHousePool clickHousePool = new ClickHousePool();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean insert(String sql, String... params) {
        Connection connection = clickHousePool.getConnection();
        boolean b = false;
        ClickHousePreparedStatement pst = null;
        if (connection == null) {
            System.out.println("connection is empty");
        }
        try {
            pst = (ClickHousePreparedStatement) connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }
            b = pst.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            clickHousePool.releaseConnection(connection);
        }
        return b;
    }


    @Override
    public boolean delete(String sql, String... params) {
        Connection connection = clickHousePool.getConnection();
        return false;
    }

    @Override
    public ResultSet queryResultSet(String sql, String... params) {
        Connection connection = clickHousePool.getConnection();
        ResultSet rst = null;
        ClickHousePreparedStatement pst = null;
        if (connection == null) {
            System.out.println("connection is empty");
        }
        try {
            pst = (ClickHousePreparedStatement) connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }
            rst = pst.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            clickHousePool.releaseConnection(connection);
        }
        return rst;
    }

    @Override
    public int insertBatch(String sql, List<UserSignals> users) {
        Connection connection = clickHousePool.getConnection();
        int[] executeBatch = null;
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (UserSignals user : users) {
                ps.setString(1, user.getAdid());
                ps.setString(2, user.getDevice());
                ps.setString(3, user.getRegist());
                ps.setString(4, user.getLabel());
                ps.setFloat(5, user.getPredicted_prob());
                ps.setString(6, user.getDeviceid());
                ps.setFloat(7, user.getWeight());
                ps.setFloat(8, user.getPayamounts());

                ps.setString(9, user.getApp_id());
                ps.setString(10, dateFormat.format(new Date()));
                ps.addBatch();
            }
            executeBatch = ps.executeBatch();
            ps.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            clickHousePool.releaseConnection(connection);
        }
        return executeBatch != null ? executeBatch.length : 0;
    }


}
