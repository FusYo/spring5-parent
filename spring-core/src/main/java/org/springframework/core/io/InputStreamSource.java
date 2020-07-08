/*
 * Copyright 2002-2017 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple interface for objects that are sources for an {@link InputStream}.
 *
 * <p>This is the base interface for Spring's more extensive {@link Resource} interface.
 *
 * <p>For single-use streams, {@link InputStreamResource} can be used for any
 * given {@code InputStream}. Spring's {@link ByteArrayResource} or any
 * file-based {@code Resource} implementation can be used as a concrete
 * instance, allowing one to read the underlying content stream multiple times.
 * This makes this interface useful as an abstract content source for mail
 * attachments, for example.
 *
 * @author Juergen Hoeller
 * @since 20.01.2004
 * @see java.io.InputStream
 * @see Resource
 * @see InputStreamResource
 * @see ByteArrayResource
 */
/**
 * 
 * @author fussen
 * @date Jun 25, 2020 5:42:31 PM
 */
public interface InputStreamSource {

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>It is expected that each call creates a <i>fresh</i> stream.
	 * <p>This requirement is particularly important when you consider an API such
	 * as JavaMail, which needs to be able to read the stream multiple times when
	 * creating mail attachments. For such a use case, it is <i>required</i>
	 * that each {@code getInputStream()} call returns a fresh stream.
	 * @return the input stream for the underlying resource (must not be {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource doesn't exist
	 * @throws IOException if the content stream could not be opened
	 */
	//为底层资源的内容返回{@link InputStream}-在java中InputStream是字节输入流，用来将文件中的数据读取到java程序中。InputStream是所有字节输入流的顶层父类，是一个抽象类。如果要用，需要使用子类
    //预期每个调用都会创建一个新的流
	//当您考虑这样的API时，这一要求尤其重要，如JavaMail，其中需要能够多次读取流时创建邮件附件。对于这样的用例，它是<i>required</i>每个{@code getInputStream()}调用都会返回一个新的流
	InputStream getInputStream() throws IOException;

}
