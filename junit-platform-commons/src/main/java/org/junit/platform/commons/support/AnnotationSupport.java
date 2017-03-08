/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.commons.support;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
* Common annotation support.
*
* @since 1.0
*/
@API(Maintained)
public final class AnnotationSupport {

	///CLOVER:OFF
	private AnnotationSupport() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	* Determine if an annotation of {@code annotationType} is either <em>present</em> or
	* <em>meta-present</em> on the supplied {@code element}.
	*
	* @see #findAnnotation(AnnotatedElement, Class)
	*/
	public static boolean isAnnotated(
			AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.isAnnotated(element, annotationType);
	}

	/**
	* Find the first annotation of {@code annotationType} that is either <em>present</em> or
	* <em>meta-present</em> on the supplied {@code element}.
	*/
	public static <A extends Annotation> Optional<A> findAnnotation(
			AnnotatedElement element, Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(element, annotationType);
	}

	/**
	* Find all <em>repeatable</em> {@linkplain Annotation annotations} of {@code annotationType} that
	* are either <em>present</em>, <em>indirectly present</em>, or <em>meta-present</em> on the
	* supplied {@link AnnotatedElement}.
	*
	* <p>This method extends the functionality of {@link
	* java.lang.reflect.AnnotatedElement#getAnnotationsByType(Class)} with additional support for
	* meta-annotations.
	*
	* <p>In addition, if the element is a class and the repeatable annotation is {@link
	* java.lang.annotation.Inherited @Inherited}, this method will search on superclasses first in
	* order to support top-down semantics. The result is that this algorithm finds repeatable
	* annotations that would be <em>shadowed</em> and therefore not visible according to Java's
	* standard semantics for inherited, repeatable annotations, but most developers will naturally
	* assume that all repeatable annotations in JUnit are discovered regardless of whether they are
	* declared stand-alone, in a container, or as a meta-annotation (e.g., multiple declarations of
	* {@code @ExtendWith} within a test class hierarchy).
	*
	* <p>If the supplied {@code element} is {@code null}, this method simply returns an empty list.
	*
	* @param element the element to search on, potentially {@code null}
	* @param annotationType the repeatable annotation type to search for; never {@code null}
	* @return the list of all such annotations found; never {@code null}
	* @see java.lang.annotation.Repeatable
	* @see java.lang.annotation.Inherited
	*/
	public static <A extends Annotation> List<A> findRepeatableAnnotations(
			AnnotatedElement element, Class<A> annotationType) {
		return AnnotationUtils.findRepeatableAnnotations(element, annotationType);
	}

	/**
	* Find all {@code public} {@linkplain Field fields} of the supplied class or interface that are
	* of the specified {@code fieldType} and annotated or <em>meta-annotated</em> with the specified
	* {@code annotationType}.
	*
	* <p>Consult the Javadoc for {@link Class#getFields()} for details on inheritance and ordering.
	*
	* @param clazz the class or interface in which to find the fields; never {@code null}
	* @param fieldType the type of field to find; never {@code null}
	* @param annotationType the annotation type to search for; never {@code null}
	* @return the list of all such fields found; never {@code null}
	* @see Class#getFields()
	*/
	public static List<Field> findPublicAnnotatedFields(
			Class<?> clazz, Class<?> fieldType, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.findPublicAnnotatedFields(clazz, fieldType, annotationType);
	}

	/**
	* Find all {@linkplain Method methods} of the supplied class or interface that are annotated or
	* <em>meta-annotated</em> with the specified {@code annotationType}.
	*
	* @param clazz the class or interface in which to find the methods; never {@code null}
	* @param annotationType the annotation type to search for; never {@code null}
	* @param sortOrder the method sort order; never {@code null}
	* @return the list of all such methods found; never {@code null}
	*/
	public static List<Method> findAnnotatedMethods(
			Class<?> clazz, Class<? extends Annotation> annotationType, MethodSortOrder sortOrder) {
		return AnnotationUtils.findAnnotatedMethods(
				clazz, annotationType, ReflectionUtils.MethodSortOrder.valueOf(sortOrder.name()));
	}
}
