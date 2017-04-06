package org.apache.openejb.mockito;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

@RunWith(ApplicationComposer.class)
@Classes(cdi = true, innerClassesAsBean = true)
public class MockitoAndAppComposerGenericAndQualifiedTest {
		
	public static class MyBean {
		
		@Inject
		@MyQualifier
		private MyGenericInterface<String> myStringInterface;
		
		@Inject
		@MyQualifier
		private MyGenericInterface<Long> myLongInterface;
		
		@Inject
		@Default
		private MyGenericInterface<Boolean> myBooleanInterface;
		
		@Inject
		@Named("named")
		private MyGenericInterface<String> myNamedInterface;
		
		public String createMessage() {
			return myStringInterface.getMessage() + " " + myNamedInterface.getMessage() + " / " + myLongInterface.getMessage() + " / " + myBooleanInterface.getMessage();
		}
	}
	
	public interface MyGenericInterface<T> {
		
		T getMessage();
	}
		
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MyQualifier {}
	
	@Inject
	private MyBean myBean;
	
	@Mock
	@MyQualifier
	private MyGenericInterface<String> myStringInterface;
	
	@Mock
	@MyQualifier
	private MyGenericInterface<Long> myLongInterface;
	
	@Mock
	private MyGenericInterface<Boolean> myBooleanInterface;
	
	@Mock(name = "named")
	private MyGenericInterface<String> myNamedInterface;
	
	@Test
	public void test() {
		Mockito.doReturn("Hello").when(myStringInterface).getMessage();
		Mockito.doReturn("world!").when(myNamedInterface).getMessage();
		Mockito.doReturn(42L).when(myLongInterface).getMessage();
		Mockito.doReturn(true).when(myBooleanInterface).getMessage();
		Assert.assertEquals("Hello world! / 42 / true", myBean.createMessage());
		Mockito.verify(myStringInterface).getMessage();
		Mockito.verify(myNamedInterface).getMessage();
		Mockito.verify(myLongInterface).getMessage();
		Mockito.verify(myBooleanInterface).getMessage();
	}
}
