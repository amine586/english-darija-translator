package com.translator.jakarta.hello;

public class ErrorResponse {

	private String error;
	    
    public ErrorResponse(String error) {
        this.error = error;
    }
    
    // Getters et setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
