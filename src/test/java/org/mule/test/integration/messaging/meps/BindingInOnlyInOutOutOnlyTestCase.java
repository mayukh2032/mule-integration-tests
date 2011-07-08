/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.messaging.meps;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.AbstractServiceAndFlowTestCase;

// START SNIPPET: full-class
public class BindingInOnlyInOutOutOnlyTestCase extends AbstractServiceAndFlowTestCase
{
    public static final long TIMEOUT = 3000;

    public BindingInOnlyInOutOutOnlyTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][] {
            {ConfigVariant.SERVICE,
                "org/mule/test/integration/messaging/meps/pattern_binding-In-Only_In-Out_Out-Only-service.xml"},
            {ConfigVariant.FLOW,
                "org/mule/test/integration/messaging/meps/pattern_binding-In-Only_In-Out_Out-Only-flow.xml"}});
    }

    @Test
    public void testExchange() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);

        client.dispatch("inboundEndpoint", new int[]{1, 2, 3, 4, 5}, null);

        MuleMessage result = client.request("receivedEndpoint", TIMEOUT);
        assertNotNull(result);
        assertEquals("Total: 15", result.getPayloadAsString());
    }
}
// END SNIPPET: full-class
