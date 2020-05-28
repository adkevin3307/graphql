package com.entity;

public class Pharmacy {
	private String id;
	private String name;
	private String address;
	private String phone;
	private int adultMasks;
	private int childrenMasks;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getAdultMasks() {
		return adultMasks;
	}

	public void setAdultMasks(int adultMasks) {
		this.adultMasks = adultMasks;
	}

	public int getChildrenMasks() {
		return childrenMasks;
	}

	public void setChildrenMasks(int childrenMasks) {
		this.childrenMasks = childrenMasks;
	}

	@Override
	public String toString() {
		return "Pharmacy [id=" + id + ", name=" + name + ", address=" + address + ", phone=" + phone
				+ ", adultMasks=" + adultMasks + ", childrenMasks=" + childrenMasks + "]";
	}

}
