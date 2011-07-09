package org.superbiz.cdi.example;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class CsUndergradCourse implements Course {
	private String courseName;
	private int capacity;
	
	@Inject
	private Faculty faculty;
	
	public CsUndergradCourse() {
		this.courseName = "CDI 101 - Introduction to CDI";
		this.capacity = 100;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public Faculty getFaculty() {
		return faculty;
	}

	public void setFaculty(Faculty faculty) {
		this.faculty = faculty;
	}
	
	
}
