package com.smart.entities;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="CONTACT")
public class Contact {
	
	
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private int cId;
	
	@NotBlank(message = "Name is required")
	@Size(min = 3, max = 30, message = "Name is required")
	private String name;
	
	@NotBlank(message = "Nick Name field is required !!")
	@Size(min = 3, max = 30, message = "min 3 and max 20 characters are allowed !!")
	private String secondName;
	
	@NotBlank(message = "fill work!!")
	@Size(min = 3, max = 50, message = "fill work")
	private String work;
	
	@NotBlank(message = "email is required !!")
	@Size(min = 3, max = 35, message = "email is required !!")
	private String email;
	
	
	@NotBlank(message = "enter phone number !!")
	@Size(min = 10, max = 10, message = "enter 10 digit phone number!!")
	private String phone;
	
	
	@Column(length = 250)
	private String image;
	
	
	@Column(length = 5000)
	private String description;
	
	@ManyToOne
	@JsonIgnore
	private User user;
	
	
	public int getcId() {
		return cId;
	}
	public void setcId(int cId) {
		this.cId = cId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getWork() {
		return work;
	}
	public void setWork(String work) {
		this.work = work;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.cId==((Contact)obj).getcId();
	}

}
