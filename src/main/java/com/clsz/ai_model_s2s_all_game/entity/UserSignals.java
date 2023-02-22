package com.clsz.ai_model_s2s_all_game.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSignals {

    private String adid;
    private String device;
    private String regist;
    private String label;
    private float predicted_prob;
    private String deviceid;
    private float weight;
    private float payamounts;

    private String idfa;
    private String gps_adid;
    private String event_token;
    private String app_token;
    private int s2s;
    private String ip_address;
    private Long created_at;

    private String app_id;

}
