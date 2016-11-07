/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.execution;

import java.util.Map;

/**
 * Base class which provides a simplified view of {@link CompletionHandler} for cases
 * in which the task to be completed is assumed to be blocking
 * <p>
 * The {@link CompletionHandler#onCompletion(Object, Map, ExceptionCallback)} method has a final implementation
 * which delegates into {@link #doOnCompletion(Object)} and automatically catches any exception
 * and channels it through the {@link ExceptionCallback}
 *
 * @param <R> the generic type of the result objects
 */
public abstract class BlockingCompletionHandler<R, E extends Throwable> implements CompletionHandler<R, E> {

  /**
   * Delegates into {@link #doOnCompletion(Object)} and channels any thrown exceptions
   * through the {@code exceptionCallback}
   *  @param result            the result of processing
   * @param parameters
   * @param exceptionCallback handles errors processing the {@code result}
   */
  @Override
  public final void onCompletion(R result, Map<String, Object> parameters, ExceptionCallback<Throwable> exceptionCallback) {
    try {
      doOnCompletion(result);
    } catch (Exception e) {
      exceptionCallback.onException(e);
    }
  }

  /**
   * Provides a simplified view of the {@link CompletionHandler#onCompletion(Object, Map, ExceptionCallback)} contract
   * that assumes that the operation is blocking, which makes it unnecessary to explicitly
   * channel exceptions to an {@link ExceptionCallback}
   *
   * @param result the result of processing
   */
  protected abstract void doOnCompletion(R result);
}