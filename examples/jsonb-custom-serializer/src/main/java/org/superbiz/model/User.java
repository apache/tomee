package org.superbiz.model;

import java.util.Date;

import javax.json.bind.annotation.JsonbTypeDeserializer;

import org.superbiz.UserDeserializer;

@JsonbTypeDeserializer(UserDeserializer.class)
public class User {

	private Integer id;
	private String name;
	private Date registration = new Date();
	private Address address;

	public User(Integer id, String name, Address address) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
	}

	public User() {
		super();
	}

	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", registration=" + registration + ", address=" + address + "]";
	}

}
