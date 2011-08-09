/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.construct;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageDispatcher;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.vm.VMMessageDispatcher;
import org.mule.transport.vm.VMMessageDispatcherFactory;
import org.mule.transport.vm.VMMessageReceiver;

public class FlowDefaultProcessingStrategyTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/construct/flow-default-processing-strategy-config.xml";
    }

    public void testDispatchToOneWayInbound() throws Exception
    {
        MuleClient client = muleContext.getClient();
        client.dispatch("vm://oneway-in", "a", null);

        MuleMessage result = client.request("vm://oneway-out", RECEIVE_TIMEOUT);

        assertAllProcessingAsync(result);
    }

    public void testSendToOneWayInbound() throws Exception
    {
        MuleClient client = muleContext.getClient();
        MuleMessage response = client.send("vm://oneway-in", "a", null);

        assertNull(response);

        MuleMessage result = client.request("vm://oneway-out", RECEIVE_TIMEOUT);

        assertNotNull(result);

        String receiverThread = result.getInboundProperty("receiver-thread");
        String flowThread = result.getInboundProperty("processor-thread");
        String dispatcherThread = result.getInboundProperty("dispatcher-thread");

        assertEquals(Thread.currentThread().getName(), receiverThread);
        assertFalse(receiverThread.equals(flowThread));
        assertFalse(flowThread.equals(dispatcherThread));
    }

    public void testDispatchToOneWayTxInbound() throws Exception
    {
        MuleClient client = muleContext.getClient();
        client.dispatch("vm://onewaytx-in", "a", null);

        MuleMessage result = client.request("vm://onewaytx-out", RECEIVE_TIMEOUT);

        assertAllProcessingInRecieverThread(result);
    }

    public void testSendToOneWayTxInbound() throws Exception
    {
        MuleClient client = muleContext.getClient();
        MuleMessage response = client.send("vm://onewaytx-in", "a", null);

        assertNull(response);

        MuleMessage result = client.request("vm://onewaytx-out", RECEIVE_TIMEOUT);
        assertAllProcessingInClientThread(result);
    }

    public void testSendRequestResponseInbound() throws Exception
    {
        MuleClient client = muleContext.getClient();
        MuleMessage response = client.send("vm://requestresponse-in", "a", null);

        assertAllProcessingInClientThread(response);
    }

    public void testDispatchToRequestResponseInboundOneWayOutbound() throws Exception
    {
        MuleClient client = muleContext.getClient();

        client.dispatch("vm://requestresponse-oneway-in", "a", null);

        // Message never gets to reciever as receiver is not polling the queue
        assertNull(client.request("vm://requestresponse-oneway-out", RECEIVE_TIMEOUT));
    }

    public void testSendToRequestResponseInboundOneWayOutbound() throws Exception
    {
        MuleClient client = muleContext.getClient();

        MuleMessage response = client.send("vm://requestresponse-oneway-in", "a", null);
        assertNull(response);

        MuleMessage result = client.request("vm://requestresponse-oneway-out", RECEIVE_TIMEOUT);

        assertAllProcessingInClientThread(result);
    }

    protected void assertAllProcessingInClientThread(MuleMessage result)
    {
        assertSync(result);
        assertEquals(Thread.currentThread().getName(), result.getInboundProperty("receiver-thread"));
    }

    protected void assertAllProcessingInRecieverThread(MuleMessage result)
    {
        assertSync(result);
        assertTrue(((String) result.getInboundProperty("receiver-thread")).startsWith("vm.receiver"));
    }

    protected void assertSync(MuleMessage result)
    {
        assertNotNull(result);

        String receiverThread = result.getInboundProperty("receiver-thread");
        String flowThread = result.getInboundProperty("processor-thread");
        String dispatcherThread = result.getInboundProperty("dispatcher-thread");

        assertEquals(receiverThread, flowThread);
        assertEquals(flowThread, dispatcherThread);
    }

    protected void assertAllProcessingAsync(MuleMessage result)
    {
        assertNotNull(result);

        String receiverThread = result.getInboundProperty("receiver-thread");
        String flowThread = result.getInboundProperty("processor-thread");
        String dispatcherThread = result.getInboundProperty("dispatcher-thread");

        assertTrue(receiverThread.startsWith("vm.receiver"));
        assertFalse(receiverThread.equals(flowThread));
        assertFalse(flowThread.equals(dispatcherThread));
        assertFalse(receiverThread.equals(dispatcherThread));
    }

    public void testRequestResponseInboundFailingOneWayOutbound() throws Exception
    {
        MuleClient client = muleContext.getClient();

        try
        {
            MuleMessage response = client.send("vm://requestresponse-failingoneway-in", "a", null);
            fail("exception expected");
        }
        catch (Exception e)
        {

        }
    }

    public static class ThreadSensingMessageProcessor implements MessageProcessor
    {
        @Override
        public MuleEvent process(MuleEvent event) throws MuleException
        {
            event.getMessage().setOutboundProperty("processor-thread", Thread.currentThread().getName());
            return event;
        }
    }

    public static class ThreadSensingVMMessageDispatcherFactory extends VMMessageDispatcherFactory
    {

        @Override
        public MessageDispatcher create(OutboundEndpoint endpoint) throws MuleException
        {
            return new ThreadSensingVMMessageDispatcher(endpoint);
        }
    }

    public static class ThreadSensingVMMessageDispatcher extends VMMessageDispatcher
    {
        public ThreadSensingVMMessageDispatcher(OutboundEndpoint endpoint)
        {
            super(endpoint);
        }

        @Override
        protected void doDispatch(MuleEvent event) throws Exception
        {
            event.getMessage().setOutboundProperty("dispatcher-thread", Thread.currentThread().getName());
            super.doDispatch(event);
        }

        @Override
        protected MuleMessage doSend(MuleEvent event) throws Exception
        {
            event.getMessage().setOutboundProperty("dispatcher-thread", Thread.currentThread().getName());
            return super.doSend(event);
        }
    }

    public static class ThreadSensingVMMessageReceiver extends VMMessageReceiver
    {

        public ThreadSensingVMMessageReceiver(Connector connector,
                                              FlowConstruct flowConstruct,
                                              InboundEndpoint endpoint) throws CreateException
        {
            super(connector, flowConstruct, endpoint);
        }

        public MuleMessage onCall(MuleMessage message) throws MuleException
        {
            try
            {
                message.setOutboundProperty("receiver-thread", Thread.currentThread().getName());
                MuleEvent event = routeMessage(message);
                MuleMessage returnedMessage = event == null ? null : event.getMessage();
                if (returnedMessage != null)
                {
                    returnedMessage = returnedMessage.createInboundMessage();
                }
                return returnedMessage;
            }
            catch (Exception e)
            {
                throw new DefaultMuleException(e);
            }
        }

        @Override
        protected void processMessage(Object msg) throws Exception
        {
            MuleMessage message = (MuleMessage) msg;

            // Rewrite the message to treat it as a new message
            MuleMessage newMessage = message.createInboundMessage();
            newMessage.setOutboundProperty("receiver-thread", Thread.currentThread().getName());
            routeMessage(newMessage);
        }

    }

}