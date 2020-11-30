package com.galaxyzeta.client;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import com.galaxyzeta.common.annotation.RpcAutowired;
import com.galaxyzeta.common.zookeeper.CuratorConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RpcClient implements ApplicationContextAware, DisposableBean {

	public ServiceDiscovery discovery;
	public RpcClient(CuratorConfig zkConfig) {
		discovery = new ServiceDiscovery(zkConfig);
		try {
			discovery.pullLatestService();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() throws Exception {

	}

	public Object createProxyObject(Class<?> interfaceClass, String version) {
		return Proxy.newProxyInstance(interfaceClass.getClassLoader(), 
			new Class<?>[]{interfaceClass}, 
			new ObjectProxy(interfaceClass, version));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for(String beanName: beanNames) {
			Object bean = applicationContext.getBean(beanName);
			Field[] fields = bean.getClass().getFields();
			for(Field field : fields) {
				RpcAutowired annotation = field.getDeclaredAnnotation(RpcAutowired.class);
				if(annotation != null) {
					try {
						field.set(bean, createProxyObject(field.getDeclaringClass(), annotation.version()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
