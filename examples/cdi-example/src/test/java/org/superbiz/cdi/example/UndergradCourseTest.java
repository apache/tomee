package org.superbiz.cdi.example;

import javax.ejb.EJB;
import javax.ejb.embeddable.EJBContainer;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class UndergradCourseTest extends TestCase{
	@EJB
	Course csCourse;
	
	@Before
	public void setUp() throws Exception {
		Object object = EJBContainer.createEJBContainer().getContext().lookup("java:global/cdi-example/CsUndergradCourse!org.superbiz.cdi.example.Course");
		assertTrue(object instanceof Course);
		csCourse = (Course) object;
	}
	
	@Test
	public void testPostConstruct() {
		assertTrue(csCourse != null);
		
		
		assertEquals(csCourse.getFaculty().getFacultyName(), "Computer Science");
	}
}
