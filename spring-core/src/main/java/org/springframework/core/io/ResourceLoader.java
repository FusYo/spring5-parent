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

package org.springframework.core.io;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * Strategy interface for loading resources (e.. class path or file system
 * resources). An {@link org.springframework.context.ApplicationContext}
 * is required to provide this functionality, plus extended
 * {@link org.springframework.core.io.support.ResourcePatternResolver} support.
 *
 * <p>{@link DefaultResourceLoader} is a standalone implementation that is
 * usable outside an ApplicationContext, also used by {@link ResourceEditor}.
 *
 * <p>Bean properties of type Resource and Resource array can be populated
 * from Strings when running in an ApplicationContext, using the particular
 * context's resource loading strategy.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
/**
 * 加载资源的策略接口（类路径或文件系统资源）
 * ApplicationContext接口需要提供此功能
 * 因此，扩展继承ResourcePatternResolver接口来支持实现
 * 
 * DefaultResourceLoader是一个独立的实现在ApplicationContext之外可用,也由ResourceEditor类使用
 * 
 * 当在ApplicationContext中运行时，类型资源和资源数组的Bean属性可以填充，使用特定的上下文的资源加载策略
 * @author fussen
 * @date Jun 25, 2020 11:23:31 AM
 */
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	//类路径加载的伪URL前缀:" classpath:"
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;


	/**
	 * Return a Resource handle for the specified resource location.
	 * <p>The handle should always be a reusable resource descriptor,
	 * allowing for multiple {@link Resource#getInputStream()} calls.
	 * <p><ul>
	 * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
	 * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
	 * <li>Should support relative file paths, e.g. "WEB-INF/test.dat".
	 * (This will be implementation-specific, typically provided by an
	 * ApplicationContext implementation.)
	 * </ul>
	 * <p>Note that a Resource handle does not imply an existing resource;
	 * you need to invoke {@link Resource#exists} to check for existence.
	 * @param location the resource location
	 * @return a corresponding Resource handle (never {@code null})
	 * @see #CLASSPATH_URL_PREFIX
	 * @see Resource#exists()
	 * @see Resource#getInputStream()
	 */
	//返回指定资源位置的处理资源
	//处理应该始终是可重用的资源描述符
	//允许多个{@link Resource类#getInputStream()}调用
	//必须支持完全限定的url，例如。“文件:C: / test.dat
	//必须支持类路径伪url，例如。“classpath:test.dat
	//应该支持相对文件路径，例如。“WEB-INF/test.dat
	//这将是特定于实现的，通常由ApplicationContext实现提供
	//请注意，资源处理并不表示资源存在，你需要调用{@link Resource#exists}来检查是否存在
	Resource getResource(String location);

	/**
	 * Expose the ClassLoader used by this ResourceLoader.
	 * <p>Clients which need to access the ClassLoader directly can do so
	 * in a uniform manner with the ResourceLoader, rather than relying
	 * on the thread context ClassLoader.
	 * @return the ClassLoader
	 * (only {@code null} if even the system ClassLoader isn't accessible)
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	//公开这个ResourceLoader使用的类加载器。
	//需要直接访问类加载器的客户端可以通过ResourceLoader以统一的方式进行访问，而不是依赖于线程上下文类加载器
	//只有{@code null}，即使是系统类加载器也不可访问
	@Nullable
	ClassLoader getClassLoader();

}
