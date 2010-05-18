package org.mule.test.integration.tck;
/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Simple web service to use for test cases.
 */
@WebService
public interface Echo
{
    @WebResult(name = "text")
    public String echo(@WebParam(name = "text") String string);
}
