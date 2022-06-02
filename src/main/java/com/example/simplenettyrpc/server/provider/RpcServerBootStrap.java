package com.example.simplenettyrpc.server.provider;

import com.example.simplenettyrpc.server.netty.NettyServer;
import com.example.simplenettyrpc.util.annotation.EnableRpcService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * 服务端引导类，启动一个NettyServer作为服务提供者
 */
public class RpcServerBootStrap extends NettyServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    public RpcServerBootStrap(String host, int port) {
        super(host, port);
    }

    @Override
    public void destroy() throws Exception {
        //销毁Bean时，允许释放一些已打开的资源，调用stop方法先停止NettyServer
        super.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //当所有的属性完成赋值，以及final属性被初始化之后，调用start启动NettyServer
        super.start();
    }

    /**
     * RpcServerBootStrap实现了ApplicationContextAware接口
     * ApplicationContextAware作用是可以方便获取Spring容器ApplicationContext，进而可以获取到容器中的Bean
     * Spring容器会检测容器中的所有Bean，如果发现某个Bean实现了ApplicationContextAware接口，Spring容器会在创建该Bean之后，自动调用该Bean的setApplicationContextAware()方法，调用该方法时，会将容器本身作为参数传给该方法
     * 也就是说，spring 在启动的时候就需要实例化这个 class
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取所有标有@EnableRpcService注解的对象, 服务在启动的时候扫描得到所有的服务接口及其实现
        Map<String, Object> rpcServicesMap = applicationContext.getBeansWithAnnotation(EnableRpcService.class);
        if (!rpcServicesMap.isEmpty()) {
            // 遍历map中每一个rpcService，通过注解获取到调用的接口名和版本号，并根据接口名和版本号，将这个rpcService注册到NettyServer中
            for (Object rpcService : rpcServicesMap.values()) {
                EnableRpcService enableRpcService = rpcService.getClass().getAnnotation(EnableRpcService.class);
                String name = enableRpcService.value().getName();
                String version = enableRpcService.version();
                super.addService(name, version, rpcService);
            }
        }

    }
}
