/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.routing;

import org.mule.api.ExceptionPayload;
import org.mule.api.MessagingException;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

public class FirstSuccessfulTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "first-successful-test.xml";
    }

    public void testFirstSuccessful() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage response = client.send("vm://input", "XYZ", null);
        assertEquals("XYZ is a string", response.getPayloadAsString());
        response = client.send("vm://input", Integer.valueOf(9), null);
        assertEquals("9 is an integer", response.getPayloadAsString());
        response = client.send("vm://input", Long.valueOf(42), null);
        assertEquals("42 is a number", response.getPayloadAsString());
        response = client.send("vm://input", Boolean.TRUE, null);
        ExceptionPayload ep = response.getExceptionPayload();
        assertNotNull(ep);
        Throwable ex = ep.getException();
        assertTrue(ex instanceof MessagingException);
    }

}
