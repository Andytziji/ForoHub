package com.alura.forohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class ForoHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForoHubApplication.class, args);
    }

    @RestController
    @RequestMapping("/api/topics")
    public static class TopicController {
        @Autowired
        private TopicService topicService;

        @PostMapping
        public Topic createTopic(@Valid @RequestBody Topic topic) {
            return topicService.createTopic(topic);
        }

        @GetMapping
        public List<Topic> getAllTopics() {
            return topicService.getAllTopics();
        }

        @GetMapping("/{id}")
        public Topic getTopicById(@PathVariable Long id) {
            return topicService.getTopicById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + id));
        }

        @PutMapping("/{id}")
        public Topic updateTopic(@PathVariable Long id, @Valid @RequestBody Topic topicDetails) {
            return topicService.updateTopic(id, topicDetails);
        }

        @DeleteMapping("/{id}")
        public void deleteTopic(@PathVariable Long id) {
            topicService.deleteTopic(id);
        }
    }

    @Service
    public static class TopicService {
        @Autowired
        private TopicRepository topicRepository;

        public Topic createTopic(Topic topic) {
            return topicRepository.save(topic);
        }

        public List<Topic> getAllTopics() {
            return topicRepository.findAll();
        }

        public Optional<Topic> getTopicById(Long id) {
            return topicRepository.findById(id);
        }

        public Topic updateTopic(Long id, Topic topicDetails) {
            Topic topic = topicRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + id));

            topic.setTitle(topicDetails.getTitle());
            topic.setDescription(topicDetails.getDescription());

            return topicRepository.save(topic);
        }

        public void deleteTopic(Long id) {
            Topic topic = topicRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + id));

            topicRepository.delete(topic);
        }
    }

    @Entity
    public static class Topic {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        private String title;

        @NotBlank
        private String description;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public interface TopicRepository extends JpaRepository<Topic, Long> {}

    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @EnableWebSecurity
    public static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/api/topics/**").authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
            return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
            manager.createUser(User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build());
            return manager;
        }
    }
}
