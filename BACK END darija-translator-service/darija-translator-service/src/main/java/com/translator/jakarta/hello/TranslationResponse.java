package com.translator.jakarta.hello;

public class TranslationResponse {
	
	private String translation;
    
    public TranslationResponse(String translation) {
        this.translation = translation;
    }
    
    // Getters et setters
    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

}
