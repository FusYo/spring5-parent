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

package org.springframework.util;

import org.springframework.lang.Nullable;

/**
 * Helper class for resolving placeholders in texts. Usually applied to file paths.
 *
 * <p>A text may contain {@code ${...}} placeholders, to be resolved as system properties:
 * e.g. {@code ${user.dir}}. Default values can be supplied using the ":" separator
 * between key and value.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @since 1.2.5
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see System#getProperty(String)
 */
/**
 * 用于解析文本中的占位符的Helper类。通常应用于文件路径
 * 文本可以包含{@code ${...}占位符，要解析为系统属性:例如:{@code $ {user.dir}}。可以使用键和值之间的分隔符“:”来提供默认值。
 * @author fussen
 * @date Jun 30, 2020 11:19:50 AM
 */
public abstract class SystemPropertyUtils {

	/** Prefix for system property placeholders: "${". */
	//系统属性占位符的前缀:"${"
	public static final String PLACEHOLDER_PREFIX = "${";

	/** Suffix for system property placeholders: "}". */
	//系统属性占位符的后缀:"}"
	public static final String PLACEHOLDER_SUFFIX = "}";

	/** Value separator for system property placeholders: ":". */
	//系统属性占位符的值分隔符:":"
	public static final String VALUE_SEPARATOR = ":";

	//严格处理-没有缺省值的未解析占位符进行处理
	private static final PropertyPlaceholderHelper strictHelper =
			new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, false);
	//不严格处理-没有缺省值的未解析占位符将被忽略
	private static final PropertyPlaceholderHelper nonStrictHelper =
			new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, true);


	/**
	 * Resolve {@code ${...}} placeholders in the given text, replacing them with
	 * corresponding system property values.
	 * @param text the String to resolve
	 * @return the resolved String
	 * @throws IllegalArgumentException if there is an unresolvable placeholder
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 */
	//解决给定文本中的占位符{@code ${...}}，用相应的系统属性值替换它们
	public static String resolvePlaceholders(String text) {
		return resolvePlaceholders(text, false);
	}

	/**
	 * Resolve {@code ${...}} placeholders in the given text, replacing them with
	 * corresponding system property values. Unresolvable placeholders with no default
	 * value are ignored and passed through unchanged if the flag is set to {@code true}.
	 * @param text the String to resolve
	 * @param ignoreUnresolvablePlaceholders whether unresolved placeholders are to be ignored
	 * @return the resolved String
	 * @throws IllegalArgumentException if there is an unresolvable placeholder
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 * and the "ignoreUnresolvablePlaceholders" flag is {@code false}
	 */
	//如果标志被设置为{@code true}，则没有缺省值的未解析占位符将被忽略并以未更改的方式传递。
	//@param ignoreUnresolvablePlaceholders 未解析的占位符是否被忽略
	public static String resolvePlaceholders(String text, boolean ignoreUnresolvablePlaceholders) {
		//如果ignoreUnresolvablePlaceholders为true，选择nonStrictHelper，否则strictHelper
		//这里传递的是false，也即是要进行占位符处理
		//赋值
		PropertyPlaceholderHelper helper = (ignoreUnresolvablePlaceholders ? nonStrictHelper : strictHelper);
		return helper.replacePlaceholders(text, new SystemPropertyPlaceholderResolver(text));
	}


	/**
	 * PlaceholderResolver implementation that resolves against system properties
	 * and system environment variables.
	 */
	//根据系统属性和系统环境变量进行解析的PlaceholderResolver实现
	private static class SystemPropertyPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

		private final String text;

		public SystemPropertyPlaceholderResolver(String text) {
			this.text = text;
		}

		@Override
		@Nullable
		public String resolvePlaceholder(String placeholderName) {
			try {
				//返回JVM中的（Java程序）属性
				String propVal = System.getProperty(placeholderName);
				if (propVal == null) {
					// Fall back to searching the system environment.
					//返回搜索系统环境相关的属性
					propVal = System.getenv(placeholderName);
				}
				return propVal;
			}
			catch (Throwable ex) {
				System.err.println("Could not resolve placeholder '" + placeholderName + "' in [" +
						this.text + "] as system property: " + ex);
				return null;
			}
		}
	}

}
