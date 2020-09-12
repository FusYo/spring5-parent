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

package org.springframework.web.method.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Handles method return values by delegating to a list of registered {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}.
 * Previously resolved return types are cached for faster lookups.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler {

	protected final Log logger = LogFactory.getLog(getClass());

	private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();


	/**
	 * Return a read-only list with the registered handlers, or an empty list.
	 */
	public List<HandlerMethodReturnValueHandler> getHandlers() {
		return Collections.unmodifiableList(this.returnValueHandlers);
	}

	/**
	 * Whether the given {@linkplain MethodParameter method return type} is supported by any registered
	 * {@link HandlerMethodReturnValueHandler}.
	 */
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return getReturnValueHandler(returnType) != null;
	}

	@Nullable
	private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
		for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
			if (handler.supportsReturnType(returnType)) {
				return handler;
			}
		}
		return null;
	}

	/**
	 * Iterate over registered {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers} and invoke the one that supports it.
	 * @throws IllegalStateException if no suitable {@link HandlerMethodReturnValueHandler} is found.
	 */
	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

		//点击这里
		//org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor@f163f2f
		HandlerMethodReturnValueHandler handler = selectHandler(returnValue, returnType);
		if (handler == null) {
			throw new IllegalArgumentException("Unknown return value type: " + returnType.getParameterType().getName());
		}
		handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
	}

	@Nullable
	private HandlerMethodReturnValueHandler selectHandler(@Nullable Object value, MethodParameter returnType) {
		boolean isAsyncValue = isAsyncReturnValue(value, returnType);
		//returnValueHandlers 这个集合中有非常多的处理器 来处理用户到底想要返回哪种数据类型
		//例如可以返回 ModelAndView对象 ,字符串 ,list集合  普通的对象等等  都是有这些处理器来做的
		//[org.springframework.web.servlet.mvc.method.annotation.ModelAndViewMethodReturnValueHandler@7364e6af, 
//		org.springframework.web.method.annotation.ModelMethodProcessor@775b7c1b, 
//		org.springframework.web.servlet.mvc.method.annotation.ViewMethodReturnValueHandler@3259ec94, 
//		org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitterReturnValueHandler@5724adfb, 
//		org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBodyReturnValueHandler@3a1782e, 
//		org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor@2f3667e5, 
//		org.springframework.web.servlet.mvc.method.annotation.HttpHeadersReturnValueHandler@527613db, 
//		org.springframework.web.servlet.mvc.method.annotation.CallableMethodReturnValueHandler@20cf271b, 
//		org.springframework.web.servlet.mvc.method.annotation.DeferredResultMethodReturnValueHandler@28a75b24, 
//		org.springframework.web.servlet.mvc.method.annotation.AsyncTaskMethodReturnValueHandler@3432089a, 
//		org.springframework.web.method.annotation.ModelAttributeMethodProcessor@56382d0c, 
//		org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor@f163f2f, 
//		org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler@1dfa3b92, 
//		org.springframework.web.method.annotation.MapMethodProcessor@3928866f, 
//		org.springframework.web.method.annotation.ModelAttributeMethodProcessor@2e6f9c0b]
		for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
			if (isAsyncValue && !(handler instanceof AsyncHandlerMethodReturnValueHandler)) {
				continue;
			}
			//如果当前方法或者所在类使用了@ResponseBody,则返回json字符串
			if (handler.supportsReturnType(returnType)) {
				//org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor@f163f2f
				return handler;
			}
		}
		return null;
	}

	private boolean isAsyncReturnValue(@Nullable Object value, MethodParameter returnType) {
		for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
			if (handler instanceof AsyncHandlerMethodReturnValueHandler &&
					((AsyncHandlerMethodReturnValueHandler) handler).isAsyncReturnValue(value, returnType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add the given {@link HandlerMethodReturnValueHandler}.
	 */
	public HandlerMethodReturnValueHandlerComposite addHandler(HandlerMethodReturnValueHandler handler) {
		this.returnValueHandlers.add(handler);
		return this;
	}

	/**
	 * Add the given {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}.
	 */
	public HandlerMethodReturnValueHandlerComposite addHandlers(
			@Nullable List<? extends HandlerMethodReturnValueHandler> handlers) {

		if (handlers != null) {
			this.returnValueHandlers.addAll(handlers);
		}
		return this;
	}

}
