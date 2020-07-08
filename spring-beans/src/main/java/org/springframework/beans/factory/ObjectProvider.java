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

package org.springframework.beans.factory;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * A variant of {@link ObjectFactory} designed specifically for injection points,
 * allowing for programmatic optionality and lenient not-unique handling.
 *
 * <p>As of 5.1, this interface extends {@link Iterable} and provides {@link Stream}
 * support. It can be therefore be used in {@code for} loops, provides {@link #forEach}
 * iteration and allows for collection-style {@link #stream} access.
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @param <T> the object type
 * @see BeanFactory#getBeanProvider
 * @see org.springframework.beans.factory.annotation.Autowired
 */
/**
 * 专门为注入点设计的{@link ObjectFactory}的变体，允许编程选择和宽大的非唯一处理
 * 在5.1版本中，这个接口扩展了{@link Iterable}并提供了{@link Stream}支持。因此，它可以在{@code for}循环中使用，提供{@link #forEach}迭代，并允许集合风格的{@link #stream}访问
 * @author fussen
 * @date Jun 26, 2020 11:09:13 AM
 * @param <T>
 */
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * <p>Allows for specifying explicit construction arguments, along the
	 * lines of {@link BeanFactory#getBean(String, Object...)}.
	 * @param args arguments to use when creating a corresponding instance
	 * @return an instance of the bean
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	//返回由此工厂管理的对象的实例(可能是共享的或独立的)
	//允许指定显式构造参数，沿着{@link BeanFactory#getBean(String, Object…)}
	T getObject(Object... args) throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @return an instance of the bean, or {@code null} if not available
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	//返回由此工厂管理的对象的实例(可能是共享的或独立的)
	@Nullable
	T getIfAvailable() throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @param defaultSupplier a callback for supplying a default object
	 * if none is present in the factory
	 * @return an instance of the bean, or the supplied default object
	 * if no such bean is available
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	//如果工厂中不存在默认对象，用于提供默认对象的回调
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * Consume an instance (possibly shared or independent) of the object
	 * managed by this factory, if available.
	 * @param dependencyConsumer a callback for processing the target object
	 * if available (not called otherwise)
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	//如果可用，消费由该工厂管理的对象的一个实例(可能是共享的或独立的)
	//如果可用，用于处理目标对象的回调(不调用其他方法)
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @return an instance of the bean, or {@code null} if not available or
	 * not unique (i.e. multiple candidates found with none marked as primary)
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	//bean的一个实例，如果不可用或不是唯一的，则为{@code null}(例如，多个候选对象没有标记为主要对象)
	@Nullable
	T getIfUnique() throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @param defaultSupplier a callback for supplying a default object
	 * if no unique candidate is present in the factory
	 * @return an instance of the bean, or the supplied default object
	 * if no such bean is available or if it is not unique in the factory
	 * (i.e. multiple candidates found with none marked as primary)
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfUnique()
	 */
	//提供回调
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * Consume an instance (possibly shared or independent) of the object
	 * managed by this factory, if unique.
	 * @param dependencyConsumer a callback for processing the target object
	 * if unique (not called otherwise)
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	//提供回调
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * Return an {@link Iterator} over all matching object instances,
	 * without specific ordering guarantees (but typically in registration order).
	 * @since 5.1
	 * @see #stream()
	 */
	//在所有匹配的对象实例上返回{@link Iterator}，没有特定的顺序保证(但通常是注册顺序)
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * Return a sequential {@link Stream} over all matching object instances,
	 * without specific ordering guarantees (but typically in registration order).
	 * @since 5.1
	 * @see #iterator()
	 * @see #orderedStream()
	 */
	//在所有匹配的对象实例上返回顺序的{@link Stream}，没有特定的顺序保证(但通常是注册顺序)
	default Stream<T> stream() {
		throw new UnsupportedOperationException("Multi element access not supported");
	}

	/**
	 * Return a sequential {@link Stream} over all matching object instances,
	 * pre-ordered according to the factory's common order comparator.
	 * <p>In a standard Spring application context, this will be ordered
	 * according to {@link org.springframework.core.Ordered} conventions,
	 * and in case of annotation-based configuration also considering the
	 * {@link org.springframework.core.annotation.Order} annotation,
	 * analogous to multi-element injection points of list/array type.
	 * @since 5.1
	 * @see #stream()
	 * @see org.springframework.core.OrderComparator
	 */
	//在所有匹配的对象实例上返回顺序的{@link Stream}，这些对象实例是根据工厂的通用顺序比较器预先排序的
	default Stream<T> orderedStream() {
		throw new UnsupportedOperationException("Ordered element access not supported");
	}

}
