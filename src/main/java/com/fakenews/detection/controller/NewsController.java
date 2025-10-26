package com.fakenews.detection.controller;

import com.fakenews.detection.model.News;
import com.fakenews.detection.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*") // Allow requests from any origin (for frontend)
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // GET all news - http://localhost:8080/api/news
    @GetMapping
    public ResponseEntity<List<News>> getAllNews() {
        List<News> newsList = newsService.getAllNews();
        return ResponseEntity.ok(newsList);
    }

    // GET news by ID - http://localhost:8080/api/news/1
    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return newsService.getNewsById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create new news - http://localhost:8080/api/news
    @PostMapping
    public ResponseEntity<News> createNews(@RequestBody News news) {
        News savedNews = newsService.saveNews(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNews);
    }

    // DELETE news by ID - http://localhost:8080/api/news/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}
