package com.clsz.ai_model_s2s_all_game.utils;

import com.clsz.ai_model_s2s_all_game.entity.UserSignals;

import java.sql.ResultSet;
import java.util.List;

public interface CKUtils {

    public boolean insert(String sql, String... params);

    public boolean delete(String sql, String... params);

    public ResultSet queryResultSet(String sql, String... params);

    public int insertBatch(String sql, List<UserSignals> users);

}