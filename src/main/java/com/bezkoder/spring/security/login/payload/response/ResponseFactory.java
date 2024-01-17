package com.bezkoder.spring.security.login.payload.response;

import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

public class ResponseFactory {

	public static ResponseEntity<Object> build(Object responseObject, HttpStatus httpStatus, String message) {
		APIResponse response = new APIResponse();
		response.addData(responseObject);
		response.addHttpStatus(httpStatus);
		response.addMessage(message);
		return new ResponseEntity<>(response, httpStatus);
	}
	
	public static ResponseEntity<Object> build(HttpStatus httpStatus, String message) {
		APIResponse response = new APIResponse();
		response.addHttpStatus(httpStatus);
		response.addMessage(message);
		return new ResponseEntity<>(response, httpStatus);
	}
	
	public static ResponseEntity<Object> build(ResponseCookie cookie, HttpStatus httpStatus, String message) {
		APIResponse response = new APIResponse();
		response.addHttpStatus(httpStatus);
		response.addMessage(message);
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
		        .body(response);	
	}
	
	public static ResponseEntity<Object> build(Object responseObject, ResponseCookie cookie, HttpStatus httpStatus, String message) {
		APIResponse response = new APIResponse();
		response.addData(responseObject);
		response.addHttpStatus(httpStatus);
		response.addMessage(message);
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
		        .body(response);	
	}

}

class APIResponse extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public void addMessage(String message) {
		this.put("message", message);
	}

	public void addHttpStatus(HttpStatus httpStatus) {
		this.put("httpStatus", httpStatus);
	}

	public void addData(Object responseObject) {
		this.put("data", responseObject);
	}

}
