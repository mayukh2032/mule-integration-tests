/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.transformers;

import static org.apache.commons.lang3.StringUtils.reverse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.util.IOUtils.closeQuietly;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.transformer.AbstractTransformer;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.test.AbstractIntegrationTestCase;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;

public class ImplicitTransformationTestCase extends AbstractIntegrationTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/test/transformers/implicit-transformation-config.xml";
  }

  @Test
  public void testImplicitInputStreamToStringConversion() throws Exception {
    InputStream inputStream = new StringInputStream("TEST");
    Message response = flowRunner("StringEchoService").withPayload(inputStream).run().getMessage();
    assertThat(response.getPayload().getValue(), is("TSET"));
  }

  @Test
  public void testImplicitByteArrayToStringConversion() throws Exception {
    Message response = flowRunner("StringEchoService").withPayload("TEST".getBytes()).run().getMessage();
    assertThat(response.getPayload().getValue(), is("TSET"));
  }

  @Test
  public void testImplicitInputStreamToByteArrayConversion() throws Exception {
    InputStream inputStream = new StringInputStream("TEST");
    Message response = flowRunner("ByteArrayEchoService").withPayload(inputStream).run().getMessage();
    assertThat(response.getPayload().getValue(), is("TSET"));
  }

  @Test
  public void testImplicitStringToByteArrayConversion() throws Exception {
    Message response = flowRunner("ByteArrayEchoService").withPayload("TEST").run().getMessage();
    assertThat(response.getPayload().getValue(), is("TSET"));
  }

  @Test
  public void testImplicitStringToInputStreamConversion() throws Exception {
    Message response = flowRunner("InputStreamEchoService").withPayload("TEST").run().getMessage();
    assertThat(response.getPayload().getValue(), is("TSET"));
  }

  @Test
  public void testImplicitByteArrayToInputStreamConversion() throws Exception {
    Message response = flowRunner("InputStreamEchoService").withPayload("TEST".getBytes()).run().getMessage();
    assertThat(response.getPayload().getValue(), is("TSET"));
  }

  public static class TestStringTransformer extends AbstractTransformer {

    public TestStringTransformer() {
      super();
      registerSourceType(DataType.STRING);
      setReturnDataType(DataType.STRING);
    }

    @Override
    protected Object doTransform(Object src, Charset enc) throws TransformerException {
      return reverse((String) src);
    }
  }

  public static class TestByteArrayTransformer extends AbstractTransformer {

    public TestByteArrayTransformer() {
      super();
      registerSourceType(DataType.BYTE_ARRAY);
      setReturnDataType(DataType.STRING);
    }

    @Override
    protected Object doTransform(Object src, Charset enc) throws TransformerException {
      return reverse(new String((byte[]) src));
    }
  }

  public static class TestInputStreamTransformer extends AbstractTransformer {

    public TestInputStreamTransformer() {
      super();
      registerSourceType(DataType.INPUT_STREAM);
      setReturnDataType(DataType.STRING);
    }

    @Override
    protected Object doTransform(Object src, Charset enc) throws TransformerException {

      InputStream input = (InputStream) src;
      String stringValue;
      try {
        stringValue = IOUtils.toString(input);
      } finally {
        closeQuietly(input);
      }
      return reverse(stringValue);
    }
  }
}
