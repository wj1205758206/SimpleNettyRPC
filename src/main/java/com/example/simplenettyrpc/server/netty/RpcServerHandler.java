package com.example.simplenettyrpc.server.netty;

import com.example.simplenettyrpc.util.codec.CustomDecoder;
import com.example.simplenettyrpc.util.codec.RpcRequest;
import com.example.simplenettyrpc.util.codec.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 * RPC request 业务处理器
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private Map<String, Object> rpcServiceMap;
    private ThreadPoolExecutor poolExecutor; //使用线程池，多线程异步处理Map集合中的所有请求

    public RpcServerHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
        this.poolExecutor = initPool();
    }

    /**
     * 业务逻辑处理
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("RpcServer receive request: " + msg.getId() + ", body: " + msg.toString());
                RpcResponse response = new RpcResponse();
                try {
                    response.setId(msg.getId());
                    Object result = doHandler(msg); //调用doHandler方法执行具体的业务处理
                    response.setStatusCode(200);
                    response.setResult(result);
                } catch (Exception e) {
                    response.setErrorMsg(e.getMessage()); //如果发生异常，response还需要返回给client错误信息
                    response.setStatusCode(500); //错误响应码500
                    logger.error("RpcServer handler request exception: " + e.getMessage());
                }
                ChannelFuture channelFuture = ctx.writeAndFlush(response);
                //给异步返回结果channel future添加监听器，判断服务端是否成功发送给客户端response
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("RpcServer response is send, response: " + response.toString());
                    }
                });
            }
        });
    }

    /**
     * channelRead可以多次读取，channelReadComplete只会在全部读取完时调用一次
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ChannelId id = ctx.channel().id();
        logger.info("channel: {}, read complete", id.asLongText());
        super.channelReadComplete(ctx);
    }

    /**
     * 处理channel超时空闲，允许第一次读写超时，否则将超时关闭
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // channel初始化器pipeline中添加了空闲超时的handler
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT.equals(idleStateEvent.state())) {
                // channel第一次读空闲,暂时不关闭
                logger.info("channel first reader idle, continue to use");
            } else if (IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT.equals(idleStateEvent.state())) {
                // channel第一次写空闲,暂时不关闭
                logger.info("channel first writer idle, continue to use");
            } else if (IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT.equals(idleStateEvent.state())) {
                // channel第一次读写空闲,暂时不关闭
                logger.info("channel first reader and writer idle, continue to use");
            } else {
                // 如果不是第一次的读写空闲，则直接将channel关闭，不再使用
                ctx.channel().close(); //空闲超时，则关闭channel
                logger.warn("Channel idle timeout, has closed");
            }
        } else {
            //其他的事件交给父类处理
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("RpcServer caught exception: " + cause.getMessage());
        ctx.close();
        logger.error("RpcServer is closed");
    }

    /**
     * 执行具体的业务处理
     *
     * @param msg
     * @return
     */
    private Object doHandler(RpcRequest msg) {
        //获取RPC 请求协议的相关信息
        String id = msg.getId();
        String className = msg.getClassName();
        String methodName = msg.getMethodName();
        String version = msg.getVersion();
        Class<?>[] paramTypes = msg.getParamTypes();
        Object[] paramValues = msg.getParamValues();

        String rpcServiceKey = className; // 把接口名作为rpcService的key
        if (version != null && !version.isEmpty()) { // 如果指定了版本号，则把 接口名+$+版本号 作为rpcService的Key
            rpcServiceKey = rpcServiceKey + "$" + version;
        }
        if (!rpcServiceMap.containsKey(rpcServiceKey)) {
            logger.error("Can not find rpc service: name={},version={}", className, version);
            return null;
        }
        Object serviceObject = rpcServiceMap.get(rpcServiceKey);
        logger.info("Find rpc service: " + serviceObject.toString());

        //利用JDK反射调用服务端的本地方法
        try {
            Class<?> clazz = serviceObject.getClass();
            Method method = clazz.getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(serviceObject, paramValues);
        } catch (Exception e) {
            logger.error("JDK Reflect exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * 创建一个线程池
     */
    private ThreadPoolExecutor initPool() {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                8,
                16,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "rpcServiceThread-" + r.toString());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy() //池满拒绝策略
        );
        return poolExecutor;
    }
}
