package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.learner.req.LearnerUpdateRequest;
import com.ddhva.ielts.dto.learner.res.LearnerHistoryResponse;
import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import com.ddhva.ielts.model.Learner;
import com.ddhva.ielts.model.Submissions;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.repositories.SubmissionRepository;
import com.ddhva.ielts.service.LearnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearnerServiceImpl implements LearnerService {

    private final LearnerRepository learnerRepository;
    private final SubmissionRepository submissionRepository;

    private static final String UPLOAD_DIR = "uploads/avatars/";

    // ==================== GET ALL ====================
    @Override
    public List<LearnerResponse> getAll() {
        return learnerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<LearnerResponse> getAllDTO() {
        return getAll();
    }

    // ==================== GET BY ID ====================
    @Override
    public LearnerResponse getById(String id) {
        UUID uuid = UUID.fromString(id);
        Learner learner = learnerRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));
        return mapToResponse(learner);
    }

    // ==================== UPDATE PROFILE ====================
    @Override
    @Transactional
    public LearnerResponse updateProfile(String id, LearnerUpdateRequest request) {
        UUID uuid = UUID.fromString(id);
        Learner learner = learnerRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            learner.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            learner.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (learnerRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            learner.setUsername(request.getUsername());
        }

        learner.setUpdatedAt(Instant.now());
        learnerRepository.save(learner);

        return mapToResponse(learner);
    }

    // ==================== UPDATE AVATAR ====================
    @Override
    @Transactional
    public LearnerResponse updateAvatar(String id, MultipartFile file) {
        UUID uuid = UUID.fromString(id);
        Learner learner = learnerRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = uuid + "_" + System.currentTimeMillis()
                    + getExtension(file.getOriginalFilename());

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String avatarUrl = "/uploads/avatars/" + fileName;

            learner.setAvatarUrl(avatarUrl);
            learner.setUpdatedAt(Instant.now());
            learnerRepository.save(learner);

            return mapToResponse(learner);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading avatar");
        }
    }

    // ==================== GET HISTORY ====================
    @Override
    public LearnerHistoryResponse getHistory(String id, int page, int size) {
        UUID uuid = UUID.fromString(id);
        Learner learner = learnerRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));

        Page<Submissions> submissionsPage = submissionRepository
                .findByLearnerIdPageable(uuid, PageRequest.of(page, size));

        List<Submissions> submissions = submissionsPage.getContent();

        // ✅ FIX average
        List<BigDecimal> scores = submissions.stream()
                .map(Submissions::getScore)
                .filter(s -> s != null)
                .toList();

        BigDecimal avg = scores.isEmpty()
                ? BigDecimal.ZERO
                : scores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);

        String averageScore = avg.toString();

        List<LearnerHistoryResponse.SubmissionHistoryDto> historyDtos = submissions.stream()
                .map(s -> LearnerHistoryResponse.SubmissionHistoryDto.builder()
                        .submissionId(s.getId().toString())
                        .examTitle(s.getExam() != null ? s.getExam().getTitle() : "")
                        .score(s.getScore() != null ? s.getScore().toString() : "0")
                        .totalQuestions(s.getTotalQuestions() != null ? s.getTotalQuestions().toString() : "0")
                        .correctQuestions(s.getCorrectQuestions() != null ? s.getCorrectQuestions().toString() : "0")
                        .failedQuestions(s.getFailedQuestions() != null ? s.getFailedQuestions().toString() : "0")
                        .status(s.getStatus() != null ? s.getStatus().name() : "")
                        .completedAt(s.getCompletedAt() != null ? s.getCompletedAt().toString() : "")
                        .startTime(s.getStartTime() != null ? s.getStartTime().toString() : "")
                        .endTime(s.getEndTime() != null ? s.getEndTime().toString() : "")
                        .build())
                .collect(Collectors.toList());

        return LearnerHistoryResponse.builder()
                .learnerId(id)
                .fullName(learner.getFullName())
                .email(learner.getEmail())
                .totalExamsTaken((int) submissionsPage.getTotalElements())
                .averageScore(averageScore)
                .submissions(historyDtos)
                .build();
    }

    // ==================== PRIVATE ====================
    private LearnerResponse mapToResponse(Learner learner) {
        return LearnerResponse.builder()
                .id(learner.getId().toString())
                .fullName(learner.getFullName())
                .email(learner.getEmail())
                .username(learner.getUsername())
                .phoneNumber(learner.getPhoneNumber())
                .avatarUrl(learner.getAvatarUrl())
                .role(learner.getRole() != null ? learner.getRole().getName() : "LEARNER")
                .status(learner.getStatus() != null ? learner.getStatus().name() : "")
                .createdAt(learner.getCreatedAt() != null ? learner.getCreatedAt().toString() : "")
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}