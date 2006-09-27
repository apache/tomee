package org.apache.openejb.test.entity.cmp;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.apache.openejb.test.object.ObjectGraph;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpRmiIiopTests extends CmpTestClient{

    protected RmiIiopCmpHome   ejbHome;
    protected RmiIiopCmpObject ejbObject;

    public CmpRmiIiopTests(){
        super("RMI_IIOP.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/RMI-over-IIOP/EJBHome");
        ejbHome = (RmiIiopCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, RmiIiopCmpHome.class);
        ejbObject = ejbHome.create("RMI-IIOP TestBean");
    }

/*-------------------------------------------------*/
/*  String                                         */
/*-------------------------------------------------*/

    public void test01_returnStringObject() {
        try{
            String expected = new String("1");
            String actual = ejbObject.returnStringObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_returnStringObjectArray() {
        try{
            String[] expected = {"1","2","3"};
            String[] actual = ejbObject.returnStringObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Character                                      */
/*-------------------------------------------------*/

    public void test03_returnCharacterObject() {
        try{
            Character expected = new Character('1');
            Character actual = ejbObject.returnCharacterObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_returnCharacterPrimitive() {
        try{
            char expected = '1';
            char actual = ejbObject.returnCharacterPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_returnCharacterObjectArray() {
        try{
            Character[] expected = {new Character('1'),new Character('2'),new Character('3')};
            Character[] actual = ejbObject.returnCharacterObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test06_returnCharacterPrimitiveArray() {
        try{
            char[] expected = {'1','2','3'};
            char[] actual = ejbObject.returnCharacterPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Boolean                                        */
/*-------------------------------------------------*/

    public void test07_returnBooleanObject() {
        try{
            Boolean expected = new Boolean(true);
            Boolean actual = ejbObject.returnBooleanObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test08_returnBooleanPrimitive() {
        try{
            boolean expected = true;
            boolean actual = ejbObject.returnBooleanPrimitive(expected);
            assertEquals(""+expected, ""+actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test09_returnBooleanObjectArray() {
        try{
            Boolean[] expected = {new Boolean(true),new Boolean(false),new Boolean(true)};
            Boolean[] actual = ejbObject.returnBooleanObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test10_returnBooleanPrimitiveArray() {
        try{
            boolean[] expected = {false,true,true};
            boolean[] actual = ejbObject.returnBooleanPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Byte                                      */
/*-------------------------------------------------*/

    public void test11_returnByteObject() {
        try{
            Byte expected = new Byte("1");
            Byte actual = ejbObject.returnByteObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test12_returnBytePrimitive() {
        try{
            byte expected = (byte)1;
            byte actual = ejbObject.returnBytePrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test13_returnByteObjectArray() {
        try{
            Byte[] expected = {new Byte("1"),new Byte("2"),new Byte("3")};
            Byte[] actual = ejbObject.returnByteObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test14_returnBytePrimitiveArray() {
        try{
            byte[] expected = {(byte)1,(byte)2,(byte)3};
            byte[] actual = ejbObject.returnBytePrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Short                                      */
/*-------------------------------------------------*/

    public void test15_returnShortObject() {
        try{
            Short expected = new Short("1");
            Short actual = ejbObject.returnShortObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test16_returnShortPrimitive() {
        try{
            short expected = (short)1;
            short actual = ejbObject.returnShortPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test17_returnShortObjectArray() {
        try{
            Short[] expected = {new Short("1"),new Short("2"),new Short("3")};
            Short[] actual = ejbObject.returnShortObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test18_returnShortPrimitiveArray() {
        try{
            short[] expected = {(short)1,(short)2,(short)3};
            short[] actual = ejbObject.returnShortPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Integer                                        */
/*-------------------------------------------------*/

    public void test19_returnIntegerObject() {
        try{
            Integer expected = new Integer(1);
            Integer actual = ejbObject.returnIntegerObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test20_returnIntegerPrimitive() {
        try{
            int expected = 1;
            int actual = ejbObject.returnIntegerPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test21_returnIntegerObjectArray() {
        try{
            Integer[] expected = {new Integer(1),new Integer(2),new Integer(3)};
            Integer[] actual = ejbObject.returnIntegerObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test22_returnIntegerPrimitiveArray() {
        try{
            int[] expected = {1,2,3};
            int[] actual = ejbObject.returnIntegerPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Long                                           */
/*-------------------------------------------------*/

    public void test23_returnLongObject() {
        try{
            Long expected = new Long("1");
            Long actual = ejbObject.returnLongObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test24_returnLongPrimitive() {
        try{
            long expected = 1;
            long actual = ejbObject.returnLongPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test25_returnLongObjectArray() {
        try{
            Long[] expected = {new Long("1"),new Long("2"),new Long("3")};
            Long[] actual = ejbObject.returnLongObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test26_returnLongPrimitiveArray() {
        try{
            long[] expected = {1,2,3};
            long[] actual = ejbObject.returnLongPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Float                                      */
/*-------------------------------------------------*/

    public void test27_returnFloatObject() {
        try{
            Float expected = new Float("1.3");
            Float actual = ejbObject.returnFloatObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test28_returnFloatPrimitive() {
        try{
            float expected = 1.2F;
            float actual = ejbObject.returnFloatPrimitive(expected);
            assertEquals(expected, actual, 0.00D);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test29_returnFloatObjectArray() {
        try{
            Float[] expected = {new Float("1.1"),new Float("2.2"),new Float("3.3")};
            Float[] actual = ejbObject.returnFloatObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test30_returnFloatPrimitiveArray() {
        try{
            float[] expected = {1.2F,2.3F,3.4F};
            float[] actual = ejbObject.returnFloatPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i , expected[i], actual[i], 0.0D);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Double                                      */
/*-------------------------------------------------*/

    public void test31_returnDoubleObject() {
        try{
            Double expected = new Double("1.1");
            Double actual = ejbObject.returnDoubleObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test32_returnDoublePrimitive() {
        try{
            double expected = 1.2;
            double actual = ejbObject.returnDoublePrimitive(expected);
            assertEquals(expected, actual, 0.0D);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test33_returnDoubleObjectArray() {
        try{
            Double[] expected = {new Double("1.3"),new Double("2.4"),new Double("3.5")};
            Double[] actual = ejbObject.returnDoubleObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i]);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test34_returnDoublePrimitiveArray() {
        try{
            double[] expected = {1.4,2.5,3.6};
            double[] actual = ejbObject.returnDoublePrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i=0; i < actual.length; i++){
                assertEquals("Array values are not equal at index "+i ,expected[i], actual[i],0.0D);
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  EJBHome                                        */
/*-------------------------------------------------*/

    public void test35_returnEJBHome() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome expected = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", expected);

            EncCmpHome actual = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow(ejbObject.returnEJBHome(expected), EncCmpHome.class);
            assertNotNull("The EJBHome returned is null", actual);

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test36_returnEJBHome2() {
        try{
            EncCmpHome actual = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow(ejbObject.returnEJBHome(), EncCmpHome.class);
            assertNotNull("The EJBHome returned is null", actual);

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test37_returnNestedEJBHome() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome expected = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", expected);

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            EncCmpHome actual = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( graph.getObject(), EncCmpHome.class);
            assertNotNull("The EJBHome returned is null", actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test38_returnNestedEJBHome2() {
        try{
            ObjectGraph graph = ejbObject.returnNestedEJBHome();
            assertNotNull("The ObjectGraph is null", graph);

            EncCmpHome actual = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( graph.getObject(), EncCmpHome.class);
            assertNotNull("The EJBHome returned is null", actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test39_returnEJBHomeArray() {
        try{

            EncCmpHome expected[] = new EncCmpHome[3];
            for (int i=0; i < expected.length; i++){
                Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
                expected[i] = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
                assertNotNull("The EJBHome returned from JNDI is null", expected[i]);
            }

            EJBHome[] actual = ejbObject.returnEJBHomeArray(expected);
            assertNotNull("The EJBHome array returned is null", actual);
            assertEquals(expected.length, actual.length);

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  EJBObject                                      */
/*-------------------------------------------------*/

    public void test40_returnEJBObject() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncCmpObject expected = home.create("test_40 CmpBean");
            assertNotNull("The EJBObject created is null", expected);

            EncCmpObject actual = (EncCmpObject)javax.rmi.PortableRemoteObject.narrow( ejbObject.returnEJBObject(expected), EncCmpObject.class);
            assertNotNull("The EJBObject returned is null", actual);

            assertTrue("The EJBObejcts are not identical", expected.isIdentical(actual));
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test41_returnEJBObject2() {
        try{
            EncCmpObject actual = (EncCmpObject)javax.rmi.PortableRemoteObject.narrow(ejbObject.returnEJBObject(), EncCmpObject.class);
            assertNotNull("The EJBObject returned is null", actual);

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test42_returnNestedEJBObject() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncCmpObject expected = home.create("test_42 CmpBean");
            assertNotNull("The EJBObject created is null", expected);

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            EncCmpObject actual = (EncCmpObject)javax.rmi.PortableRemoteObject.narrow(graph.getObject(), EncCmpObject.class);
            assertNotNull("The EJBObject returned is null", actual);

            assertTrue("The EJBObejcts are not identical", expected.isIdentical(actual));
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test43_returnNestedEJBObject2() {
        try{
            ObjectGraph graph = ejbObject.returnNestedEJBObject();
            assertNotNull("The ObjectGraph is null", graph);

            EncCmpObject actual = (EncCmpObject)javax.rmi.PortableRemoteObject.narrow(graph.getObject(), EncCmpObject.class);
            assertNotNull("The EJBHome returned is null", actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test44_returnEJBObjectArray() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncCmpObject expected[] = new EncCmpObject[3];
            for (int i=0; i < expected.length; i++){
                expected[i] = home.create("test_44 CmpBean");
                assertNotNull("The EJBObject created is null", expected[i]);
            }

            EJBObject[] actual = ejbObject.returnEJBObjectArray(expected);
            assertNotNull("The EJBObject array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i=0; i < actual.length; i++){
                assertTrue("The EJBObejcts are not identical", expected[i].isIdentical(actual[i]));
            }

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

/*-------------------------------------------------*/
/*  EJBMetaData                                    */
/*-------------------------------------------------*/

    public void test45_returnEJBMetaData() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EJBMetaData expected = home.getEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", expected);

            EJBMetaData actual = ejbObject.returnEJBMetaData(expected);
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(expected.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(expected.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test46_returnEJBMetaData() {
        try{
            EJBMetaData actual = ejbObject.returnEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(actual.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(actual.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test47_returnNestedEJBMetaData() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EJBMetaData expected = home.getEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", expected);

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            EJBMetaData actual = (EJBMetaData)graph.getObject();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(expected.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(expected.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test48_returnNestedEJBMetaData2() {
        try{
            ObjectGraph graph = ejbObject.returnNestedEJBMetaData();
            assertNotNull("The ObjectGraph is null", graph);

            EJBMetaData actual = (EJBMetaData)graph.getObject();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertNotNull("The home interface class of the EJBMetaData is null", actual.getHomeInterfaceClass());
            assertNotNull("The remote interface class of the EJBMetaData is null", actual.getRemoteInterfaceClass());
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test49_returnEJBMetaDataArray() {
        try{

            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EJBMetaData expected[] = new EJBMetaData[3];
            for (int i=0; i < expected.length; i++){
                expected[i] = home.getEJBMetaData();
                assertNotNull("The EJBMetaData returned is null", expected[i]);
            }

            EJBMetaData[] actual = (EJBMetaData[])ejbObject.returnEJBMetaDataArray(expected);
            assertNotNull("The EJBMetaData array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i=0; i < actual.length; i++){
                assertNotNull("The EJBMetaData returned is null", actual[i]);
                assertEquals(expected[i].getHomeInterfaceClass(), actual[i].getHomeInterfaceClass());
                assertEquals(expected[i].getRemoteInterfaceClass(), actual[i].getRemoteInterfaceClass());
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Handle                                         */
/*-------------------------------------------------*/

    public void test50_returnHandle() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncCmpObject object = home.create("test_50 CmpBean");
            assertNotNull("The EJBObject created is null", object);

            Handle expected = object.getHandle();
            assertNotNull("The EJBObject Handle returned is null", expected);
            assertNotNull("The EJBObject in the Handle is null", expected.getEJBObject());

            Handle actual = ejbObject.returnHandle(expected);
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

            EJBObject exp = expected.getEJBObject();
            EJBObject act = actual.getEJBObject();

            assertTrue("The EJBObjects in the Handles are not identical", exp.isIdentical(act));
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test51_returnHandle() {
        try{
            Handle actual = ejbObject.returnHandle();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test52_returnNestedHandle() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncCmpObject object = home.create("test_52 CmpBean");
            assertNotNull("The EJBObject created is null", object);

            Handle expected = object.getHandle();
            assertNotNull("The EJBObject Handle returned is null", expected);
            assertNotNull("The EJBObject in the Handle is null", expected.getEJBObject());

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            Handle actual = (Handle)graph.getObject();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

            EJBObject exp = expected.getEJBObject();
            EJBObject act = actual.getEJBObject();

            assertTrue("The EJBObjects in the Handles are not identical", exp.isIdentical(act));

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test53_returnNestedHandle2() {
        try{
            ObjectGraph graph = ejbObject.returnNestedHandle();
            assertNotNull("The ObjectGraph is null", graph);

            Handle actual = (Handle)graph.getObject();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test54_returnHandleArray() {
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/EncBean");
            EncCmpHome home = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncCmpObject object = home.create("test_54 CmpBean");
            assertNotNull("The EJBObject created is null", object);

            Handle expected[] = new Handle[3];
            for (int i=0; i < expected.length; i++){
                expected[i] = object.getHandle();
                assertNotNull("The EJBObject Handle returned is null", expected[i]);
            }

            Handle[] actual = (Handle[])ejbObject.returnHandleArray(expected);
            assertNotNull("The Handle array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i=0; i < expected.length; i++){
                assertNotNull("The EJBObject Handle returned is null", actual[i]);
                assertNotNull("The EJBObject in the Handle is null", actual[i].getEJBObject());
                assertTrue("The EJBObjects in the Handles are not equal", expected[i].getEJBObject().isIdentical(actual[i].getEJBObject()));
            }

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Foo                                      */
/*-------------------------------------------------*/

    public void test55_returnObjectGraph() {
    }
    public void test56_returnObjectGraphArray() {
    }
}

