/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.construct;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.tck.core.lifecycle.LifecycleTrackerProcessor.FLOW_CONSRUCT_PROPERTY;
import static org.mule.tck.core.lifecycle.LifecycleTrackerProcessor.LIFECYCLE_TRACKER_PROCESSOR_PROPERTY;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.AbstractIntegrationTestCase;

import org.junit.Test;

public class SubFlowTestCase extends AbstractIntegrationTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/test/construct/sub-flow.xml";
  }

  @Test
  public void testSubFlowViaProcessorRef() throws Exception {
    CoreEvent result = flowRunner("SubFlowViaProcessorRef").withPayload("").run();
    assertThat(result.getMessage().getPayload().getValue(), is("1xyz2"));

    assertThat(result.getVariables().get(LIFECYCLE_TRACKER_PROCESSOR_PROPERTY).getValue(),
               is("[setMuleContext, initialise, start]"));
    assertThat(result.getVariables().get(FLOW_CONSRUCT_PROPERTY).getValue(),
               is(registry.lookupByName("SubFlowViaProcessorRef").get()));
  }

  @Test
  public void testSubFlowViaFlowRef() throws Exception {
    CoreEvent result = flowRunner("SubFlowViaFlowRef").withPayload("").run();
    assertThat(result.getMessage().getPayload().getValue(), is("1xyz2"));

    assertThat(result.getVariables().get(LIFECYCLE_TRACKER_PROCESSOR_PROPERTY).getValue(),
               is("[setMuleContext, initialise, start]"));
    assertThat(result.getVariables().get(FLOW_CONSRUCT_PROPERTY).getValue(),
               is(registry.lookupByName("SubFlowViaFlowRef").get()));
  }

  @Test
  public void testFlowviaFlowRef() throws Exception {
    assertThat(getPayloadAsString(flowRunner("FlowViaFlowRef").withPayload("").run().getMessage()), is("1xyz2"));
  }

  @Test
  public void testServiceviaFlowRef() throws Exception {
    assertThat(getPayloadAsString(flowRunner("ServiceViaFlowRef").withPayload("").run().getMessage()), is("1xyz2"));
  }

  @Test
  public void testFlowWithSubFlowWithComponent() throws Exception {
    assertThat(getPayloadAsString(flowRunner("flowWithsubFlowWithComponent").withPayload("0").run().getMessage()), is("0"));

  }

  @Test
  public void testFlowWithSameSubFlowTwice() throws Exception {
    assertThat(getPayloadAsString(flowRunner("flowWithSameSubFlowTwice").withPayload("0").run().getMessage()), is("0xyzxyz"));
  }

  @Test
  public void testFlowWithSameSubFlowSingletonTwice() throws Exception {
    assertThat(getPayloadAsString(flowRunner("flowWithSameSubFlowSingletonTwice").withPayload("0").run().getMessage()),
               is("0xyzxyz"));
  }

  @Test
  public void testFlowWithSameGlobalChainTwice() throws Exception {
    assertThat(getPayloadAsString(flowRunner("flowWithSameGlobalChainTwice").withPayload("0").run().getMessage()), is("0xyzxyz"));
  }

  @Test
  public void testFlowWithSameGlobalChainSingletonTwice() throws Exception {
    assertThat(getPayloadAsString(flowRunner("flowWithSameGlobalChainSingletonTwice").withPayload("0").run().getMessage()),
               is("0xyzxyz"));
  }

}
