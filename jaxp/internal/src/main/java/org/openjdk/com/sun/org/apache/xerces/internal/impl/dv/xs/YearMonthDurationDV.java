/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openjdk.com.sun.org.apache.xerces.internal.impl.dv.xs;

import org.openjdk.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import org.openjdk.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import org.openjdk.javax.xml.datatype.DatatypeConstants;
import org.openjdk.javax.xml.datatype.Duration;

import java.math.BigInteger;

/**
 * Used to validate the <yearMonthDuration> type
 *
 * @author Ankit Pasricha, IBM
 * @version $Id: YearMonthDurationDV.java,v 1.6 2010-11-01 04:39:47 joehw Exp $
 * @xerces.internal
 */
class YearMonthDurationDV extends DurationDV {

    public Object getActualValue(String content, ValidationContext context)
            throws InvalidDatatypeValueException {
        try {
            return parse(content, YEARMONTHDURATION_TYPE);
        } catch (Exception ex) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "yearMonthDuration"});
        }
    }

    protected Duration getDuration(DateTimeData date) {
        int sign = 1;
        if (date.year < 0 || date.month < 0) {
            sign = -1;
        }
        return datatypeFactory.newDuration(sign == 1,
                date.year != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf(sign * date.year) : null,
                date.month != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf(sign * date.month) : null,
                null,
                null,
                null,
                null);
    }
}
