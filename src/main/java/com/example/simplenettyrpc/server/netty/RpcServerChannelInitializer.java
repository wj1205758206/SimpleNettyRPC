package com.example.simplenettyrpc.server.netty;

import com.example.simplenettyrpc.domain.UserInfo;
import com.example.simplenettyrpc.util.codec.CustomDecoder;
import com.example.simplenettyrpc.util.codec.CustomEncoder;
import com.example.simplenettyrpc.util.codec.RpcRequest;
import com.example.simplenettyrpc.util.codec.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * worker group中的socketChannel初始化器，同时给socketChannel添加必要的handler处理器
 */
public class RpcServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> rpcServiceMap;

    public RpcServerChannelInitializer(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //添加channel空闲处理器
        pipeline.addLast(new IdleStateHandler(3, 3, 5, TimeUnit.SECONDS));
        //添加自定义的RpcRequest请求和RpcResponse响应的编解码器，同时解决TCP粘包和拆包问题
        pipeline.addLast(new CustomDecoder(RpcRequest.class));
        pipeline.addLast(new CustomEncoder(RpcResponse.class));
        //添加Server端业务处理器,用来处理所有的rpc service
        pipeline.addLast(new RpcServerHandler(rpcServiceMap));
    }
}
