package com.example.simplenettyrpc.util.codec;

import java.io.Serializable;

/**
 * 自定义的RPC response响应协议
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -3568239347777758521L;

    private String id; //响应id
    private Object result; //返回的响应结果
    private int statusCode; //返回的状态码
    private String errorMsg; //返回的错误信息

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "id='" + id + '\'' +
                ", result=" + result +
                ", statusCode=" + statusCode +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
