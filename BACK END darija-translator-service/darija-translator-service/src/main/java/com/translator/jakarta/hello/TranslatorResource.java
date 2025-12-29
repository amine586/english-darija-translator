package com.translator.jakarta.hello;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/translate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslatorResource {
    
    private static final String GEMINI_API_KEY = "AIzaSyA78ElZFyGyG33adIdxCF4YXkcGMehtZHw";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    @POST
    public Response translate(TranslationRequest request) {
    	//add
    	System.out.println("=== TRANSLATE REQUEST START ===");
        System.out.println("Request object: " + request);
        System.out.println("Request text: " + (request != null ? request.getText() : "null request"));
        //wjb
        try {
        	
        	if (request == null || request.getText() == null || request.getText().isEmpty()) {
                System.out.println("ERROR: Empty or null text received");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Text is required"))
                        .build();
            }

            String translatedText = callGeminiAPI(request.getText());
            //added
            System.out.println("Translated text: " + translatedText);
            
            TranslationResponse response = new TranslationResponse(translatedText);
            System.out.println("=== TRANSLATE REQUEST END ===");
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Translation failed: " + e.getMessage()))
                    .build();
        }
    }
    
    private String callGeminiAPI(String englishText) throws Exception {
        System.out.println("Calling Gemini API with text: " + englishText);
        
        String prompt = String.format(
            "Translate the following English text to Moroccan Arabic Darija. Provide only the translation without any explanations:\n\n\"%s\"",
            englishText
        );
        
        String requestBody = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        );
        
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", GEMINI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status: " + response.statusCode() + 
                                       " - Response: " + response.body());
        }
        
        // Extract translation from response - FIXED PARSING
        String responseBody = response.body();
        
        // Find the "text": " pattern and extract content until the next unescaped quote
        int textStart = responseBody.indexOf("\"text\": \"");
        if (textStart == -1) {
            throw new RuntimeException("Failed to find 'text' field in response: " + responseBody);
        }
        
        // Move past "text": "
        int contentStart = textStart + 9;
        
        // Find the closing quote (not escaped)
        int contentEnd = contentStart;
        while (contentEnd < responseBody.length()) {
            if (responseBody.charAt(contentEnd) == '"' && 
                (contentEnd == 0 || responseBody.charAt(contentEnd - 1) != '\\')) {
                break;
            }
            contentEnd++;
        }
        
        if (contentEnd >= responseBody.length()) {
            throw new RuntimeException("Failed to find end of text field in response");
        }
        
        String result = responseBody.substring(contentStart, contentEnd)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .trim();
        
        System.out.println("Extracted translation: '" + result + "'");
        return result;
    }
}