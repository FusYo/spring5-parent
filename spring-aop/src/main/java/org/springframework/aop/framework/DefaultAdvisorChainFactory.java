/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.framework;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.support.MethodMatchers;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple but definitive way of working out an advice chain for a Method,
 * given an {@link Advised} object. Always rebuilds each advice chain;
 * caching can be provided by subclasses.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0.3
 */
@SuppressWarnings("serial")

public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	/**
	 * 从提供的配置实例config中获取advisor列表,遍历处理这些advisor.如果是IntroductionAdvisor,
	 * 则判断此Advisor能否应用到目标类targetClass上.如果是PointcutAdvisor,则判断
	 * 此Advisor能否应用到目标方法method上.将满足条件的Advisor通过AdvisorAdaptor转化成Interceptor列表返回.
	 */
	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Method method, @Nullable Class<?> targetClass) {

		// This is somewhat tricky... We have to process introductions first,
		// but we need to preserve order in the ultimate list.
		List<Object> interceptorList = new ArrayList<>(config.getAdvisors().length);
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
		//查看是否包含IntroductionAdvisor类型的Advisor
		boolean hasIntroductions = hasMatchingIntroductions(config, actualClass);
		//这里实际上注册一系列AdvisorAdapter,用于将Advisor转化成MethodInterceptor
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
		//遍历通知器
		for (Advisor advisor : config.getAdvisors()) {
			//PointcutAdvisor类型的通知器
			//通过@Aspect加入的通知器类型为InstantiationModelAwarePointcutAdvisorImpl，实现了PointcutAdvisor
			if (advisor instanceof PointcutAdvisor) {
				// Add it conditionally.
				PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
				//判断切面逻辑的调用链是否提前进行过过滤，如果进行过，则不再进行目标方法匹配
				//如果没有进行过，则再进行一次匹配，使用AnnotationAwareAspectJAutoProxyCreator
				//在生成切面逻辑的时候就已经进行了过滤，因而这里返回true
				if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
					//这个地方这两个方法的位置可以互换下
					//将Advisor转化成MethodInterceptor数组
					MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
					//检查当前advisor的pointcut是否可以匹配当前方法
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					//进行匹配的时候，首先检查是否为TntroductionAwareMethodMatcher类型的Matcher
					//如果是，则调用定义的matches（）方法进行匹配，如果不是，则直接调用当前切面的matches()方法进行匹配。
					//这里由于当前进行匹配是可能存在部分在静态匹配时无法确认的方法匹配结果。因而这里调用是必要的，
					//而对于能够确认的匹配逻辑,这里调用也是很快的，因为前面已经对匹配结果进行了缓存
					if (MethodMatchers.matches(mm, method, actualClass, hasIntroductions)) {
						//判断如果是动态匹配，则使用TntroductionAwareMethodMatcher进行封装
						if (mm.isRuntime()) {
							// Creating a new object instance in the getInterceptors() method
							// isn't a problem as we normally cache created chains.
							for (MethodInterceptor interceptor : interceptors) {
								interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
							}
						}
						else {
							//如果是静态匹配，则直接将调用链返回
							interceptorList.addAll(Arrays.asList(interceptors));
						}
					}
				}
			}
			else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				//判断如果为IntroductionAdvisor类型的Advisor，则将调用链封装为Interceptor数组
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					Interceptor[] interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(Arrays.asList(interceptors));
				}
			}
			else {
				//使用自定义的转换器定义对Advisor进行转换的逻辑，因为getInterceptors()方法中将会使用相应的Adapter对目标Advisor进行匹配
				//如果能匹配上，通过其getInterceptors()方法将自定义的Advice转换为MethodInterceptor对象
				Interceptor[] interceptors = registry.getInterceptors(advisor);
				interceptorList.addAll(Arrays.asList(interceptors));
			}
		}

		return interceptorList;
	}

	/**
	 * Determine whether the Advisors contain matching introductions.
	 */
	private static boolean hasMatchingIntroductions(Advised config, Class<?> actualClass) {
		for (int i = 0; i < config.getAdvisors().length; i++) {
			Advisor advisor = config.getAdvisors()[i];
			if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(actualClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
