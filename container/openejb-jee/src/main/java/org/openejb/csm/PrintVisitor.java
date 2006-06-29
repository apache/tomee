/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.csm;

import org.apache.xmlbeans.SchemaParticle;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class PrintVisitor implements MappedItemVisitor {

    private List seen = new ArrayList();

    public void visit(MappedItem item) {
        if (seen(item)) {
            return;
        }

        if (item instanceof MappedField)
        {
            MappedField mappedField = (MappedField) item;

            System.out.println();
            String owner = mappedField.getField().getDeclaringClass().getSimpleName();
            System.out.println("// (" + owner + ") " + item.getClassType().getSimpleName() + "  " + item.getJavaName() + " " + item.getXmlName());
            System.out.println();
        }
        else
        {
            System.out.println();
            System.out.println("// (null)" + item.getClassType().getSimpleName() + "  " + item.getJavaName() + " " + item.getXmlName());
            System.out.println();
        }

        System.out.println("item.getJavaName() = " + item.getJavaName());
        System.out.println("item.getClassType() = " + item.getClassType());
        System.out.println("item.getGenericType() = " + item.getGenericType());

        System.out.println("item.getXmlName() = " + item.getXmlName());

        if (item.getParticle() == null)
        {
            System.out.println("item.getParticle() = " + null);
        }
        else
        {
            printParticle("item.getParticle()", item.getParticle());
        }

        System.out.println("item.getType().getName().getLocalPart() = " + item.getType().getName().getLocalPart());

        if (item.getType().getContentModel() == null)
        {
            System.out.println("item.getType().getContentModel() = " + item.getType().getContentModel());
        }
        else
        {
            printParticle("item.getType().getContentModel()", item.getType().getContentModel());
            if (item.getType().getContentModel().getParticleType() == SchemaParticle.CHOICE) {

            }
            else if (item.getType().getContentModel().getParticleChildren() == null)
            {
                System.out.println("item.getType().getContentModel().getParticleChildren().length = " + 0);
            }
            else
            {
                System.out.println("item.getType().getContentModel().getParticleChildren().length = " + item.getType().getContentModel().getParticleChildren().length);
                for (int i = 0; i < item.getType().getContentModel().getParticleChildren().length; i++)
                {
                    SchemaParticle particle = item.getType().getContentModel().getParticleChildren()[i];
                    printParticle("item.getType().getContentModel().getParticleChildren()[" + i + "]", particle);
                }
            }
        }
    }

    private boolean seen(MappedItem item) {
        try {
            return seen.contains(item);
        } finally {
            seen.add(item);
        }
    }

    private void printParticle(String prefix, SchemaParticle particle) {
        if (particle.getName() != null)
        {
            System.out.println(prefix + ".getName().getLocalPart() = " + particle.getName().getLocalPart());
        }
        if (particle.getType() != null && particle.getType().getName() != null)
        {
            System.out.println(prefix + ".getType().getName().getLocalPart() = " + particle.getName().getLocalPart());
        }

        System.out.print(prefix + ".getParticleType() = " + particle.getParticleType());
        switch (particle.getParticleType())
        {
            case SchemaParticle.ALL:
                System.out.println(" (ALL)");
                break;
            case SchemaParticle.CHOICE:
                System.out.println(" (CHOICE)");
                break;
            case SchemaParticle.ELEMENT:
                System.out.println(" (ELEMENT)");
                break;
            case SchemaParticle.SEQUENCE:
                System.out.println(" (SEQUENCE)");
                break;
            case SchemaParticle.WILDCARD:
                System.out.println(" (WILDCARD)");
                break;
        }

        System.out.println(prefix + ".getIntMinOccurs() = " + particle.getIntMinOccurs());
        System.out.println(prefix + ".getIntMaxOccurs() = " + particle.getIntMaxOccurs());

        if (particle.getParticleType() == SchemaParticle.CHOICE)
        {
            SchemaParticle[] children = particle.getParticleChildren();
            System.out.println(prefix + ".choice.length = " + children.length);
            for (int i = 0; i < children.length; i++)
            {
                SchemaParticle child = children[i];
                printParticle(prefix + ".choice[" + i + "]", child);
            }
        }
    }
}
