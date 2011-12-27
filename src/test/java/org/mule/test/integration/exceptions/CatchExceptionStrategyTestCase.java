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

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.api.processor.MessageProcessor;
import org.mule.tck.AbstractServiceAndFlowTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThat;

public class CatchExceptionStrategyTestCase extends AbstractServiceAndFlowTestCase
{
    public static final int TIMEOUT = 5000;
    public static final String ERROR_PROCESSING_NEWS = "error processing news";
    @Rule
    public DynamicPort dynamicPort1 = new DynamicPort("port1");
    @Rule
    public DynamicPort dynamicPort2 = new DynamicPort("port2");
    @Rule
    public DynamicPort dynamicPort3 = new DynamicPort("port3");
    @Rule
    public DynamicPort dynamicPort4 = new DynamicPort("port4");

    public CatchExceptionStrategyTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{/*{AbstractServiceAndFlowTestCase.ConfigVariant.SERVICE, "org/mule/test/exceptions/catch-exception-strategy-use-case-service.xml"},*/
                {ConfigVariant.FLOW, "org/mule/test/integration/exceptions/catch-exception-strategy-use-case-flow.xml"}});
    }

    @Test
    public void testHttpJsonErrorResponse() throws Exception
    {
        testJsonErrorResponse(String.format("http://localhost:%s/service", dynamicPort1.getNumber()));
    }

    @Test
    public void testHttpsJsonErrorResponse() throws Exception
    {
        testJsonErrorResponse(String.format("https://localhost:%s/httpsservice", dynamicPort3.getNumber()));
    }

    @Test
    public void testVmJsonErrorResponse() throws Exception
    {
        testJsonErrorResponse("vm://in");
    }

    @Test
    public void testJmsJsonErrorResponse() throws Exception
    {
        testJsonErrorResponse("jms://in");
    }

    @Test
    public void testTcpJsonErrorResponse() throws Exception
    {
        testJsonErrorResponse(String.format("tcp://localhost:%s",dynamicPort2.getNumber()));
    }

    @Test
    @Ignore
    public void testCxfHttpJsonErrorResponse() throws Exception
    {
        LocalMuleClient client = muleContext.getClient();
        MuleMessage response = client.send(String.format("http://localhost:%s/cxfservice", dynamicPort4.getNumber()), "", null, TIMEOUT);
        assertThat(response, IsNull.<Object>notNullValue());
        assertThat(response.getPayloadAsString(), Is.is("{\"errorMessage\":\"error processing news\",\"userId\":15,\"title\":\"News title\"}"));
    }

    private void testJsonErrorResponse(String endpointUri) throws Exception
    {
        LocalMuleClient client = muleContext.getClient();
        MuleMessage response = client.send(endpointUri, "{\"userId\":\"15\"}", null, TIMEOUT);
        assertThat(response, IsNull.<Object>notNullValue());
        assertThat(response.getPayloadAsString(), Is.is("{\"errorMessage\":\"error processing news\",\"userId\":15,\"title\":\"News title\"}"));
    }


    public static class LoadNewsProcessor implements MessageProcessor
    {
        @Override
        public MuleEvent process(MuleEvent event) throws MuleException
        {
            NewsRequest newsRequest = (NewsRequest) event.getMessage().getPayload();
            NewsResponse newsResponse = new NewsResponse();
            newsResponse.setUserId(newsRequest.getUserId());
            newsResponse.setTitle("News title");
            event.getMessage().setPayload(newsResponse);
            return event;
        }
    }

    public static class NewsErrorProcessor implements MessageProcessor
    {

        @Override
        public MuleEvent process(MuleEvent event) throws MuleException
        {
            ((NewsResponse)event.getMessage().getPayload()).setErrorMessage(ERROR_PROCESSING_NEWS);
            return event;
        }
    }

    public static class NewsRequest
    {
        private int userId;

        public int getUserId()
        {
            return userId;
        }

        public void setUserId(int userId)
        {
            this.userId = userId;
        }
    }

    public static class NewsResponse
    {
        private int userId;
        private String title;
        private String errorMessage;

        public int getUserId()
        {
            return userId;
        }

        public void setUserId(int userId)
        {
            this.userId = userId;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage)
        {
            this.errorMessage = errorMessage;
        }
    }

    @WebService
    public static class Echo
    {
        @WebResult(name="text")
        public String echo(@WebParam(name="text") String string)
        {
            return string;
        }
    }

}
