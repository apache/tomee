package org.superbiz.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class User {

	private Integer id;
	private String name;
	private Date registration;

	public User(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;

		final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.set(2019, Calendar.JANUARY, 1);
		registration = c.getTime();
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

	public Date getRegistration() {
		return registration;
	}

	public void setRegistration(Date registration) {
		this.registration = registration;
	}

}
