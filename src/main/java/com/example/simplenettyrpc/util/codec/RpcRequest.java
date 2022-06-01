package com.example.simplenettyrpc.util.codec;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 自定义RPC request请求协议
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -1654539347777758521L;

    // 我们通过自定义的RpcRequest请求协议，就可以知道client发送来的请求，想要调用Server端哪个类下的哪个方法，以及入参数据有哪些
    private String id; //请求id
    private String className; //请求的类名
    private String methodName; //请求的方法名
    private Class<?>[] paramTypes; //请求中的参数类型
    private Object[] paramValues; //请求中参数的值
    private String version; //请求想要调用服务的版本号

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getParamValues() {
        return paramValues;
    }

    public void setParamValues(Object[] paramValues) {
        this.paramValues = paramValues;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "id='" + id + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", paramValues=" + Arrays.toString(paramValues) +
                ", version='" + version + '\'' +
                '}';
    }
}
