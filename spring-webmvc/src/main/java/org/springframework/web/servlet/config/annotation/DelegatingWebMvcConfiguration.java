/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.servlet.config.annotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * A subclass of {@code WebMvcConfigurationSupport} that detects and delegates
 * to all beans of type {@link WebMvcConfigurer} allowing them to customize the
 * configuration provided by {@code WebMvcConfigurationSupport}. This is the
 * class actually imported by {@link EnableWebMvc @EnableWebMvc}.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
/**
 * DelegatingWebMvcConfiguration是对Spring MVC进行配置的一个代理类。它结合缺省配置和用户配置定义Spring MVC运行时最终使用的配置
 * @author fussen
 * Sep 10, 2020 10:24:43 AM
 */
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

	// WebMvcConfigurerComposite 其实就是对多个 WebMvcConfigurer 的一个组合,
	// 从命名就可以看出这一点
	// WebMvcConfigurerComposite 自身也实现了接口 WebMvcConfigurer,
	// 为什么要组合多个 WebMvcConfigurer 然后自己又实现该接口 ?
	// 这么做的主要目的是在配置时简化逻辑。调用者对 WebMvcConfigurerComposite可以当作一个 WebMvcConfigurer 来使用，而对它的每个方法的调用都又会传导到它所包含的各个 WebMvcConfigurer 。
	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();

	// 注入一组WebMvcConfigurer，这些WebMvcConfigurer由开发人员提供，或者框架其他部分提供，已经
	// 以bean的方式注入到容器中
	@Autowired(required = false)
	public void setConfigurers(List<WebMvcConfigurer> configurers) {
		if (!CollectionUtils.isEmpty(configurers)) {
			this.configurers.addWebMvcConfigurers(configurers);
		}
	}

	// 以下各个方法都是对WebMvcConfigurationSupport提供的配置原材料定制回调方法的覆盖实现，
	// 对这些方法的调用最终都转化成了对configurers的方法调用，从而实现了定制化缺省Spring MVC 
	// 配置的作用
	@Override
	protected void configurePathMatch(PathMatchConfigurer configurer) {
		this.configurers.configurePathMatch(configurer);
	}

	@Override
	protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		this.configurers.configureContentNegotiation(configurer);
	}

	@Override
	protected void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		this.configurers.configureAsyncSupport(configurer);
	}

	@Override
	protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		this.configurers.configureDefaultServletHandling(configurer);
	}

	@Override
	protected void addFormatters(FormatterRegistry registry) {
		this.configurers.addFormatters(registry);
	}

	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		this.configurers.addInterceptors(registry);
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		this.configurers.addResourceHandlers(registry);
	}

	@Override
	protected void addCorsMappings(CorsRegistry registry) {
		this.configurers.addCorsMappings(registry);
	}

	@Override
	protected void addViewControllers(ViewControllerRegistry registry) {
		this.configurers.addViewControllers(registry);
	}

	@Override
	protected void configureViewResolvers(ViewResolverRegistry registry) {
		this.configurers.configureViewResolvers(registry);
	}

	@Override
	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		this.configurers.addArgumentResolvers(argumentResolvers);
	}

	@Override
	protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		this.configurers.addReturnValueHandlers(returnValueHandlers);
	}

	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		this.configurers.configureMessageConverters(converters);
	}

	@Override
	protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		this.configurers.extendMessageConverters(converters);
	}

	@Override
	protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		this.configurers.configureHandlerExceptionResolvers(exceptionResolvers);
	}

	@Override
	protected void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		this.configurers.extendHandlerExceptionResolvers(exceptionResolvers);
	}

	@Override
	@Nullable
	protected Validator getValidator() {
		return this.configurers.getValidator();
	}

	@Override
	@Nullable
	protected MessageCodesResolver getMessageCodesResolver() {
		return this.configurers.getMessageCodesResolver();
	}

}
