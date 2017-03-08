/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
* A {@link DiscoverySelector} that selects a {@link Method} or a combination of class name, method
* name, and parameter types so that {@link org.junit.platform.engine.TestEngine TestEngines} can
* discover tests or containers based on methods.
*
* <p>If a Java {@link Method} is provided, the selector will return that {@code Method} and its
* method and class names accordingly. If a {@link Class} and method name, a class name and method
* name, or simply a fully qualified method name is provided, the selector will only attempt to
* lazily load the {@link Class} and {@link Method} if {@link #getJavaClass()} or {@link
* #getJavaMethod()} is invoked.
*
* <p>In this context, Java {@link Method} means anything that can be referenced as a {@link Method}
* on the JVM &mdash; for example, methods from other JVM languages such Groovy, Scala, etc.
*
* @since 1.0
* @see org.junit.platform.engine.support.descriptor.MethodSource
*/
@API(Experimental)
public class MethodSelector implements DiscoverySelector {

	private final String className;
	private final String methodName;
	private final String methodParameterTypes;

	private Class<?> javaClass;
	private Method javaMethod;

	MethodSelector(String className, String methodName) {
		this(className, methodName, null);
	}

	MethodSelector(String className, String methodName, String methodParameterTypes) {
		this.className = className;
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	MethodSelector(Class<?> javaClass, String methodName) {
		this(javaClass, methodName, null);
	}

	public MethodSelector(Class<?> javaClass, String methodName, String methodParameterTypes) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	MethodSelector(Class<?> javaClass, Method method) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.javaMethod = method;
		this.methodName = method.getName();
		this.methodParameterTypes = null;
	}

	/** Get the selected class name. */
	public String getClassName() {
		return this.className;
	}

	/** Get the selected method name. */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	* Get the parameter types for the selected method as a {@link String}, typically a
	* comma-separated list of atomic types, fully qualified class names, or array types.
	*
	* <p>Note: the parameter types are provided as a single string instead of a collection in order
	* to allow this selector to be used in a generic fashion by various test engines. It is therefore
	* the responsibility of the caller of this method to determine how to parse the returned string.
	*/
	public String getMethodParameterTypes() {
		return this.methodParameterTypes;
	}

	/**
	* Get the {@link Class} in which the selected {@linkplain #getJavaMethod method} is declared, or
	* a subclass thereof.
	*
	* <p>If the {@link Class} was not provided, but only the name, this method attempts to lazily
	* load the {@code Class} based on its name and throws a {@link PreconditionViolationException} if
	* the class cannot be loaded.
	*
	* @see #getJavaMethod()
	*/
	public Class<?> getJavaClass() {
		lazyLoadJavaClass();
		return this.javaClass;
	}

	/**
	* Get the selected {@link Method}.
	*
	* <p>If the {@link Method} was not provided, but only the name, this method attempts to lazily
	* load the {@code Method} based on its name and throws a {@link PreconditionViolationException}
	* if the method cannot be loaded.
	*
	* @see #getJavaClass()
	*/
	public Method getJavaMethod() {
		lazyLoadJavaMethod();
		return this.javaMethod;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("className", this.className)
				.append("methodName", this.methodName)
				.append("methodParameterTypes", this.methodParameterTypes)
				.toString();
		// @formatter:on
	}

	private void lazyLoadJavaClass() {
		if (this.javaClass == null) {
			this.javaClass =
					ReflectionUtils.loadClass(this.className)
							.orElseThrow(
									() ->
											new PreconditionViolationException(
													"Could not load class with name: " + this.className));
		}
	}

	private void lazyLoadJavaMethod() {
		lazyLoadJavaClass();

		if (this.javaMethod == null) {
			if (StringUtils.isNotBlank(this.methodParameterTypes)) {
				this.javaMethod =
						ReflectionUtils.findMethod(this.javaClass, this.methodName, this.methodParameterTypes)
								.orElseThrow(
										() ->
												new PreconditionViolationException(
														String.format(
																"Could not find method with name [%s] and parameter types [%s] in class [%s].",
																this.methodName,
																this.methodParameterTypes,
																this.javaClass.getName())));
			} else {
				this.javaMethod =
						ReflectionUtils.findMethod(this.javaClass, this.methodName)
								.orElseThrow(
										() ->
												new PreconditionViolationException(
														String.format(
																"Could not find method with name [%s] in class [%s].",
																this.methodName, this.javaClass.getName())));
			}
		}
	}
}
