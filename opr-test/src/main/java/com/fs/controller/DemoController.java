package com.fs.controller;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fs.pojo.User;
import com.fs.service.Demo1Service;
import com.fs.service.Demo2Service;

@RestController
@RequestMapping("/demo")
public class DemoController implements ApplicationContextAware,BeanNameAware{
	
//	@Autowired
//	private Demo1Service demo1Service;
//	@Autowired
//	private Demo2Service demo2Service;
	private String beanName;
	private ApplicationContext applicationContext;


	@RequestMapping("/hello")
	public String hello() {
//		System.out.println("获取到当前bean名称为：" + beanName);
//		String[]  beanDefinitionNames = applicationContext.getBeanDefinitionNames();
//		for(String name : beanDefinitionNames) {
//			System.out.println("IOC容器里面有的bean："+name);
//		}
		return "hello spring yuanma";
	}
	
	@RequestMapping("/helloArgs")
	public String helloArgs(@RequestBody User user) {
//		System.out.println("获取到当前bean名称为：" + beanName);
//		String[]  beanDefinitionNames = applicationContext.getBeanDefinitionNames();
//		for(String name : beanDefinitionNames) {
//			System.out.println("IOC容器里面有的bean："+name);
//		}
		return "MY NAME is :" + user.getName();
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
