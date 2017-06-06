/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.module.extension.oauth;

import org.mule.runtime.core.api.store.ListableObjectStore;

import org.junit.Test;

public class CustomStoreOAuthExtensionTestCase extends BaseOAuthExtensionTestCase {

  private static final String CUSTOM_STORE_NAME = "customStore";
  private ListableObjectStore objectStore;

  @Override
  protected String getConfigFile() {
    return "custom-store-oauth-extension-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    ownerId = CUSTOM_OWNER_ID;
    storedOwnerId = CUSTOM_OWNER_ID + "-oauth";
    objectStore = muleContext.getObjectStoreManager().getObjectStore(CUSTOM_STORE_NAME);
  }

  @Override
  protected void doTearDown() throws Exception {
    muleContext.getObjectStoreManager().disposeStore(objectStore);
  }

  @Test
  public void useCustomStore() throws Exception {
    simulateDanceStart();
    simulateCallback();

    assertOAuthStateStored(CUSTOM_STORE_NAME, storedOwnerId, ownerId);
  }
}
