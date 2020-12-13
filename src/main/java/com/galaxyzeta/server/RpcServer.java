package com.galaxyzeta.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.galaxyzeta.common.annotation.RpcServiceComponent;
import com.galaxyzeta.common.zookeeper.CuratorConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RpcServer implements ApplicationContextAware, DisposableBean {

	private int serverPort;

	private int serverTimeout;

	private static final Logger LOG = LoggerFactory.getLogger(RpcServer.class);

	private ServiceRegistry registry;

	public RpcServer(String serverAddress, int serverPort, int serverTimeout, CuratorConfig curatorConfig) {
		this.serverPort = serverPort;
		this.serverTimeout = serverTimeout;
		registry = new ServiceRegistry(serverAddress, serverPort, curatorConfig);
	}

	public void start() throws Exception {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new RpcServerHandlerInitializer(registry, serverTimeout));
			LOG.info("Server started...");

			registry.publishService();

			ChannelFuture future = bootstrap.bind(serverPort).sync();
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	@Override
	public void destroy() throws Exception {

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Map<String, Object> defs = applicationContext.getBeansWithAnnotation(RpcServiceComponent.class);
		Iterator<Entry<String, Object>> iter = defs.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Object> ent = iter.next();

			Object serviceGroupObject = ent.getValue();
			Class<?> implClass = serviceGroupObject.getClass();

			RpcServiceComponent serviceAnnotation = implClass.getAnnotation(RpcServiceComponent.class);
			Class<?> interfaceClass = serviceAnnotation.interfaceClass();
			String version = serviceAnnotation.version();
			registry.addService(interfaceClass.getName(), version, serviceGroupObject);
		}
	}
}
