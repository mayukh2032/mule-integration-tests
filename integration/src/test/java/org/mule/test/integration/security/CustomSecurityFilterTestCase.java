/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.security;

import org.mule.api.MuleMessage;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.tck.FunctionalTestCase;

import java.util.HashMap;

/**
 * See MULE-4916: spring beans inside a security filter
 */
public class CustomSecurityFilterTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/security/custom-security-filter-test.xml";
    }

    public void testOutboundAutenticationSend() throws Exception
    {
        DefaultLocalMuleClient client = new DefaultLocalMuleClient(muleContext);
        
        HashMap<String, Object> props = new HashMap<String,Object>();
        props.put("username", "ross");
        props.put("pass", "ross");
        MuleMessage result = client.send("vm://test", "hi", props);
        assertNull(result.getExceptionPayload());
        
        props.put("pass", "badpass");
        try
        {
            client.send("vm://test", "hi", props);
            fail("Exception expected");
        }
        catch (Exception e)
        {
            // expected
        }
    }
}