/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: FilteredStepIterator.java,v 1.2.4.1 2005/09/06 06:20:13 pvedula Exp $
 */

package org.openjdk.com.sun.org.apache.xalan.internal.xsltc.dom;

import org.openjdk.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

/**
 * Extends a StepIterator by adding the ability to filter nodes. It
 * uses filters similar to those of a FilterIterator.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
public final class FilteredStepIterator extends StepIterator {

    private Filter _filter;

    public FilteredStepIterator(DTMAxisIterator source,
                                DTMAxisIterator iterator,
                                Filter filter) {
        super(source, iterator);
        _filter = filter;
    }

    public int next() {
        int node;
        while ((node = super.next()) != END) {
            if (_filter.test(node)) {
                return returnNode(node);
            }
        }
        return node;
    }

}
