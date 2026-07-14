package com.advisor.booking.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdvisorService {

    private final AdvisorRepository advisorRepository;

    public AdvisorService(AdvisorRepository advisorRepository) {
        this.advisorRepository = advisorRepository;
    }

    public List<Advisor> getAllActiveAdvisors() {
        return advisorRepository.findAllByOrderByIdAsc();
    }

    public Advisor getAdvisorById(Long id) {
        return advisorRepository.findById(id)
            .orElseThrow(() -> new AdvisorNotFoundException("Advisor not found: " + id));
    }

    public Advisor getAdvisorByEmail(String email) {
        return advisorRepository.findByEmail(email)
            .orElseThrow(() -> new AdvisorNotFoundException("Advisor not found with email: " + email));
    }

    public Advisor createAdvisor(Advisor advisor) {
        if (advisorRepository.findByEmail(advisor.getEmail()).isPresent()) {
            throw new AdvisorAlreadyExistsException("Advisor already exists with email: " + advisor.getEmail());
        }
        return advisorRepository.save(advisor);
    }

    public Advisor updateAdvisor(Long id, Advisor advisorDetails) {
        Advisor advisor = getAdvisorById(id);
        
        advisor.setName(advisorDetails.getName());
        advisor.setEmail(advisorDetails.getEmail());
        
        return advisorRepository.save(advisor);
    }

    public Advisor deactivateAdvisor(Long id) {
        Advisor advisor = getAdvisorById(id);
        advisorRepository.delete(advisor);
        return advisor;
    }

    public static class AdvisorNotFoundException extends RuntimeException {
        public AdvisorNotFoundException(String message) {
            super(message);
        }
    }

    public static class AdvisorAlreadyExistsException extends RuntimeException {
        public AdvisorAlreadyExistsException(String message) {
            super(message);
        }
    }
}
