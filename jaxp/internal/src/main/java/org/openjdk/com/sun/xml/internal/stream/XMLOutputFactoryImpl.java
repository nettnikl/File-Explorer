/*
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.openjdk.com.sun.xml.internal.stream;

import org.openjdk.com.sun.org.apache.xerces.internal.impl.Constants;
import org.openjdk.com.sun.org.apache.xerces.internal.impl.PropertyManager;
import org.openjdk.com.sun.xml.internal.stream.writers.XMLDOMWriterImpl;
import org.openjdk.com.sun.xml.internal.stream.writers.XMLEventWriterImpl;
import org.openjdk.com.sun.xml.internal.stream.writers.XMLStreamWriterImpl;
import org.openjdk.javax.xml.stream.XMLOutputFactory;
import org.openjdk.javax.xml.stream.XMLStreamException;
import org.openjdk.javax.xml.transform.dom.DOMResult;
import org.openjdk.javax.xml.transform.stax.StAXResult;
import org.openjdk.javax.xml.transform.stream.StreamResult;

import java.io.OutputStream;
import java.io.Writer;

/**
 * This class provides the implementation of XMLOutputFactory.
 *
 * @author Neeraj Bajaj,
 * @author k.venugopal@sun.com
 */
public class XMLOutputFactoryImpl extends XMLOutputFactory {

    //List of supported properties and default values.
    private PropertyManager fPropertyManager = new PropertyManager(PropertyManager.CONTEXT_WRITER);

    //cache the instance of XMLStreamWriterImpl
    private XMLStreamWriterImpl fStreamWriter = null;

    /**
     * TODO: at the current time, XMLStreamWriters are not Thread safe.
     */
    boolean fReuseInstance = false;

    /**
     * Creates a new instance of XMLOutputFactory
     */
    public XMLOutputFactoryImpl() {
    }

    public org.openjdk.javax.xml.stream.XMLEventWriter createXMLEventWriter(java.io.OutputStream outputStream) throws org.openjdk.javax.xml.stream.XMLStreamException {
        return createXMLEventWriter(outputStream, null);
    }

    public org.openjdk.javax.xml.stream.XMLEventWriter createXMLEventWriter(java.io.OutputStream outputStream, String encoding) throws org.openjdk.javax.xml.stream.XMLStreamException {
        return new XMLEventWriterImpl(createXMLStreamWriter(outputStream, encoding));
    }

    public org.openjdk.javax.xml.stream.XMLEventWriter createXMLEventWriter(org.openjdk.javax.xml.transform.Result result) throws org.openjdk.javax.xml.stream.XMLStreamException {

        if (result instanceof StAXResult && ((StAXResult) result).getXMLEventWriter() != null)
            return ((StAXResult) result).getXMLEventWriter();

        return new XMLEventWriterImpl(createXMLStreamWriter(result));
    }

    public org.openjdk.javax.xml.stream.XMLEventWriter createXMLEventWriter(java.io.Writer writer) throws org.openjdk.javax.xml.stream.XMLStreamException {
        return new XMLEventWriterImpl(createXMLStreamWriter(writer));
    }

    public org.openjdk.javax.xml.stream.XMLStreamWriter createXMLStreamWriter(org.openjdk.javax.xml.transform.Result result) throws org.openjdk.javax.xml.stream.XMLStreamException {

        if (result instanceof StreamResult) {
            return createXMLStreamWriter((StreamResult) result, null);
        } else if (result instanceof DOMResult) {
            return new XMLDOMWriterImpl((DOMResult) result);
        } else if (result instanceof StAXResult) {
            if (((StAXResult) result).getXMLStreamWriter() != null) {
                return ((StAXResult) result).getXMLStreamWriter();
            } else {
                throw new java.lang.UnsupportedOperationException("Result of type " + result + " is not supported");
            }
        } else {
            if (result.getSystemId() != null) {
                //this is not correct impl of SAXResult. Keep it for now for compatibility
                return createXMLStreamWriter(new StreamResult(result.getSystemId()));
            } else {
                throw new java.lang.UnsupportedOperationException("Result of type " + result + " is not supported. " +
                        "Supported result types are: DOMResult, StAXResult and StreamResult.");
            }
        }

    }

    public org.openjdk.javax.xml.stream.XMLStreamWriter createXMLStreamWriter(java.io.Writer writer) throws org.openjdk.javax.xml.stream.XMLStreamException {
        return createXMLStreamWriter(toStreamResult(null, writer, null), null);
    }

    public org.openjdk.javax.xml.stream.XMLStreamWriter createXMLStreamWriter(java.io.OutputStream outputStream) throws org.openjdk.javax.xml.stream.XMLStreamException {
        return createXMLStreamWriter(outputStream, null);
    }

    public org.openjdk.javax.xml.stream.XMLStreamWriter createXMLStreamWriter(java.io.OutputStream outputStream, String encoding) throws org.openjdk.javax.xml.stream.XMLStreamException {
        return createXMLStreamWriter(toStreamResult(outputStream, null, null), encoding);
    }

    public Object getProperty(String name) throws java.lang.IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Property not supported");
        }
        if (fPropertyManager.containsProperty(name))
            return fPropertyManager.getProperty(name);
        throw new IllegalArgumentException("Property not supported");
    }

    public boolean isPropertySupported(String name) {
        if (name == null) {
            return false;
        } else {
            return fPropertyManager.containsProperty(name);
        }
    }

    public void setProperty(String name, Object value) throws java.lang.IllegalArgumentException {
        if (name == null || value == null || !fPropertyManager.containsProperty(name)) {
            throw new IllegalArgumentException("Property " + name + "is not supported");
        }
        if (name == Constants.REUSE_INSTANCE || name.equals(Constants.REUSE_INSTANCE)) {
            fReuseInstance = ((Boolean) value).booleanValue();
            if (DEBUG) System.out.println("fReuseInstance is set to " + fReuseInstance);

            // TODO: XMLStreamWriters are not Thread safe,
            // don't let application think it is optimizing
            if (fReuseInstance) {
                throw new IllegalArgumentException(
                        "Property "
                                + name
                                + " is not supported: XMLStreamWriters are not Thread safe");
            }
        } else {//for any other property set the flag
            //REVISIT: Even in this case instance can be reused, by passing PropertyManager
            fPropertyChanged = true;
        }
        fPropertyManager.setProperty(name, value);
    }

    /**
     * StreamResult object is re-used and the values are set appropriately.
     */
    StreamResult toStreamResult(OutputStream os, Writer writer, String systemId) {
        StreamResult sr = new StreamResult();
        sr.setOutputStream(os);
        sr.setWriter(writer);
        sr.setSystemId(systemId);
        return sr;
    }

    org.openjdk.javax.xml.stream.XMLStreamWriter createXMLStreamWriter(org.openjdk.javax.xml.transform.stream.StreamResult sr, String encoding) throws org.openjdk.javax.xml.stream.XMLStreamException {
        //if factory is configured to reuse the instance & this instance can be reused
        //& the setProperty() hasn't been called
        try {
            if (fReuseInstance && fStreamWriter != null && fStreamWriter.canReuse() && !fPropertyChanged) {
                fStreamWriter.reset();
                fStreamWriter.setOutput(sr, encoding);
                if (DEBUG) System.out.println("reusing instance, object id : " + fStreamWriter);
                return fStreamWriter;
            }
            return fStreamWriter = new XMLStreamWriterImpl(sr, encoding, new PropertyManager(fPropertyManager));
        } catch (java.io.IOException io) {
            throw new XMLStreamException(io);
        }
    }//createXMLStreamWriter(StreamResult,String)

    private static final boolean DEBUG = false;

    /**
     * This flag indicates the change of property. If true,
     * <code>PropertyManager</code> should be passed when creating
     * <code>XMLStreamWriterImpl</code>
     */
    private boolean fPropertyChanged;
}//XMLOutputFactory
