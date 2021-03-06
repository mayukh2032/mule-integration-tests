/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.exceptions;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mule.functional.junit4.matchers.ThrowableMessageMatcher.hasMessage;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.CRITICAL_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.SOURCE_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.SOURCE_ERROR_RESPONSE_GENERATE_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.SOURCE_ERROR_RESPONSE_SEND_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.SOURCE_RESPONSE_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.SOURCE_RESPONSE_GENERATE_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.SOURCE_RESPONSE_SEND_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.UNKNOWN_ERROR_IDENTIFIER;
import static org.mule.runtime.module.extension.api.loader.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static org.mule.runtime.module.extension.api.loader.AbstractJavaExtensionModelLoader.VERSION;
import static org.mule.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mule.extension.http.internal.temporary.HttpConnector;
import org.mule.extension.socket.api.SocketsExtension;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.module.extension.api.loader.java.DefaultJavaExtensionModelLoader;
import org.mule.test.integration.AbstractConfigurationFailuresTestCase;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(ERROR_HANDLING)
@Story("Validations")
public class ErrorHandlingConfigurationFailuresTestCase extends AbstractConfigurationFailuresTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void errorHandlerCantHaveOnErrorWithoutTypeOrExpression() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException
        .expectMessage(containsString("Every handler (except for the last one) within an 'error-handler' must specify a 'when' or 'type' attribute."));
    loadConfiguration("org/mule/test/integration/exceptions/exception-strategy-in-choice-without-expression.xml");
  }

  @Test
  public void onErrorReferenceMustHaveTypeOrExpression() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException
        .expectMessage(containsString("Every handler (except for the last one) within an 'error-handler' must specify a 'when' or 'type' attribute."));
    loadConfiguration("org/mule/test/integration/exceptions/on-error-ref-without-matcher-config.xml");
  }

  // TODO MULE-10061 - Review once the MuleContext lifecycle is clearly defined
  @Test
  public void defaultErrorHandlerReferencesNonExistentErrorHandler() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("No global error handler defined with name 'nonExistentEh'."));
    loadConfiguration("org/mule/test/integration/exceptions/default-error-handler-reference-non-existent-es.xml");
  }

  @Test
  public void errorHandlerReferenceAndDefinitionNotAllowed() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(containsString("A reference error-handler cannot have on-errors."));
    loadConfiguration("org/mule/test/integration/exceptions/error-handler-reference-and-definition-config.xml");
  }

  @Test
  public void xaTransactionalTryNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("Unable to create Try Scope with a Transaction Type: [XA]"));
    loadConfiguration("org/mule/test/integration/transaction/xa-transactional-try-config.xml");
  }

  @Test
  public void unknownErrorFilteringNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectCause(hasMessage(equalTo(notFound(UNKNOWN_ERROR_IDENTIFIER))));
    loadConfiguration("org/mule/test/integration/exceptions/unknown-error-filtering-config.xml");
  }

  @Test
  public void sourceErrorResponseFilteringNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectCause(hasMessage(equalTo(notFound(SOURCE_ERROR_IDENTIFIER))));
    loadConfiguration("org/mule/test/integration/exceptions/source-error-response-filtering-config.xml");
  }

  @Test
  public void sourceErrorResponseSendFilteringNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectCause(hasMessage(equalTo(notFound(SOURCE_ERROR_RESPONSE_SEND_ERROR_IDENTIFIER))));
    loadConfiguration("org/mule/test/integration/exceptions/source-error-response-send-filtering-config.xml");
  }

  @Test
  public void sourceErrorResponseGenerateFilteringNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectCause(hasMessage(equalTo(notFound(SOURCE_ERROR_RESPONSE_GENERATE_ERROR_IDENTIFIER))));
    loadConfiguration("org/mule/test/integration/exceptions/source-error-response-generate-filtering-config.xml");
  }

  @Test
  public void criticalErrorFilteringNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectCause(hasMessage(equalTo(notFound(CRITICAL_IDENTIFIER))));
    loadConfiguration("org/mule/test/integration/exceptions/critical-error-filtering-config.xml");
  }

  @Test
  public void severalAnyMappingsNotAllowed() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(containsString("Only one mapping for 'ANY' or an empty source type is allowed."));
    loadConfiguration("org/mule/test/integration/exceptions/several-any-mappings-config.xml");
  }

  @Test
  public void nonExistingSourceMappingNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("Could not find error 'NON_EXISTING'."));
    loadConfiguration("org/mule/test/integration/exceptions/non-existing-source-mapping-config.xml");
  }

  @Test
  public void middleAnyMappingsNotAllowed() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(containsString("Only the last error mapping can have 'ANY' or an empty source type."));
    loadConfiguration("org/mule/test/integration/exceptions/middle-any-mapping-config.xml");
  }

  @Test
  public void repeatedMappingsNotAllowed() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException
        .expectMessage(containsString("Repeated source types are not allowed. Offending types are 'ROUTING', 'EXPRESSION'."));
    loadConfiguration("org/mule/test/integration/exceptions/repeated-mappings-config.xml");
  }

  @Test
  public void nonExistingCoreMappingsNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("There's no MULE error named 'NON_EXISTING'."));
    loadConfiguration("org/mule/test/integration/exceptions/non-existent-core-mapping-config.xml");
  }

  @Test
  public void usedNamespaceMappingsNotAllowed() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("Cannot use error type 'HTTP:NOT_FOUND': namespace already exists."));
    loadConfiguration("org/mule/test/integration/exceptions/used-namespace-mappings-config.xml");
  }

  @Test
  public void nonExistingCoreErrorCannotBeRaised() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("There's no MULE error named 'NONEXISTENT'."));
    loadConfiguration("org/mule/test/integration/exceptions/non-existent-core-raise-error-config.xml");
  }

  @Test
  public void usedNamespaceErrorCannotBeRaised() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(containsString("Cannot use error type 'HTTP:TIMEOUT': namespace already exists."));
    loadConfiguration("org/mule/test/integration/exceptions/used-namespace-raise-error-config.xml");
  }

  @Test
  public void sourceResponseGenerateOnErrorContinue() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(equalTo(notAllowed(SOURCE_RESPONSE_GENERATE_ERROR_IDENTIFIER)));
    loadConfiguration("org/mule/test/integration/exceptions/on-error-continue-response-generate.xml");
  }

  @Test
  public void sourceResponseSendOnErrorContinue() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(equalTo(notAllowed(SOURCE_RESPONSE_SEND_ERROR_IDENTIFIER)));
    loadConfiguration("org/mule/test/integration/exceptions/on-error-continue-response-send.xml");
  }

  @Test
  public void sourceResponseErrorOnErrorContinue() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(equalTo(notAllowed(SOURCE_RESPONSE_ERROR_IDENTIFIER)));
    loadConfiguration("org/mule/test/integration/exceptions/on-error-continue-response-error.xml");
  }

  @Test
  public void sourceErrorInListOnErrorContinue() throws Exception {
    expectedException.expect(InitialisationException.class);
    expectedException.expectMessage(equalTo(notAllowed(SOURCE_RESPONSE_ERROR_IDENTIFIER)));
    loadConfiguration("org/mule/test/integration/exceptions/on-error-continue-source-error-list.xml");
  }

  private String notFound(String type) {
    return format("Could not find ErrorType for the given identifier: '%s'", type);
  }

  private String notAllowed(String type) {
    return format("Source errors are not allowed in 'on-error-continue' handlers. Offending type is '%s'.", type);
  }

  protected List<ExtensionModel> getRequiredExtensions() {
    ExtensionModel sockets = loadExtension(SocketsExtension.class, emptySet());
    ExtensionModel http = loadExtension(HttpConnector.class, singleton(sockets));
    return asList(http, sockets);
  }

  protected ExtensionModel loadExtension(Class extension, Set<ExtensionModel> deps) {
    DefaultJavaExtensionModelLoader loader = new DefaultJavaExtensionModelLoader();
    Map<String, Object> ctx = new HashMap<>();
    ctx.put(TYPE_PROPERTY_NAME, extension.getName());
    ctx.put(VERSION, "1.0.0-SNAPSHOT");
    return loader.loadExtensionModel(currentThread().getContextClassLoader(), DslResolvingContext.getDefault(deps), ctx);
  }
}
