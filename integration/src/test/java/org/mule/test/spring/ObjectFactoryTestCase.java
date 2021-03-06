/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.spring;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.test.allure.AllureConstants.LifecycleAndDependencyInjectionFeature.LIFECYCLE_AND_DEPENDENCY_INJECTION;
import static org.mule.test.allure.AllureConstants.LifecycleAndDependencyInjectionFeature.ObjectFactoryStory.OBJECT_FACTORY_INECTION_AND_LIFECYCLE;
import org.mule.test.AbstractIntegrationTestCase;
import org.mule.tests.parsers.api.TestObject;
import org.mule.tests.parsers.api.TestObjectFactory;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(LIFECYCLE_AND_DEPENDENCY_INJECTION)
@Story(OBJECT_FACTORY_INECTION_AND_LIFECYCLE)
public class ObjectFactoryTestCase extends AbstractIntegrationTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/test/spring/object-factory-config.xml";
  }

  @Test
  public void validateInjectionAndLifecycleOverObjectFactoryAndTheObjectCreatedByIt() {
    TestObject testObject = registry.lookupByType(TestObject.class).get();
    assertThat(testObject, notNullValue());

    TestObjectFactory objectFactory = testObject.getObjectFactory();
    assertThat(objectFactory.isInjectionDoneBeforeGetObject(), is(true));
    assertThat(objectFactory.getLifecycleActions().isEmpty(), is(true));

    assertThat(testObject.getLockFactory(), is(notNullValue()));

    muleContext.dispose();

    assertThat(testObject.getLifecycleActions(), contains("initialise", "start", "stop", "dispose"));
    assertThat(objectFactory.getLifecycleActions().isEmpty(), is(true));
  }
}
