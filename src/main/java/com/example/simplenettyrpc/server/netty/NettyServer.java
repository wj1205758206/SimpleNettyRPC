package com.example.simplenettyrpc.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC Server底层是基于Netty的Server
 */
public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private String host;
    private int port;
    private Map<String, Object> rpcServiceMap = new HashMap<>();

    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void addService(String name, String version, Object rpcService) {
        logger.info("add service, interface name:{}, version:{}, object:{}", name, version, rpcService);
        String rpcServiceKey = name; // 把接口名作为rpcService的key
        if (version != null && !version.isEmpty()) { // 如果指定了版本号，则把 接口名+$+版本号 作为rpcService的Key
            rpcServiceKey = rpcServiceKey + "$" + version;
        }
        rpcServiceMap.put(rpcServiceKey, rpcService);
    }

    private void start(String host, int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建一个boss group
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 默认创建 CPU核心 * 2 个worker group

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerChannelInitializer(this.rpcServiceMap))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("start netty server exception: " + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
