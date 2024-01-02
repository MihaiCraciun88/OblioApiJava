package com.obliosoftware;

import org.json.JSONObject;

public class AccessToken {
    public int request_time;
    public int expires_in;
    public String token_type;
    public String access_token;

    public AccessToken fromJsonString(String json)
    {
        JSONObject result = new JSONObject(json);
        access_token    = result.getString("access_token");
        expires_in      = result.getInt("expires_in");
        request_time    = result.getInt("request_time");
        token_type      = result.getString("token_type");
        return this;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("access_token", access_token);
        object.put("expires_in", expires_in);
        object.put("request_time", request_time);
        object.put("token_type", token_type);
        return object.toString();
    }
}
