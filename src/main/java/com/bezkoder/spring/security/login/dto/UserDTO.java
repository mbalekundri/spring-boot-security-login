package com.bezkoder.spring.security.login.dto;

import java.util.HashSet;
import java.util.Set;

import com.bezkoder.spring.security.login.models.Role;

import lombok.Data;

@Data
public class UserDTO {

	private Long id;

	private String username;

	private String email;

	private Set<Role> roles = new HashSet<>();

}
