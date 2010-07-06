/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.endpoints;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

public class EndpointMessageProcessorsTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/endpoints/endpoint-message-processors.xml";
    }

    public void testSynchronousOutbound() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        
        MuleMessage response = client.send("vm://in1", "input", null);
        assertNotNull(response);
        assertEquals("input:A:B:service1:E:F:service2:G:H:C:D", response.getPayload());
    }

    public void testAsynchronousOutbound() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        
        MuleMessage response = client.send("vm://in2", "input", null);
        assertNotNull(response);
        assertEquals("input:A:B:service1:C:D", response.getPayload());

        response = client.request("vm://out", 1000);
        assertNotNull(response);
        assertEquals("input:A:B:service1:E:F", response.getPayload());
    }
}

