/*
 * Copyright 2002-2019 the original author or authors.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * Utility class for working with Strings that have placeholder values in them. A placeholder takes the form
 * {@code ${name}}. Using {@code PropertyPlaceholderHelper} these placeholders can be substituted for
 * user-supplied values. <p> Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

	private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}


	private final String placeholderPrefix;

	private final String placeholderSuffix;

	private final String simplePrefix;

	@Nullable
	private final String valueSeparator;

	private final boolean ignoreUnresolvablePlaceholders;


	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * Unresolvable placeholders are ignored.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
		this(placeholderPrefix, placeholderSuffix, null, true);
	}

	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
	 * be ignored ({@code true}) or cause an exception ({@code false})
	 */
	//创建一个新的{@code PropertyPlaceholderHelper}，它使用提供的前缀和后缀
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			@Nullable String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			//进入此分支，simplePrefix = “{”
			this.simplePrefix = simplePrefixForSuffix;
		}
		else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	/**
	 * Replaces all placeholders of format {@code ${name}} with the corresponding
	 * property from the supplied {@link Properties}.
	 * @param value the value containing the placeholders to be replaced
	 * @param properties the {@code Properties} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	//将格式{@code ${name}}的所有占位符替换为提供的{@link Properties}中的相应属性
	public String replacePlaceholders(String value, final Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		//properties::getProperty等同于
		//new PlaceholderResolver() {
		//public String resolvePlaceholder(String placeholderName) {
		//	return properties.getProperty(placeholderName);
		//}
		return replacePlaceholders(value, properties::getProperty);
	}

	/**
	 * Replaces all placeholders of format {@code ${name}} with the value returned
	 * from the supplied {@link PlaceholderResolver}.
	 * @param value the value containing the placeholders to be replaced
	 * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	//将格式{@code ${name}}的所有占位符替换为{@link PlaceholderResolver}提供的返回值
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
	    //发现laceholderResolver placeholderResolver参数存在数据   ??????
	    //俨然就是PropertySourcesPropertyResolver对象中的数据​   ?????
		//conversionService = null	
        //ignoreUnresolvableNestedPlaceholders = false	
		//nonStrictHelper = null	
		//placeholderPrefix = "${" (id=76)	
		//placeholderSuffix = "}" (id=77)	
		//propertySources:  PropertiesPropertySource {name='systemProperties'}   SystemEnvironmentPropertySource {name='systemEnvironment'}
		//requiredProperties = []
		//strictHelper	同上方法
		//valueSeparator = ":" (id=72)	
		return parseStringValue(value, placeholderResolver, null);
	}

	//递归
	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, @Nullable Set<String> visitedPlaceholders) {
		//检查这个字符串时候有 ${ 前缀
		int startIndex = value.indexOf(this.placeholderPrefix);
		if (startIndex == -1) {
			//如果没有，直接返回
			return value;
		}

		StringBuilder result = new StringBuilder(value);
		while (startIndex != -1) {
			//如果有 ${ 前缀，再检查搜索第一个占位符后缀的索引
			int endIndex = findPlaceholderEndIndex(result, startIndex);
			if (endIndex != -1) {
				//提取第一个占位符中的原始字符串，如${server.port}->server.port
				String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				String originalPlaceholder = placeholder;
				if (visitedPlaceholders == null) {
					visitedPlaceholders = new HashSet<>(4);
				}
				//将当前的占位符存到set集合中，如果set集合有了，就会添加失败
                //就会报错，循环引用错误，比如${a},这个a的值依然是${a}
                //这样就陷入了无限解析了，根本停不下来
				if (!visitedPlaceholders.add(originalPlaceholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
				}
				// Recursive invocation, parsing placeholders contained in the placeholder key.
				//// 递归调用，实际上就是解析嵌套的占位符，因为提取的原始字符串有可能还有一层或者多层占位符,如：${${a}},所以要继续解析
				placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
				// Now obtain the value for the fully resolved key...
				 // 递归调用完毕后，调用getPropertyAsRawString获取key对应的字符串值-也即是设置的属性值
				String propVal = placeholderResolver.resolvePlaceholder(placeholder);
				// 如果字符串值为null，则进行默认值的解析，因为默认值有可能也使用了占位符，如${server.port:${server.port2:8080}}
				if (propVal == null && this.valueSeparator != null) {
					//查找这个propVal是否为：分割的字符串
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if (separatorIndex != -1) {
						//如果propVal为key:value,那么这个值应为key
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						 //如果propVal为key:value,那么就是value-提取默认值的字符串
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						// //跟上面的一样去系统属性中查找
						propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
						//如果为空，那么就设置为defaultValue
						if (propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				 // 上一步解析出来的值不为null，但是它有可能是一个带占位符的值，所以后面对值进行递归解析
				if (propVal != null) {
					// Recursive invocation, parsing placeholders contained in the
					// previously resolved placeholder value.
					//这个值可能也有占位符，继续递归解析
					propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
					 //得到了占位符对应的值后替换掉第一个被解析完毕的占位符属性，例如${server.port}-${spring.application.name} ->变成 9090--${spring.application.name}
					result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					if (logger.isTraceEnabled()) {
						logger.trace("Resolved placeholder '" + placeholder + "'");
					}
					//继续查找是否还有后续的占位符
					startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				 //如果propValue为null，那么就说明这个占位符没有值，如果设置为忽略
                //不能解析的占位符，那么继续后续的占位符，否则报错
				else if (this.ignoreUnresolvablePlaceholders) {
					// // 如果propVal为null并且ignoreUnresolvablePlaceholders设置为true，直接返回当前的占位符之间的原始字符串尾的索引，也就是跳过解析
					// Proceed with unprocessed value.
					startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				}
				else {
					throw new IllegalArgumentException("Could not resolve placeholder '" +
							placeholder + "'" + " in value \"" + value + "\"");
				}
				 //解析成功就删除set集合中对应的占位符
				visitedPlaceholders.remove(originalPlaceholder);
			}
			else {
				//// endIndex = -1说明解析结束
				startIndex = -1;
			}
		}
		return result.toString();
	}

	//基于传入的起始索引，搜索第一个占位符后缀的索引，兼容嵌套的占位符
	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
		while (index < buf.length()) {
			if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
			else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			}
			else {
				index++;
			}
		}
		return -1;
	}


	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 */
	//用于解析字符串中占位符的替换属性值的策略接口。
	@FunctionalInterface
	public interface PlaceholderResolver {

		/**
		 * Resolve the supplied placeholder name to the replacement value.
		 * @param placeholderName the name of the placeholder to resolve
		 * @return the replacement value, or {@code null} if no replacement is to be made
		 */
		//将提供的占位符名称解析为替换值
		@Nullable
		String resolvePlaceholder(String placeholderName);
	}

}
