package com.example.simplenettyrpc.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * RPC Server底层是基于Netty的Server
 */
public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private String host;
    private int port;
    private ServerBootstrap serverBootstrap;
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
        logger.info("RPC service add to map, rpcServiceKey:{}, rpcService:{}", rpcServiceKey, rpcService);
    }

    /**
     * 启动NettyServer
     */
    public void start() {
        logger.info("RPC Server starting...");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建一个boss group
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 默认创建 CPU核心 * 2 个worker group

        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerChannelInitializer(this.rpcServiceMap))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
            logger.info("Bind host:{}, port:{}, started", host, port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("start netty server exception: " + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("RPC Server shutdown gracefully");
        }
    }

    /**
     * 关闭NettyServer
     */
    public void stop() {
        try {
            EventLoopGroup eventLoopGroup = serverBootstrap.group();
            if (!eventLoopGroup.isShutdown() || !eventLoopGroup.isTerminated()) {
                //优雅关闭，2s确保正在执行的task被提交
                Future<?> future = eventLoopGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
                if (future.isSuccess()) {
                    serverBootstrap = null; //置为null，方便GC
                    logger.info("RPC Server shutdown gracefully");
                    return;
                }
                logger.error("ServerBootstrap shutdown gracefully fail");
            }
        } catch (Exception e) {
            logger.error("RPC Server stop fail: " + e.getMessage());
        }
    }
}
