/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.core.endpoint.inbound;

import org.mule.compatibility.core.api.endpoint.InboundEndpoint;
import org.mule.compatibility.core.context.notification.EndpointMessageNotification;
import org.mule.compatibility.core.transport.AbstractConnector;
import org.mule.runtime.core.AbstractAnnotatedObject;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.util.ObjectUtils;

/**
 * Publishes a {@link EndpointMessageNotification}'s when a message is received.
 */
public class InboundNotificationMessageProcessor extends AbstractAnnotatedObject implements Processor {

  protected InboundEndpoint endpoint;

  public InboundNotificationMessageProcessor(InboundEndpoint endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public Event process(Event event) throws MuleException {
    AbstractConnector connector = (AbstractConnector) endpoint.getConnector();
    if (connector.isEnableMessageEvents(endpoint.getMuleContext())) {
      connector.fireNotification(new EndpointMessageNotification(event.getMessage(), endpoint, endpoint.getFlowConstruct(),
                                                                 EndpointMessageNotification.MESSAGE_RECEIVED));
    }

    return event;
  }

  /**
   * @return underlying {@link InboundEndpoint}
   */
  public final InboundEndpoint getInboundEndpoint() {
    return this.endpoint;
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
