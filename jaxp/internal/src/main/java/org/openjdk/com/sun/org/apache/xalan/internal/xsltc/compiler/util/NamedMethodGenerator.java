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
 * $Id: NamedMethodGenerator.java,v 1.2.4.1 2005/09/05 11:19:56 pvedula Exp $
 */

package org.openjdk.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import org.openjdk.com.sun.org.apache.bcel.internal.generic.ALOAD;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.ASTORE;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.Instruction;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.InstructionList;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.Type;

/**
 * This class is used for named templates. Named template methods have access
 * to the DOM, the current iterator, the handler and the current node.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class NamedMethodGenerator extends MethodGenerator {
    protected static final int CURRENT_INDEX = 4;

    // The index of the first parameter (after dom/iterator/handler/current)
    private static final int PARAM_START_INDEX = 5;

    public NamedMethodGenerator(int access_flags, Type return_type,
                                Type[] arg_types, String[] arg_names,
                                String method_name, String class_name,
                                InstructionList il, ConstantPoolGen cp) {
        super(access_flags, return_type, arg_types, arg_names, method_name,
                class_name, il, cp);
    }

    public int getLocalIndex(String name) {
        if (name.equals("current")) {
            return CURRENT_INDEX;
        }
        return super.getLocalIndex(name);
    }

    public Instruction loadParameter(int index) {
        return new ALOAD(index + PARAM_START_INDEX);
    }

    public Instruction storeParameter(int index) {
        return new ASTORE(index + PARAM_START_INDEX);
    }
}
