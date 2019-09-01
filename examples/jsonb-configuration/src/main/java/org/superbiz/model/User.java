package org.superbiz.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;

public class User {

	private Integer id;
	private String name;
	private LocalDate registration;

	public User(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
		registration = LocalDate.of(2019, Month.JANUARY, 1);
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getRegistration() {
		return registration;
	}

	public void setRegistration(LocalDate registration) {
		this.registration = registration;
	}
}
