/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.oauth2.internal.authorizationcode;

import static org.mule.extension.http.api.HttpHeaders.Names.AUTHORIZATION;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import org.mule.extension.oauth2.api.RequestAuthenticationException;
import org.mule.extension.oauth2.internal.AbstractGrantType;
import org.mule.extension.oauth2.internal.authorizationcode.state.ConfigOAuthContext;
import org.mule.extension.oauth2.internal.authorizationcode.state.ResourceOwnerOAuthContext;
import org.mule.extension.oauth2.internal.tokenmanager.TokenManagerConfig;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.core.util.AttributeEvaluator;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.module.http.api.listener.HttpListenerConfig;
import org.mule.service.http.api.HttpService;
import org.mule.service.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.service.http.api.server.HttpServer;
import org.mule.service.http.api.server.HttpServerConfiguration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the config element for oauth:authentication-code-config.
 * <p/>
 * This config will: - If the authorization-request is defined then it will create a flow listening for an user call to begin the
 * oauth login. - If the token-request is defined then it will create a flow for listening in the redirect uri so we can get the
 * authentication code and retrieve the access token
 */
<<<<<<< Upstream, based on origin/mule-4.x
public class DefaultAuthorizationCodeGrantType extends AbstractGrantType implements Lifecycle, AuthorizationCodeGrantType {
=======
@Alias("authorization-code-grant-type")
public class DefaultAuthorizationCodeGrantType extends AbstractGrantType
    implements Initialisable, AuthorizationCodeGrantType, Startable, Stoppable, MuleContextAware {
>>>>>>> 3a5ba3e starting migration

  private static final Logger logger = LoggerFactory.getLogger(DefaultAuthorizationCodeGrantType.class);

  private String clientId;
  private String clientSecret;
  private HttpListenerConfig localCallbackConfig;
  private String localCallbackConfigPath;
  private String localCallbackUrl;
  private String externalCallbackUrl;
  private AuthorizationRequestHandler authorizationRequestHandler;
  private AbstractAuthorizationCodeTokenRequestHandler tokenRequestHandler;
  @Inject
  private MuleContext muleContext;
  @Inject
  private HttpService httpService;
  @Inject
  private SchedulerService schedulerService;
  private TlsContextFactory tlsContextFactory;
  private TokenManagerConfig tokenManagerConfig;
  private AttributeEvaluator localAuthorizationUrlResourceOwnerIdEvaluator;
  private AttributeEvaluator resourceOwnerIdEvaluator;

  private HttpServer server;

  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  public void setClientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public void setAuthorizationRequestHandler(final AuthorizationRequestHandler authorizationRequestHandler) {
    this.authorizationRequestHandler = authorizationRequestHandler;
  }

  public void setTokenRequestHandler(final AbstractAuthorizationCodeTokenRequestHandler tokenRequestHandler) {
    this.tokenRequestHandler = tokenRequestHandler;
  }

  public void setLocalCallbackConfig(HttpListenerConfig localCallbackConfig) {
    this.localCallbackConfig = localCallbackConfig;
  }

  public void setLocalCallbackConfigPath(String localCallbackConfigPath) {
    this.localCallbackConfigPath = localCallbackConfigPath;
  }

  public void setLocalCallbackUrl(String localCallbackUrl) {
    this.localCallbackUrl = localCallbackUrl;
  }

  public void setExternalCallbackUrl(String externalCallbackUrl) {
    this.externalCallbackUrl = externalCallbackUrl;
  }

  @Override
  public HttpListenerConfig getLocalCallbackConfig() {
    return localCallbackConfig;
  }

  @Override
  public String getLocalCallbackConfigPath() {
    return localCallbackConfigPath;
  }

  @Override
  public String getLocalCallbackUrl() {
    return localCallbackUrl;
  }

  @Override
  public String getExternalCallbackUrl() {
    return externalCallbackUrl;
  }

  public ConfigOAuthContext getConfigOAuthContext() {
    return tokenManagerConfig.getConfigOAuthContext();
  }

  @Override
  public String getRefreshTokenWhen() {
    return tokenRequestHandler.getRefreshTokenWhen();
  }

  @Override
  public AttributeEvaluator getLocalAuthorizationUrlResourceOwnerIdEvaluator() {
    return localAuthorizationUrlResourceOwnerIdEvaluator;
  }

  @Override
  public AttributeEvaluator getResourceOwnerIdEvaluator() {
    return resourceOwnerIdEvaluator;
  }

  @Override
  public void refreshToken(final Event currentFlowEvent, final String resourceOwnerId) throws MuleException {
    tokenRequestHandler.refreshToken(currentFlowEvent, resourceOwnerId);
  }

  @Override
  public ConfigOAuthContext getUserOAuthContext() {
    return tokenManagerConfig.getConfigOAuthContext();
  }

  @Override
  public String getClientSecret() {
    return clientSecret;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  @Override
  public TlsContextFactory getTlsContext() {
    return tlsContextFactory;
  }

  public void setTlsContext(TlsContextFactory tlsContextFactory) {
    this.tlsContextFactory = tlsContextFactory;
  }

  @Override
  public void initialise() throws InitialisationException {
    try {
      if (tokenManagerConfig == null) {
        this.tokenManagerConfig = TokenManagerConfig.createDefault(muleContext);
        this.tokenManagerConfig.initialise();
      }
      if (localAuthorizationUrlResourceOwnerIdEvaluator == null) {
        localAuthorizationUrlResourceOwnerIdEvaluator = new AttributeEvaluator(null);
      }
      localAuthorizationUrlResourceOwnerIdEvaluator.initialize(muleContext.getExpressionManager());
      if (resourceOwnerIdEvaluator == null) {
        resourceOwnerIdEvaluator = new AttributeEvaluator(ResourceOwnerOAuthContext.DEFAULT_RESOURCE_OWNER_ID);
      }
      resourceOwnerIdEvaluator.initialize(muleContext.getExpressionManager());
      if (localCallbackConfig != null && localCallbackUrl != null) {
        throw new IllegalArgumentException("Attributes localCallbackConfig and localCallbackUrl are mutually exclusive");
      }
      if ((localCallbackConfig == null) != (localCallbackConfigPath == null)) {
        throw new IllegalArgumentException("Attributes localCallbackConfig and localCallbackConfigPath must be both present or absent");
      }

      if (tlsContextFactory != null) {
        initialiseIfNeeded(tlsContextFactory);
        tokenRequestHandler.setTlsContextFactory(tlsContextFactory);
      }
      tokenRequestHandler.initialise();

      buildHttpServer();
    } catch (Exception e) {
      throw new InitialisationException(e, this);
    }
  }

  private void buildHttpServer() throws InitialisationException {
    final HttpServerConfiguration.Builder serverConfigBuilder = new HttpServerConfiguration.Builder();

    if (getLocalCallbackUrl() != null) {
      try {
        final URL localCallbackUrl = new URL(getLocalCallbackUrl());
        serverConfigBuilder.setHost(localCallbackUrl.getHost()).setPort(localCallbackUrl.getPort());
      } catch (MalformedURLException e) {
        logger.warn("Could not parse provided url %s. Validate that the url is correct", getLocalCallbackUrl());
        throw new InitialisationException(e, this);
      }
    } else if (getLocalCallbackConfig() != null) {
      serverConfigBuilder
          .setHost(getLocalCallbackConfig().getHost())
          .setPort(getLocalCallbackConfig().getPort())
          .setTlsContextFactory(getLocalCallbackConfig().getTlsContext());
    } else {
      throw new IllegalStateException("No localCallbackUrl or localCallbackConfig defined.");
    }

    if (getTlsContext() != null) {
      serverConfigBuilder.setTlsContextFactory(getTlsContext());
    }

    // TODO MULE-11272 Change to cpu-lite
    HttpServerConfiguration serverConfiguration =
        serverConfigBuilder.setSchedulerSupplier(() -> schedulerService.ioScheduler()).build();

    try {
      server = httpService.getServerFactory().create(serverConfiguration);
    } catch (ConnectionException e) {
      logger.warn("Could not create server for OAuth callback.");
      throw new InitialisationException(e, this);
    }

  }

  @Override
  public void authenticate(Event muleEvent, HttpRequestBuilder builder) throws MuleException {
    final String resourceOwnerId = resourceOwnerIdEvaluator.resolveStringValue(muleEvent);
    if (resourceOwnerId == null) {
      throw new RequestAuthenticationException(createStaticMessage(String
          .format("Evaluation of %s return an empty resourceOwnerId",
                  localAuthorizationUrlResourceOwnerIdEvaluator.getRawValue())));
    }
    final String accessToken = getUserOAuthContext().getContextForResourceOwner(resourceOwnerId).getAccessToken();
    if (accessToken == null) {
      throw new RequestAuthenticationException(createStaticMessage(String.format(
                                                                                 "No access token for the %s user. Verify that you have authenticated the user before trying to execute an operation to the API.",
                                                                                 resourceOwnerId)));
    }
    builder.addHeader(AUTHORIZATION, buildAuthorizationHeaderContent(accessToken));
  }

  @Override
  public boolean shouldRetry(final Event firstAttemptResponseEvent) throws MuleException {
    if (!StringUtils.isBlank(getRefreshTokenWhen())) {
      final Object value =
          muleContext.getExpressionManager().evaluate(getRefreshTokenWhen(), firstAttemptResponseEvent).getValue();
      if (!(value instanceof Boolean)) {
        throw new MuleRuntimeException(createStaticMessage("Expression %s should return a boolean but return %s",
                                                           getRefreshTokenWhen(), value));
      }
      Boolean shouldRetryRequest = (Boolean) value;
      if (shouldRetryRequest) {
        try {
          refreshToken(firstAttemptResponseEvent, resourceOwnerIdEvaluator.resolveStringValue(firstAttemptResponseEvent));
        } catch (MuleException e) {
          throw new MuleRuntimeException(e);
        }
      }
      return shouldRetryRequest;
    }
    return false;
  }

  public void setLocalAuthorizationUrlResourceOwnerId(final String resourceOwnerId) {
    localAuthorizationUrlResourceOwnerIdEvaluator = new AttributeEvaluator(resourceOwnerId);
  }

  public void setResourceOwnerId(String resourceOwnerId) {
    this.resourceOwnerIdEvaluator = new AttributeEvaluator(resourceOwnerId);
  }

  public void setTokenManager(TokenManagerConfig tokenManagerConfig) {
    this.tokenManagerConfig = tokenManagerConfig;
  }

  @Override
  public void start() throws MuleException {
    try {
      server.start();
    } catch (IOException e) {
      throw new DefaultMuleException(e);
    }

    if (authorizationRequestHandler != null) {
      authorizationRequestHandler.setOauthConfig(this);
      authorizationRequestHandler.init();
      authorizationRequestHandler.start();
    }
    if (tokenRequestHandler != null) {
      tokenRequestHandler.setOauthConfig(this);
      tokenRequestHandler.init();
      tokenRequestHandler.start();
    }
  }

  @Override
  public void stop() throws MuleException {
    tokenRequestHandler.stop();
    authorizationRequestHandler.stop();
    server.stop();
  }

  @Override
  public void dispose() {
    server.dispose();
  }

  @Override
  public HttpServer getServer() {
    return server;
  }
}
