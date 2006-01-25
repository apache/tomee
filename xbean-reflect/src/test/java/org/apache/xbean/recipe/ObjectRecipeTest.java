package org.apache.xbean.recipe;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.*;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.propertyeditor.PropertyEditors;

public class ObjectRecipeTest extends TestCase {
    ObjectRecipe objectRecipe;

    public void testObjectRecipe() throws Exception {
        PropertyEditors.class.getName();
        Object value = PropertyEditors.getValue(Integer.TYPE, "21");
        System.out.println("value = " + value);

        ObjectRecipe objectRecipe = new ObjectRecipe(Person.class);
        objectRecipe.setProperty("name","Joe");
        objectRecipe.setProperty("age", "21");

        Person person = (Person) objectRecipe.create(Person.class.getClassLoader());
        System.out.println("person = " + person);
    }


}