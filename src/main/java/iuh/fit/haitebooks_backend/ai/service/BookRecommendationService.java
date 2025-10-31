package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookRecommendationService {

    private final BookRepository bookRepository;
    private final BookEmbeddingRepository embeddingRepo;
    private final AIService aiService;

    public BookRecommendationService(BookRepository bookRepository,
                                     BookEmbeddingRepository embeddingRepo,
                                     AIService aiService) {
        this.bookRepository = bookRepository;
        this.embeddingRepo = embeddingRepo;
        this.aiService = aiService;
    }

    public List<Book> recommendSimilarBooks(Long bookId) {
        Optional<Book> targetOpt = bookRepository.findById(bookId);
        if (targetOpt.isEmpty()) return List.of();

        Book target = targetOpt.get();

        List<Double> targetVector = embeddingRepo.findByBookId(bookId)
                .map(e -> parseEmbedding(e.getEmbeddingJson()))
                .orElseGet(() -> {
                    List<Double> newEmbedding = aiService.generateEmbedding(
                            target.getTitle() + " " + target.getDescription());
                    saveEmbedding(target, newEmbedding);
                    return newEmbedding;
                });

        List<Book> allBooks = bookRepository.findAll();
        Map<Book, Double> similarityMap = new HashMap<>();

        for (Book book : allBooks) {
            if (book.getId().equals(bookId)) continue;

            List<Double> bookVector = embeddingRepo.findByBookId(book.getId())
                    .map(e -> parseEmbedding(e.getEmbeddingJson()))
                    .orElseGet(() -> {
                        List<Double> newEmbedding = aiService.generateEmbedding(
                                book.getTitle() + " " + book.getDescription());
                        saveEmbedding(book, newEmbedding);
                        return newEmbedding;
                    });

            double score = cosineSimilarity(targetVector, bookVector);
            similarityMap.put(book, score);
        }

        return similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Double> parseEmbedding(String json) {
        return Arrays.stream(json.replace("[", "")
                        .replace("]", "")
                        .split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    private void saveEmbedding(Book book, List<Double> embedding) {
        BookEmbedding e = new BookEmbedding();
        e.setBook(book);
        e.setEmbeddingJson(embedding.toString());
        embeddingRepo.save(e);
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += v1.get(i) * v1.get(i);
            normB += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }
}