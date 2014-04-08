/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ProxyInterfaceResolverTest extends TestCase {
    public void test() throws Exception {
        Class smoothie;
        List<Class> ingredients;

        // No remotes
        smoothie = implement(Mango.class, Lime.class, Lemon.class);
        ingredients = resolve(smoothie, Mango.class, Lime.class, Lemon.class);
        assertEquals(3, ingredients.size());
        assertTrue(ingredients.contains(Mango.class));
        assertTrue(ingredients.contains(Lime.class));
        assertTrue(ingredients.contains(Lemon.class));

        // All remotes
        smoothie = implement(Cherry.class, Honey.class, Grape.class);
        ingredients = resolve(smoothie, Cherry.class, Honey.class, Grape.class);
        assertEquals(3, ingredients.size());
        assertTrue(ingredients.contains(Cherry.class));
        assertTrue(ingredients.contains(Grape.class));
        assertTrue(ingredients.contains(Honey.class));

        // mixed remote and non-remote, no conflicts
        smoothie = implement(Banana.class, Honey.class, Creme.class);
        ingredients = resolve(smoothie, Banana.class, Honey.class, Creme.class);
        assertEquals(3, ingredients.size());
        assertTrue(ingredients.contains(Banana.class));
        assertTrue(ingredients.contains(Honey.class));
        assertTrue(ingredients.contains(Creme.class));

        // mixed remote and non-remote, conflicts (cherry, grape)
        smoothie = implement(Mango.class, Banana.class, Creme.class, Honey.class, Cherry.class, Grape.class);
        ingredients = resolve(smoothie, Mango.class, Banana.class, Creme.class, Honey.class, Cherry.class, Grape.class);
        assertEquals(4, ingredients.size());
        assertTrue(ingredients.contains(Mango.class));
        assertTrue(ingredients.contains(Banana.class));
        assertTrue(ingredients.contains(Creme.class));
        assertTrue(ingredients.contains(Honey.class));

        // mixed remote and non-remote, conflicts (mango, banana)
        smoothie = implement(Cherry.class, Mango.class, Banana.class, Creme.class, Honey.class, Grape.class);
        ingredients = resolve(smoothie, Cherry.class, Mango.class, Banana.class, Creme.class, Honey.class, Grape.class);
        assertEquals(4, ingredients.size());
        assertTrue(ingredients.contains(Cherry.class));
        assertTrue(ingredients.contains(Grape.class));
        assertTrue(ingredients.contains(Creme.class));
        assertTrue(ingredients.contains(Honey.class));
    }

    public List<Class> resolve(Class impl, Class mainInterface, Class... interfaces) {
        return ProxyInterfaceResolver.getInterfaces(impl, mainInterface, Arrays.asList(interfaces));
    }


    public Class implement(Class<?>... interfaces) {
        return java.lang.reflect.Proxy.getProxyClass(this.getClass().getClassLoader(), interfaces);
    }

    public interface Mango {
        void exist() throws RoundException, GreenException;

        void mango();
    }

    public interface Lime {
        void exist() throws RoundException, GreenException;

        void lime();
    }

    public interface Lemon {
        void exist() throws RoundException, YellowException;

        void lemon();
    }

    public interface Banana {
        void exist() throws LongException, YellowException;

        void banana();
    }

    public interface Creme {
        void thiken();
    }

    public interface Cherry extends java.rmi.Remote {
        void exist() throws RoundException, RemoteException;

        void cherry() throws RemoteException;
    }

    public interface Grape extends java.rmi.Remote {
        void exist() throws RoundException, RemoteException;

        void grape() throws RemoteException;
    }

    public interface Honey extends java.rmi.Remote {
        void sweeten() throws RemoteException;
    }

    //--------------//
    public static class RoundException extends Exception {
    }

    public static class GreenException extends Exception {
    }

    public static class YellowException extends Exception {
    }

    public static class LongException extends Exception {
    }


}

