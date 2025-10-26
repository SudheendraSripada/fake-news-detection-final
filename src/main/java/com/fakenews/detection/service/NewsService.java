package com.fakenews.detection.service;

import com.fakenews.detection.model.News;
import com.fakenews.detection.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // Get all news
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    // Get news by ID
    public Optional<News> getNewsById(Long id) {
        return newsRepository.findById(id);
    }

    // Save news with fake detection
    public News saveNews(News news) {
        // Apply fake news detection logic
        news.setFake(detectFakeNews(news.getContent()));
        return newsRepository.save(news);
    }

    // Delete news
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    // Simple keyword-based fake news detection algorithm
    private boolean detectFakeNews(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        // List of suspicious keywords that often appear in fake news
        String[] fakeKeywords = {
                "shocking", "clickbait", "scam", "miracle",
                "unbelievable", "breaking", "urgent", "exclusive",
                "you won't believe", "doctors hate", "secret",
                "conspiracy", "hoax", "fake", "lie"
        };

        String lowerContent = content.toLowerCase();

        // Check if content contains any fake keywords
        for (String keyword : fakeKeywords) {
            if (lowerContent.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
