/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.aop.framework.autoproxy;

import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Generic auto proxy creator that builds AOP proxies for specific beans
 * based on detected Advisors for each bean.
 *
 * <p>Subclasses must implement the abstract {@link #findCandidateAdvisors()}
 * method to return a list of Advisors applying to any object. Subclasses can
 * also override the inherited {@link #shouldSkip} method to exclude certain
 * objects from auto-proxying.
 *
 * <p>Advisors or advices requiring ordering should implement the
 * {@link org.springframework.core.Ordered} interface. This class sorts
 * Advisors by Ordered order value. Advisors that don't implement the
 * Ordered interface will be considered as unordered; they will appear
 * at the end of the advisor chain in undefined order.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
/**
 * 通用的自动代理创建器，基于每个bean检测到的通知为特定bean构建AOP代理
 * @author fussen
 * Aug 18, 2020 11:16:44 AM
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	@Nullable
	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;

	// 重写了setBeanFactory方法，需要保证bean工厂必须是ConfigurableListableBeanFactory
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		// 等同于：this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory)
		// 对Helper进行初始化，找advisor最终委托给他
		// BeanFactoryAdvisorRetrievalHelperAdapter继承自BeanFactoryAdvisorRetrievalHelper,为私有内部类，
		//主要重写了isEligibleBean（）方法，调用.this.isEligibleAdvisorBean(beanName)方法
		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}

	// 这是复写父类的方法，也是实现代理方式。找到作用在这个Bean里面的切点方法
    // 当然 最终最终委托给BeanFactoryAdvisorRetrievalHelper去做的
	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
		//targetSource传值为null
		//获取到当前Bean所匹配的通知列表
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * Find all eligible Advisors for auto-proxying this class.
	 * @param beanClass the clazz to find advisors for
	 * @param beanName the name of the currently proxied bean
	 * @return the empty List, not {@code null},
	 * if there are no pointcuts or interceptors
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	//找到自动代理该类的所有合格的通知
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		//找到spring IOC容器中所有的候选Advisor
		//[InstantiationModelAwarePointcutAdvisor: expression [pointcut()]; 
		//advice method [public java.lang.Object com.fs.aspect.DemoAspect.around(org.aspectj.lang.JoinPoint)]; perClauseKind=SINGLETON, InstantiationModelAwarePointcutAdvisor: expression [pointcut()]; 
		//advice method [public void com.fs.aspect.DemoAspect.before(org.aspectj.lang.JoinPoint)]; perClauseKind=SINGLETON, InstantiationModelAwarePointcutAdvisor: expression [pointcut()]; 
		//advice method [public void com.fs.aspect.DemoAspect.after(org.aspectj.lang.JoinPoint)]; perClauseKind=SINGLETON]
		// 根据ClassFilter和MethodMatcher等等各种匹配。（但凡只有有一个方法被匹配上了，就会给他创建代理类了）
		// 方法用的ReflectionUtils.getAllDeclaredMethods:因此哪怕是私有方法，匹配上都会给创建的代理对象，这点务必要特别特别的注意
		//这里调用的是子类AnnotationAwareAspectJAutoProxyCreator实现的方法。
		//InstantiationModelAwarePointcutAdvisor实现了Advisor接口
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		//判断找到Advisor能不能作用在当前类上
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		//提供一个钩子。子类可以复写此方法  然后对eligibleAdvisors进行处理（增加/删除/修改等等）
		// AspectJAwareAdvisorAutoProxyCreator提供了实现
		extendAdvisors(eligibleAdvisors);
		//排序
		if (!eligibleAdvisors.isEmpty()) {
			// 默认排序方式：AnnotationAwareOrderComparator.sort()排序  这个排序和Order接口有关
			// 但是子类：AspectJAwareAdvisorAutoProxyCreator有复写此排序方法，需要特别注意
			//调用的是AspectJAwareAdvisorAutoProxyCreator实现的方法
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * Find all candidate Advisors to use in auto-proxying.
	 * @return the List of candidate Advisors
	 */
	//找到所有在自动代理中使用的候选通知
	// AnnotationAwareAspectJAutoProxyCreator对它有复写
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * Search the given candidate Advisors to find all Advisors that
	 * can apply to the specified bean.
	 * @param candidateAdvisors the candidate Advisors
	 * @param beanClass the target's bean class
	 * @param beanName the target's bean name
	 * @return the List of applicable Advisors
	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
	 */
	//搜索给定的候选通知，以找到所有可以应用到指定bean的通知
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * Return whether the Advisor bean with the given name is eligible
	 * for proxying in the first place.
	 * @param beanName the name of the Advisor bean
	 * @return whether the bean is eligible
	 */
	// 判断给定的BeanName这个Bean，是否是合格的(BeanFactoryAdvisorRetrievalHelper里会用到这个属性)
	// 其中：DefaultAdvisorAutoProxyCreator和InfrastructureAdvisorAutoProxyCreator有复写
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * Sort advisors based on ordering. Subclasses may choose to override this
	 * method to customize the sorting strategy.
	 * @param advisors the source List of Advisors
	 * @return the sorted List of Advisors
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * Extension hook that subclasses can override to register additional Advisors,
	 * given the sorted Advisors obtained to date.
	 * <p>The default implementation is empty.
	 * <p>Typically used to add Advisors that expose contextual information
	 * required by some of the later advisors.
	 * @param candidateAdvisors Advisors that have already been identified as
	 * applying to a given bean
	 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * This auto-proxy creator always returns pre-filtered Advisors.
	 */
	// 此处复写了父类的方法，返回true
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
	 * surrounding AbstractAdvisorAutoProxyCreator facilities.
	 */
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}
