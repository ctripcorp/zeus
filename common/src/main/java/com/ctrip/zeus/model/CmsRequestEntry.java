package com.ctrip.zeus.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by zhoumy on 2016/7/29.
 */
public class CmsRequestEntry {
    private String accessToken;
    private Object requestBody;

    private static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public String toJsonString() {
        try {
            String body = requestBody instanceof String ? requestBody.toString() : objectMapper.writeValueAsString(requestBody);
            return String.format("{\"access_token\":\"%s\",\"request_body\":%s}", accessToken, body);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
