package com.advisor.booking.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/advisors")
public class AdvisorController {

    private final AdvisorService advisorService;

    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @GetMapping
    public ResponseEntity<?> getAllAdvisors() {
        log.info("Getting all active advisors");
        List<Advisor> advisors = advisorService.getAllActiveAdvisors();
        return ResponseEntity.ok(Map.of("advisors", advisors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdvisor(@PathVariable Long id) {
        try {
            Advisor advisor = advisorService.getAdvisorById(id);
            return ResponseEntity.ok(advisor);
        } catch (AdvisorService.AdvisorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Advisor not found",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAdvisor(@RequestBody Advisor advisor) {
        try {
            Advisor createdAdvisor = advisorService.createAdvisor(advisor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAdvisor);
        } catch (AdvisorService.AdvisorAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Advisor already exists",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdvisor(@PathVariable Long id, @RequestBody Advisor advisor) {
        try {
            Advisor updatedAdvisor = advisorService.updateAdvisor(id, advisor);
            return ResponseEntity.ok(updatedAdvisor);
        } catch (AdvisorService.AdvisorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Advisor not found",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateAdvisor(@PathVariable Long id) {
        try {
            advisorService.deactivateAdvisor(id);
            return ResponseEntity.ok(Map.of(
                "message", "Advisor deactivated successfully"
            ));
        } catch (AdvisorService.AdvisorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Advisor not found",
                "message", e.getMessage()
            ));
        }
    }
}
