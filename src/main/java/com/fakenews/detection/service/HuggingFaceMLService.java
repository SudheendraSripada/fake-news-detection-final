package com.fakenews.detection.service;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.huggingface.translator.TextClassificationTranslator;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.training.util.ProgressBar;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * HuggingFaceMLService integrates pretrained transformers for fake news detection
 * Uses distilbert-base-uncased model from HuggingFace for text classification
 */
@Service
public class HuggingFaceMLService {
    
    private Predictor<String, Classifications> predictor;
    private static final String MODEL_NAME = "distilbert-base-uncased";
    
    public HuggingFaceMLService() {
        try {
            initializeModel();
        } catch (Exception e) {
            System.err.println("Error initializing HuggingFace model: " + e.getMessage());
        }
    }
    
    /**
     * Initialize HuggingFace transformer model for fake news classification
     */
    private void initializeModel() throws Exception {
        // Criteria for model loading with text classification
        Criteria<String, Classifications> criteria = Criteria.builder()
                .setTypes(String.class, Classifications.class)
                .optModelName(MODEL_NAME)
                .optEngine("PyTorch")
                .optProgress(new ProgressBar())
                .build();
        
        // Load model and create predictor
        var model = ModelZoo.loadModel(criteria);
        this.predictor = model.newPredictor();
    }
    
    /**
     * Predict fake news probability using HuggingFace transformer
     * @param content news article content
     * @return confidence score for fake news classification
     */
    public double predictFakeNews(String content) {
        try {
            if (content == null || content.isEmpty() || predictor == null) {
                return 0.0; // Default to real news if no content or model not ready
            }
            
            // Limit content length to 512 tokens for efficiency
            String truncatedContent = truncateContent(content, 512);
            
            // Get classification from model
            Classifications result = predictor.predict(truncatedContent);
            
            // Extract fake news probability
            double fakeScore = 0.0;
            for (Classifications.Classification classification : result.items()) {
                if (classification.getClassName().equalsIgnoreCase("fake") || 
                    classification.getClassName().equalsIgnoreCase("negative")) {
                    fakeScore = classification.getProbability();
                    break;
                }
            }
            
            return fakeScore;
        } catch (Exception e) {
            System.err.println("Error predicting fake news: " + e.getMessage());
            return 0.5; // Return neutral score on error
        }
    }
    
    /**
     * Determine if news is fake based on confidence threshold
     * @param content news article content
     * @return true if content is detected as fake news
     */
    public boolean isFakeNews(String content) {
        double score = predictFakeNews(content);
        return score > 0.5; // Classification threshold
    }
    
    /**
     * Get detailed ML analysis of news content
     * @param content news article content
     * @return MLAnalysisResult with confidence scores and classification
     */
    public MLAnalysisResult analyzeNews(String content) {
        double fakeScore = predictFakeNews(content);
        boolean isFake = isFakeNews(content);
        
        return new MLAnalysisResult(
            isFake,
            fakeScore,
            String.format("Fake news confidence: %.2f%%", fakeScore * 100)
        );
    }
    
    /**
     * Truncate content to specified number of characters
     */
    private String truncateContent(String content, int maxChars) {
        return content.length() > maxChars ? content.substring(0, maxChars) : content;
    }
    
    /**
     * Inner class for ML analysis results
     */
    public static class MLAnalysisResult {
        public boolean isFake;
        public double confidence;
        public String message;
        
        public MLAnalysisResult(boolean isFake, double confidence, String message) {
            this.isFake = isFake;
            this.confidence = confidence;
            this.message = message;
        }
    }
}
