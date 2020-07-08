package com.fs;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
	
	
	public static void main(String[] args) {
		System.setProperty("configName", "applicationContext");
		ApplicationContext context = new ClassPathXmlApplicationContext("/test/${configName}.xml");
        System.out.println(context.getBean("demoController"));
        System.out.println(context.getBean("demo2ServiceImpl"));
	}

}
