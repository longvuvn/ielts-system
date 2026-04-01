package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.library.req.LibraryRequest;
import com.ddhva.ielts.dto.library.res.LibraryResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.enums.LibraryStatus;
import com.ddhva.ielts.model.Learner;
import com.ddhva.ielts.model.Library;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.repositories.LibraryRepository;
import com.ddhva.ielts.service.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepository;
    private final ModelMapper modelMapper;
    private final LearnerRepository learnerRepository;


    @Override
    public Pagination<LibraryResponse> getAllLibrariesByLearnerId(String learnerId, int page, int size) {
        UUID learnerUUID = UUID.fromString(learnerId);
        Pageable pageable = PageRequest.of(page, size);
        Learner learner = learnerRepository.findById(learnerUUID)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));
        Page<Library> libraries = libraryRepository.findByLearner_Id(learner.getId(), pageable)
                .orElseThrow(() -> new IllegalArgumentException("No library found"));
        return getPagination(libraries, page, size);
    }

    @Override
    public Pagination<LibraryResponse> searchLibrary(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Library> libraries = libraryRepository.searchLibrary(title, pageable)
                .orElseThrow(() -> new IllegalArgumentException("No library found"));
        return getPagination(libraries, page, size);
    }

    private Pagination<LibraryResponse> getPagination(Page<Library> libraries, int page, int size){
        List<LibraryResponse> responses = libraries.stream()
                .map(library -> {
                    LibraryResponse res = modelMapper.map(library, LibraryResponse.class);
                    if(library.getLearner() != null){
                        res.setLearnerId(library.getLearner().getId().toString());
                    }
                    return res;
                }).collect(Collectors.toList());
        log.info("Successfully fetched {} libraries", libraries.getTotalElements());
        Pagination<LibraryResponse> pagination = new Pagination<>();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setTotalElements(libraries.getTotalElements());
        pagination.setTotalPages(libraries.getTotalPages());
        pagination.setContent(responses);
        return pagination;
    }
    @Override
    public LibraryResponse getLibraryById(String libraryId) {
        UUID libraryUUID = UUID.fromString(libraryId);
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        LibraryResponse response = modelMapper.map(library, LibraryResponse.class);
        if(library.getLearner() != null){
            response.setLearnerId(library.getLearner().getId().toString());
        }
        log.info("Successfully fetched library {}", library.getId());
        return response;
    }

    @Override
    @Transactional
    public LibraryResponse createLibrary(LibraryRequest libraryRequest) {
        UUID learnerId = UUID.fromString(libraryRequest.getLearnerId());
        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));

        Library library = modelMapper.map(libraryRequest, Library.class);
        library.setLearner(learner);
        library = libraryRepository.save(library);
        library.setStatus(LibraryStatus.ACTIVE);
        LibraryResponse response = modelMapper.map(library, LibraryResponse.class);
        if(library.getLearner() != null){
            response.setLearnerId(library.getLearner().getId().toString());
        }
        log.info("Successfully created library {}", library.getId());
        return response;
    }

    @Override
    @Transactional
    public LibraryResponse updateLibrary(String libraryId, LibraryRequest libraryRequest) {
        UUID libraryUUID = UUID.fromString(libraryId);
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        modelMapper.map(libraryRequest, library);
        library = libraryRepository.save(library);
        LibraryResponse response = modelMapper.map(library, LibraryResponse.class);
        if(library.getLearner() != null){
            response.setLearnerId(library.getLearner().getId().toString());
        }
        log.info("Successfully updated library {}", library.getId());
        return response;
    }

    @Override
    @Transactional
    public void softDeleteLibrary(String libraryId) {
        UUID libraryUUID = UUID.fromString(libraryId);
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        library.setStatus(LibraryStatus.INACTIVE);
        library.setDeletedAt(Instant.now());
        libraryRepository.save(library);
        log.info("Successfully soft deleted library {}", library.getId());
    }

    @Override
    @Transactional
    public void deleteLibrary(String libraryId) {
        UUID libraryUUID = UUID.fromString(libraryId);
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        if (library.getFlashcards() != null && !library.getFlashcards().isEmpty()) {
            throw new DataIntegrityViolationException("Không thể xóa thư viện khi Flashcard tồn tại!");
        }
        libraryRepository.delete(library);
        log.info("Successfully deleted library {}", library.getId());
    }
}