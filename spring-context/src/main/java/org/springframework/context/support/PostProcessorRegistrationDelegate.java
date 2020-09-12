/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 * 委派模式中的委派者
 * 负责委派各种后置处理器处理各种工作
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	//执行BeanFactory级别的后置处理器
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// 1. 处理用户自己的后置处理器,处理的是用户自己创建对象传的ac.addBeanFactoryPostProcessor()
		//如果你想在扫描所有包之前就执行你的后置处理器的话,可以把后置处理器注册该Spring先,就是通过 ac.addBeanFactoryPostProcessor(对象) 这样子注解
		Set<String> processedBeans = new HashSet<>();
           //判断当前的beanFactory是否是BeanDefinitionRegistry
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			//定义两个集合BeanDefinitionRegistryPostProcessor继承了BeanFactoryPostProcessor
			//bdrpp 增强了一个功能 ,也就是说他们的功能是不一样的,所以需要定义两个集合
			//regularPostProcessors 存储用户自己传进来的BeanFactoryPostProcessor的实现类
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			//registryProcessors存储用户自己传进来的BeanDefinitionRegistryPostProcessor的实现类
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
			//beanFactoryPostProcessors为[]，直接跳过，往下执行
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				//判断的bean是否实现了BeanDefinitionRegistryPostProcessor
				//如果bean实现了BeanDefinitionRegistryPostProcessor这个接口,可以获取到整个容器对象,也就是registry对象
				//而实现BeanFactoryPostProcessor,只能获取到bean工厂对象
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					//如果当前的bean实现的是BeanDefinitionRegistryPostProcessor
					//添加进registryProcessors集合
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					//如果只是实现了beanFactoryPostProcessors添加进regularPostProcessors集合
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.

			//2. 处理Spring内部的后置处理器,就只有一个类实现了BeanDefinitionRegistryPostProcessor,也即是ConfigurationClassPostProcessor
			//currentRegistryProcessors存放Spring内部类实现了BeanDefinitionRegistryPostProcessor接口的类
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			//从beanFactory工厂中寻找判断哪个类型实现了BeanDefinitionRegistryPostProcessor接口,拿出该类的beanName
			//[org.springframework.context.annotation.internalConfigurationAnnotationProcessor]
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			//遍历所有的后置处理器beanName ,通过beanName从bean工厂中把对象取出来
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					//初始化操作激活后置处理器
					//beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class)------》org.springframework.context.annotation.ConfigurationClassPostProcessor@272113c4
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					//把名称存储到processedBeans集合中
					processedBeans.add(ppName);
				}
			}
			//排序,不是重点 ,内部只有一个
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			//合并list ,把 用户之间定义和Spring之间内部的合并在一个集合中
			registryProcessors.addAll(currentRegistryProcessors);

            //执行当前的后置处理器,其实到这里currentRegistryProcessors只有一个处理器
			//主要处理扫描配置类对应包下的类，及加载自定义的BeanDefinitionRegistryPostProcessor后置处理器，注册bean到容器中
			//这里也会执行AOP的@EnableAspectJAutoProxy中的AspectJAutoProxyRegistrar类，将org.springframework.aop.config.internalAutoProxyCreator注册到IOC容器中
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			//清空集合
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			//其实就是再一次获取 Spring内部实现了 BeanDefinitionRegistryPostProcessor接口的类
			//其实担心上面的代码执行完了,可能有动态地又添加了实现BeanDefinitionRegistryPostProcessor的类
			//其实是几乎不可能的,Spring保障一点嘛
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				//不会进去的 因为上面 我们 processedBeans.add(ppName)
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			//排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
	     //  和上面上面是一个道理
			//最后，调用所有其他beandefinitionregistrypostprocessor，直到没有其他的出现为止
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				//[org.springframework.context.annotation.internalConfigurationAnnotationProcessor, customBeanDefinitionRegistryPostProcessor]
				for (String ppName : postProcessorNames) {
					//processedBeans == [org.springframework.context.annotation.internalConfigurationAnnotationProcessor]
					if (!processedBeans.contains(ppName)) {
						//这里customBeanDefinitionRegistryPostProcessor自定义的BeanDefinitionRegistryPostProcessor开始执行
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				//这里会将自定义的BeanDefinitionRegistryPostProcesso设置的类注册到容器中
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			//我们处理实现了 BeanDefinitionRegistryPostProcessor的ConfigurationClassPostProcessor类里面的postProcessBeanDefinitionRegistry方法
			//还没有处理postProcessBeanFactory这个方法
			//开始cglib
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			//regularPostProcessors是没有任何值的
			//因为在上面的代码中要是实现了 BeanDefinitionRegistryPostProcessor的就存储在 registryProcessors集合中
			//只有仅仅实现了BeanFactoryPostProcessor才会存储在regularPostProcessors这个集合中
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		
		//[org.springframework.context.annotation.internalConfigurationAnnotationProcessor, 
		//org.springframework.context.event.internalEventListenerProcessor, customBeanDefinitionRegistryPostProcessor]
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		//下面的代码执行用户自己定义的后置处理器,重点在这里
		//因为所有的包都被扫描出来了,我们用户自定义的BeanFactoryPostProcessors后置处理器可以开始执行了
		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		//1.实现了PriorityOrdered的
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		//2.实现了Ordered的
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		//3.只是单纯实现BeanFactoryPostProcessors.的
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		//org.springframework.context.event.internalEventListenerProcessor：在初始化之前执行后置处理器。。。
		//org.springframework.context.event.internalEventListenerFactory：在初始化之前执行后置处理器。。。
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		//从Spring内部获取查看哪些类是实现了BeanPostProcessor的,内部查找到两个
		// AutoWiredAnnotationBeanPostProcessor   CommonAnnotationBeanPostProcessor
        //如果加了启用AOP的注解@EnableAspectJAutoProxy 在这里也会出现AnnotationAwareAspectAutoProxyCreator
		//postProcessorNames = [org.springframework.context.annotation.internalAutowiredAnnotationProcessor, 
		// org.springframework.context.annotation.internalCommonAnnotationProcessor, customBeanPostProcessor]
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		//注册BeanPostProcessorChecker，当bean在BeanPostProcessor实例化过程中创建时，即当一个bean不能被所有BeanPostProcessor处理时，记录一个信息消息
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		//在实现优先排序的beanpostprocessor之间分离
		//实例化有限排序集合
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		//实例化内置后置处理器集合
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		//实例化顺序后置处理器名称集合
		List<String> orderedPostProcessorNames = new ArrayList<>();
		//实例化自定义后置处理器名称集合
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				//org.springframework.context.annotation.internalAutowiredAnnotationProcessor
				//org.springframework.context.annotation.internalAutowiredAnnotationProcessor：在初始化之前执行后置处理器。。。
				//org.springframework.context.annotation.internalCommonAnnotationProcessor
				//org.springframework.context.annotation.internalCommonAnnotationProcessor：在初始化之前执行后置处理器。。。
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				//自定义-customBeanPostProcessor
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		//首先，注册实现PriorityOrdered的beanpostprocessor
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		//接下来，注册实现Ordered的BeanPostProcessors
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			//---》AnnotationAwareAspectJAutoProxyCreators实现了Ordered接口，在下面代码出会进行实例化，跟普通的bean的创建过程一样
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			System.out.println("============如果使用了切面，在此处AnnotationAwareAspectJAutoProxyCreators创建器已经被实例化了");
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		//现在，注册所有常规beanpostprocessor
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			//customBeanPostProcessor：在初始化之前执行后置处理器。。。
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		//最后，重新注册所有内部beanpostprocessor
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		//重新注册用于检测内部bean的后处理器作为应用监听器，将其移动到处理器链的末端(用于拾取代理等等)
		//最后，注册ApplicationListenerDetector
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	//调用给定的BeanDefinitionRegistryPostProcessor bean
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			//注解加载类形式启动，将加了注解的业务类注册到容器中，同时初始化自定义BeanDefinitionRegistryPostProcessor（如我们测试案例里面创建了CustomBeanDefinitionRegistryPostProcessor），如果类需要注册，这个时候会被注册到容器中
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
