package com.example.simplenettyrpc.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端业务处理器
 */
public class UserInfoHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(UserInfoHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 服务端获取客户端发送来的消息，服务端调用本地服务
        logger.info("client request msg: " + msg.toString());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("UserInfoHandler caught exception: " + cause.getMessage());
        ctx.close();
    }
}
