package com.dev.QuizApp.controller;

import com.dev.QuizApp.dto.QuestionDTO;
import com.dev.QuizApp.entity.Options;
import com.dev.QuizApp.entity.Question;
import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.repository.QuestionRepo;
import com.dev.QuizApp.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * All endpoints in this controller are restricted to ADMIN role.
 * The URL-level guard in SecurityConfig is the primary gate;
 * @PreAuthorize is a defence-in-depth annotation.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private UserProfileService userProfileService;

    // ─────────────────────────────────────────────────────────────
    //  Question Management
    // ─────────────────────────────────────────────────────────────

    /** Add a new question */
    @PostMapping("/questions")
    public ResponseEntity<QuestionDTO> addQuestion(@RequestBody QuestionRequest req) {
        Question question = new Question();
        question.setQuestionText(req.questionText());
        question.setCorrectAnswer(req.correctAnswer());
        question.setCategory(req.category().toUpperCase());
        question.setExplanation(req.explanation());

        // Build Options and link them to the question
        if (req.options() != null) {
            List<Options> opts = req.options().stream().map(text -> {
                Options o = new Options();
                o.setOptionText(text);
                o.setQuestion(question);
                return o;
            }).toList();
            question.setOptions(opts);
        }

        return ResponseEntity.ok(toQuestionDTO(questionRepo.save(question)));
    }

    /** Update an existing question */
    @PutMapping("/questions/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id,
                                            @RequestBody QuestionRequest req) {
        return questionRepo.findById(id).map(q -> {
            q.setQuestionText(req.questionText());
            q.setCorrectAnswer(req.correctAnswer());
            q.setCategory(req.category().toUpperCase());
            q.setExplanation(req.explanation());
            if (q.getOptions() == null) {
                q.setOptions(new ArrayList<>());
            }
            q.getOptions().clear();
            if (req.options() != null) {
                req.options().forEach(text -> {
                    Options option = new Options();
                    option.setOptionText(text);
                    option.setQuestion(q);
                    q.getOptions().add(option);
                });
            }
            return ResponseEntity.ok(toQuestionDTO(questionRepo.save(q)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Delete a question by ID */
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable Long id) {
        if (!questionRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        questionRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Question deleted successfully"));
    }

    /** List all questions (admin view, no shuffling) */
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        return ResponseEntity.ok(questionRepo.findAll().stream()
                .map(this::toQuestionDTO)
                .toList());
    }

    /** List questions by category */
    @GetMapping("/questions/category/{category}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByCategory(@PathVariable String category) {
        // Re-use the native query from QuestionRepo but return all, not random 10
        return ResponseEntity.ok(questionRepo.findAll().stream()
                .filter(q -> q.getCategory().equalsIgnoreCase(category))
                .map(this::toQuestionDTO)
                .toList());
    }

    private QuestionDTO toQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getQuestionId());
        dto.setQuestionId(question.getQuestionId());
        dto.setQuestionText(question.getQuestionText());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setCategory(question.getCategory());
        dto.setExplanation(question.getExplanation());
        dto.setOptions(question.getOptions() == null ? List.of() : question.getOptions().stream()
                .map(Options::getOptionText)
                .toList());
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    //  User Management
    // ─────────────────────────────────────────────────────────────

    /** List all users */
    @GetMapping("/users")
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userProfileService.getAllUsers());
    }

    /** Promote a user to ADMIN */
    @PutMapping("/users/{username}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userProfileService.promoteToAdmin(username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Demote an admin back to USER */
    @PutMapping("/users/{username}/demote")
    public ResponseEntity<?> demoteUser(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userProfileService.demoteToUser(username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Delete a user account */
    @DeleteMapping("/users/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        try {
            userProfileService.deleteUser(username);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Inner request record (replaces a separate DTO class)
    // ─────────────────────────────────────────────────────────────
    public record QuestionRequest(
            String questionText,
            String correctAnswer,
            String category,
            String explanation,
            List<String> options
    ) {}
}
