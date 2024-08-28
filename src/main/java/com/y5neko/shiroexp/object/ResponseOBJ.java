package com.y5neko.shiroexp.object;

import okhttp3.Headers;

/**
 * Response类，用以完成Web请求接收响应内容
 */
public class ResponseOBJ {
    private byte[] response;
    private Headers headers;
    private int statusCode;

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }
}
