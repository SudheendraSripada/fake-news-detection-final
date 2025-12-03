package com.fakenews.detection.controller;

import com.fakenews.detection.service.HuggingFaceMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.gson.JsonObject;

/**
 * MLController provides REST endpoints for HuggingFace model predictions
 * Endpoints expose fake news detection using distilbert-base-uncased model
 */
@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = "*")
public class MLController {
    
    private final HuggingFaceMLService mlService;
    
    @Autowired
    public MLController(HuggingFaceMLService mlService) {
        this.mlService = mlService;
    }
    
    /**
     * Predict fake news from given text using HuggingFace model
     * POST /api/ml/predict
     * 
     * @param request JSON with "text" field
     * @return ML prediction with confidence score
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predictFakeNews(@RequestBody MLPredictionRequest request) {
        try {
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Text field is required and cannot be empty"));
            }
            
            // Get ML analysis from HuggingFace model
            HuggingFaceMLService.MLAnalysisResult result = mlService.analyzeNews(request.getText());
            
            // Build response
            MLPredictionResponse response = new MLPredictionResponse(
                result.isFake ? "FAKE" : "REAL",
                String.format("%.4f", result.confidence),
                result.message,
                request.getText().substring(0, Math.min(100, request.getText().length())) + "..."
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error during prediction: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed analysis with label probabilities
     * POST /api/ml/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeDetailedNews(@RequestBody MLPredictionRequest request) {
        try {
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Text field is required"));
            }
            
            HuggingFaceMLService.MLAnalysisResult result = mlService.analyzeNews(request.getText());
            
            JsonObject detailedAnalysis = new JsonObject();
            detailedAnalysis.addProperty("is_fake", result.isFake);
            detailedAnalysis.addProperty("confidence_score", result.confidence);
            detailedAnalysis.addProperty("classification", result.isFake ? "FAKE" : "REAL");
            detailedAnalysis.addProperty("message", result.message);
            detailedAnalysis.addProperty("model", "distilbert-base-uncased");
            
            return ResponseEntity.ok(detailedAnalysis.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Analysis failed: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/ml/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        JsonObject health = new JsonObject();
        health.addProperty("status", "UP");
        health.addProperty("model", "HuggingFace Transformers");
        health.addProperty("model_name", "distilbert-base-uncased");
        health.addProperty("task", "text-classification");
        return ResponseEntity.ok(health.toString());
    }
    
    // DTO Classes
    public static class MLPredictionRequest {
        private String text;
        
        public MLPredictionRequest() {}
        
        public MLPredictionRequest(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
    }
    
    public static class MLPredictionResponse {
        private String prediction;
        private String confidence;
        private String message;
        private String text_preview;
        
        public MLPredictionResponse(String prediction, String confidence, String message, String text_preview) {
            this.prediction = prediction;
            this.confidence = confidence;
            this.message = message;
            this.text_preview = text_preview;
        }
        
        public String getPrediction() { return prediction; }
        public String getConfidence() { return confidence; }
        public String getMessage() { return message; }
        public String getText_preview() { return text_preview; }
    }
    
    public static class ErrorResponse {
        private String error;
        private long timestamp;
        
        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }
}
