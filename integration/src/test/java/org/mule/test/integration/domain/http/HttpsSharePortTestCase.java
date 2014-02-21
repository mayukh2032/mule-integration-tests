/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.domain.http;

import org.mule.tck.junit4.rule.SystemProperty;

public class HttpsSharePortTestCase extends HttpSharePortTestCase
{

    @Override
    protected String getDomainConfig()
    {
        return "domain/http/https-shared-connector.xml";
    }

    @Override
    protected SystemProperty getEndpointSchemeSystemProperty()
    {
        return new SystemProperty("scheme", "https");
    }
}
