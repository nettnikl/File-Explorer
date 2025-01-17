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
 * $Id: AttributeSetMethodGenerator.java,v 1.5 2005/09/28 13:48:24 pvedula Exp $
 */

package org.openjdk.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import org.openjdk.com.sun.org.apache.bcel.internal.Constants;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.ALOAD;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.ASTORE;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.Instruction;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.InstructionList;
import org.openjdk.com.sun.org.apache.bcel.internal.generic.Type;

/**
 * @author Santiago Pericas-Geertsen
 */
public final class AttributeSetMethodGenerator extends MethodGenerator {

    protected static final int CURRENT_INDEX = 4;
    private static final int PARAM_START_INDEX = 5;

    private static final String[] argNames = new String[4];
    private static final Type[] argTypes =
            new Type[4];

    static {
        argTypes[0] = Util.getJCRefType(DOM_INTF_SIG);
        argTypes[1] = Util.getJCRefType(NODE_ITERATOR_SIG);
        argTypes[2] = Util.getJCRefType(TRANSLET_OUTPUT_SIG);
        argTypes[3] = Type.INT;
        argNames[0] = DOCUMENT_PNAME;
        argNames[1] = ITERATOR_PNAME;
        argNames[2] = TRANSLET_OUTPUT_PNAME;
        argNames[3] = NODE_PNAME;
    }

    public AttributeSetMethodGenerator(String methodName, ClassGenerator classGen) {
        super(Constants.ACC_PRIVATE,
                Type.VOID,
                argTypes, argNames, methodName,
                classGen.getClassName(),
                new InstructionList(),
                classGen.getConstantPool());
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
