/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.exceptions;

import static org.junit.Assert.assertNotNull;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class MessageContextTestCase extends AbstractServiceAndFlowTestCase
{
    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/test/integration/exceptions/message-context-test-service.xml"},
            {ConfigVariant.FLOW, "org/mule/test/integration/exceptions/message-context-test-flow.xml"}
        });
    }

    public MessageContextTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    /**
     * Test for MULE-4361
     */
    @Test
    public void testAlternateExceptionStrategy() throws Exception
    {
        MuleClient client = muleContext.getClient();
        DefaultMuleMessage msg = new DefaultMuleMessage("Hello World", muleContext);
        MuleMessage response = client.send("testin", msg, 200000);
        assertNotNull(response);
    }
}
