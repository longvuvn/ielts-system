package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.flashcard.req.FlashcardRequest;
import com.ddhva.ielts.dto.flashcard.res.FlashcardResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.enums.FlashcardStatus;
import com.ddhva.ielts.model.Flashcard;
import com.ddhva.ielts.model.Library;
import com.ddhva.ielts.repositories.FlashcardRepository;
import com.ddhva.ielts.repositories.LibraryRepository;
import com.ddhva.ielts.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final ModelMapper modelMapper;
    private final LibraryRepository libraryRepository;


    @Override
    public Pagination<FlashcardResponse> getAllFlashcardsByLibraryId(String libraryId, int page, int size) {
        UUID libraryUUID = UUID.fromString(libraryId);
        Pageable pageable = PageRequest.of(page, size);
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        Page<Flashcard> flashcards = flashcardRepository.findByLibrary_Id(library.getId(), pageable)
                .orElseThrow(() -> new IllegalArgumentException("No flashcard found"));

        return getPagination(flashcards, page, size);
    }

    @Override
    public Pagination<FlashcardResponse> searchFlashcard(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Flashcard> flashcards = flashcardRepository.searchTitle(title,pageable)
                .orElseThrow(() -> new IllegalArgumentException("No flashcard found"));
        return getPagination(flashcards, page, size);
    }

    private Pagination<FlashcardResponse> getPagination(Page<Flashcard> flashcards, int page, int size){
        List<FlashcardResponse> responses = flashcards.stream()
                .map(flashcard -> modelMapper.map(flashcard, FlashcardResponse.class))
                .collect(Collectors.toList());
        Pagination<FlashcardResponse> pagination = new Pagination<>();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setTotalElements(flashcards.getTotalElements());
        pagination.setTotalPages(flashcards.getTotalPages());
        pagination.setContent(responses);
        log.info("Successfully fetched flashcards");
        return pagination;
    }

    @Override
    public FlashcardResponse getFlashcardById(String flashcardId) {
        UUID flashcardUUID = UUID.fromString(flashcardId);
        Flashcard flashcard = flashcardRepository.findById(flashcardUUID)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
        log.info("Successfully fetched flashcard {}", flashcard.getId());
        return modelMapper.map(flashcard, FlashcardResponse.class);

    }

    @Override
    public FlashcardResponse createFlashcard(FlashcardRequest request) {
        UUID libraryUUID = UUID.fromString(request.getLibraryId());
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        Flashcard flashcard = modelMapper.map(request, Flashcard.class);
        flashcard.setLibrary(library);
        flashcard.setStatus(FlashcardStatus.ACTIVE);
        flashcard = flashcardRepository.save(flashcard);
        log.info("Successfully created flashcard {}", flashcard.getId());
        return modelMapper.map(flashcard, FlashcardResponse.class);
    }

    @Override
    public FlashcardResponse updateFlashcard(String flashcardId, FlashcardRequest request) {
        UUID flashcardUUID = UUID.fromString(flashcardId);
        UUID libraryUUID = UUID.fromString(request.getLibraryId());
        Flashcard flashcard = flashcardRepository.findById(flashcardUUID)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
        Library library = libraryRepository.findById(libraryUUID)
                .orElseThrow(() -> new IllegalArgumentException("Library not found"));
        flashcard.setLibrary(library);
        flashcard.setStatus(FlashcardStatus.valueOf(request.getStatus()));
        modelMapper.map(request, flashcard);
        flashcard = flashcardRepository.save(flashcard);
        log.info("Successfully updated flashcard {}", flashcard.getId());
        return modelMapper.map(flashcard, FlashcardResponse.class);
    }

    @Override
    public void deleteFlashcard(String flashcardId) {
        UUID flashcardUUID = UUID.fromString(flashcardId);
        Flashcard flashcard = flashcardRepository.findById(flashcardUUID)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
        flashcardRepository.delete(flashcard);
        log.info("Successfully deleted flashcard {}", flashcard.getId());
    }
}