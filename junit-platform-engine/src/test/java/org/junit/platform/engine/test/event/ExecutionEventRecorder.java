/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.engine.test.event;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.FINISHED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.SKIPPED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.STARTED;
import static org.junit.platform.engine.test.event.ExecutionEvent.byPayload;
import static org.junit.platform.engine.test.event.ExecutionEvent.byTestDescriptor;
import static org.junit.platform.engine.test.event.ExecutionEvent.byType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.event.ExecutionEvent.Type;

/**
* {@link EngineExecutionListener} that records all events and makes them available to tests.
*
* @since 1.0
* @see ExecutionEvent
*/
public class ExecutionEventRecorder implements EngineExecutionListener {

	public static List<ExecutionEvent> execute(
			TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
		TestDescriptor engineTestDescriptor =
				testEngine.discover(discoveryRequest, UniqueId.forEngine(testEngine.getId()));
		ExecutionEventRecorder listener = new ExecutionEventRecorder();
		testEngine.execute(
				new ExecutionRequest(
						engineTestDescriptor, listener, discoveryRequest.getConfigurationParameters()));
		return listener.getExecutionEvents();
	}

	public final List<ExecutionEvent> executionEvents = new CopyOnWriteArrayList<>();

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		addEvent(ExecutionEvent.dynamicTestRegistered(testDescriptor));
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		addEvent(ExecutionEvent.executionSkipped(testDescriptor, reason));
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		addEvent(ExecutionEvent.executionStarted(testDescriptor));
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		addEvent(ExecutionEvent.executionFinished(testDescriptor, result));
	}

	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		addEvent(ExecutionEvent.reportingEntryPublished(testDescriptor, entry));
	}

	public List<ExecutionEvent> getExecutionEvents() {
		return executionEvents;
	}

	public Stream<ExecutionEvent> eventStream() {
		return getExecutionEvents().stream();
	}

	public long getTestSkippedCount() {
		return testEventsByType(SKIPPED).count();
	}

	public long getTestStartedCount() {
		return testEventsByType(STARTED).count();
	}

	public long getReportingEntryPublishedCount() {
		return testEventsByType(REPORTING_ENTRY_PUBLISHED).count();
	}

	public long getDynamicTestRegisteredCount() {
		return testEventsByType(DYNAMIC_TEST_REGISTERED).count();
	}

	public long getTestFinishedCount() {
		return testEventsByType(FINISHED).count();
	}

	public long getTestSuccessfulCount() {
		return getTestFinishedCount(Status.SUCCESSFUL);
	}

	public long getTestAbortedCount() {
		return getTestFinishedCount(Status.ABORTED);
	}

	public long getTestFailedCount() {
		return getTestFinishedCount(Status.FAILED);
	}

	public long getContainerSkippedCount() {
		return containerEventsByType(SKIPPED).count();
	}

	public long getContainerStartedCount() {
		return containerEventsByType(STARTED).count();
	}

	public long getContainerFinishedCount() {
		return containerEventsByType(FINISHED).count();
	}

	public List<ExecutionEvent> getSkippedTestEvents() {
		return testEventsByType(Type.SKIPPED).collect(toList());
	}

	public List<ExecutionEvent> getSuccessfulTestFinishedEvents() {
		return testFinishedEvents(Status.SUCCESSFUL).collect(toList());
	}

	public List<ExecutionEvent> getFailedTestFinishedEvents() {
		return testFinishedEvents(Status.FAILED).collect(toList());
	}

	private long getTestFinishedCount(Status status) {
		return testFinishedEvents(status).count();
	}

	private Stream<ExecutionEvent> testFinishedEvents(Status status) {
		return testEventsByType(FINISHED)
				.filter(
						byPayload(
								TestExecutionResult.class, where(TestExecutionResult::getStatus, isEqual(status))));
	}

	private Stream<ExecutionEvent> testEventsByType(Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isTest);
	}

	private Stream<ExecutionEvent> containerEventsByType(Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isContainer);
	}

	private Stream<ExecutionEvent> eventsByTypeAndTestDescriptor(
			Type type, Predicate<? super TestDescriptor> predicate) {
		return eventStream().filter(byType(type).and(byTestDescriptor(predicate)));
	}

	private void addEvent(ExecutionEvent event) {
		executionEvents.add(event);
	}
}
