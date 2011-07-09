package org.superbiz.cdi.example;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

public class Faculty {
	private List<String> facultyMembers;
	private String facultyName;
	
	@PostConstruct
	public void initialize() {
		setFacultyMembers(new ArrayList<String> ());
		getFacultyMembers().add("Ian Schultz");
		getFacultyMembers().add("Diane Reyes");
		facultyName = "Computer Science";
	}

	public List<String> getFacultyMembers() {
		return facultyMembers;
	}

	public void setFacultyMembers(List<String> facultyMembers) {
		this.facultyMembers = facultyMembers;
	}

	public String getFacultyName() {
		return facultyName;
	}

	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}
	
	
}	
