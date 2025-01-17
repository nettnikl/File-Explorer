/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: FuncContains.java,v 1.2.4.1 2005/09/14 19:53:44 jeffsuttor Exp $
 */
package org.openjdk.com.sun.org.apache.xpath.internal.functions;

import org.openjdk.com.sun.org.apache.xpath.internal.XPathContext;
import org.openjdk.com.sun.org.apache.xpath.internal.objects.XBoolean;
import org.openjdk.com.sun.org.apache.xpath.internal.objects.XObject;

/**
 * Execute the Contains() function.
 *
 * @xsl.usage advanced
 */
public class FuncContains extends Function2Args {
    static final long serialVersionUID = 5084753781887919723L;

    /**
     * Execute the function.  The function must return
     * a valid object.
     *
     * @param xctxt The current execution context.
     * @return A valid XObject.
     * @throws org.openjdk.javax.xml.transform.TransformerException
     */
    public XObject execute(XPathContext xctxt) throws org.openjdk.javax.xml.transform.TransformerException {

        String s1 = m_arg0.execute(xctxt).str();
        String s2 = m_arg1.execute(xctxt).str();

        // Add this check for JDK consistency for empty strings.
        if (s1.length() == 0 && s2.length() == 0)
            return XBoolean.S_TRUE;

        int index = s1.indexOf(s2);

        return (index > -1) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
