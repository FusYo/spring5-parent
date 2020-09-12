package com.fs;

import java.util.HashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fs.controller.DemoController;
import com.fs.event.Event;
import com.fs.event.EventListener;
import com.fs.event.EventSource;
import com.fs.event.OneEventListener;
import com.fs.event.TwoEventListener;
import com.fs.event.spring.DiscountCouponListener;
import com.fs.event.spring.EmailListener;
import com.fs.event.spring.OrderEvent;
import com.google.common.collect.Maps;

@Configuration
@ComponentScan("com.fs")
public class Test {
	
	
	public static void main(String[] args) {
		//XML形式
		System.setProperty("configName", "applicationContext");
		//XML文件配置方式，执行refresh()将包下面加了注解的类都注册到容器中
//		ApplicationContext context = new ClassPathXmlApplicationContext("/test/${configName}.xml");
//        System.out.println(context.getBean("demoController"));
//        System.out.println(context.getBean("demo2ServiceImpl"));
//        
//        
//        System.out.println("获取工厂修饰的对象："+context.getBean("userFactoryBean"));
//        System.out.println("获取工厂对象本身："+context.getBean("&userFactoryBean"));
        
         //注解形式
		 //扫描包的方式，在执行refresh()前会将所有包下面加了注解的类都注册到容器中
//		 ApplicationContext context = new AnnotationConfigApplicationContext("com.fs");
		 //加载类的方式，在执行refresh()前只会将当前加了配置注解的类（Test类，因为这个时候还没有开始扫描包）注册到容器中，refresh()方法中对包路径下面加了注解的业务类再注册到容器
		 //ApplicationContext context = new AnnotationConfigApplicationContext(Test.class);
//		 System.out.println(context.getBean("demoController"));
//         System.out.println(context.getBean("demo2ServiceImpl"));
//         System.out.println("CustomBeanDefinitionRegistryPostProcessor后置处理器创建的Bean对象："+context.getBean("customUser"));
//		 DemoController demoController =  context.getBean(DemoController.class);
//		 System.out.println(demoController.hello());
		 
		
		//测试事件监听模式
//		Event event = new Event();
//		event.setType("one");
//		EventSource es = new EventSource();
//		EventListener one = new OneEventListener();
//		es.registry(one);
//		EventListener two = new TwoEventListener();
//		es.registry(two);
//		es.publishEvent(event);
//		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.fs");
//		 //增加监听器
//	     context.addApplicationListener(new EmailListener());
//	     context.addApplicationListener(new DiscountCouponListener());
//		 HashMap<Object, Object> map = Maps.newHashMap();
//	     map.put("account", "1001");//用以获取业务中需要的信息，比如电话号、邮箱、订单信息等等
//		 OrderEvent event = new OrderEvent(map);
//		 context.publishEvent(event);
		
		 
	}

}
