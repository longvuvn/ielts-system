package com.ddhva.ielts.flashcard;

import com.ddhva.ielts.dto.flashcard.req.FlashcardRequest;
import com.ddhva.ielts.dto.flashcard.res.FlashcardResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.enums.FlashcardStatus;
import com.ddhva.ielts.model.Flashcard;
import com.ddhva.ielts.model.Library;
import com.ddhva.ielts.repositories.FlashcardRepository;
import com.ddhva.ielts.repositories.LibraryRepository;
import com.ddhva.ielts.service.impl.FlashcardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FlashcardServiceImpl flashcardService;

    private Library newLibrary(UUID id) {
        Library lib = new Library();
        lib.setId(id);
        lib.setName("Library A");
        return lib;
    }

    private Flashcard newFlashcard(UUID id, Library library) {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(id);
        flashcard.setLibrary(library);
        flashcard.setStatus(FlashcardStatus.ACTIVE);
        return flashcard;
    }

    @Test
    @DisplayName("Should get all flashcards by library id")
    void testGetAllFlashcardsByLibraryId() {
        UUID libraryId = UUID.randomUUID();
        Library library = newLibrary(libraryId);

        Flashcard f1 = newFlashcard(UUID.randomUUID(), library);
        Flashcard f2 = newFlashcard(UUID.randomUUID(), library);

        Page<Flashcard> pageData = new PageImpl<>(List.of(f1, f2), PageRequest.of(0, 10), 2);

        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
        when(flashcardRepository.findByLibrary_Id(eq(libraryId), any(PageRequest.class)))
                .thenReturn(Optional.of(pageData));
        when(modelMapper.map(any(Flashcard.class), eq(FlashcardResponse.class)))
                .thenReturn(new FlashcardResponse());

        Pagination<FlashcardResponse> result = flashcardService.getAllFlashcardsByLibraryId(libraryId.toString(), 0, 10);

        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should search flashcard by title")
    void testSearchFlashcard() {
        Flashcard f = new Flashcard();
        f.setId(UUID.randomUUID());
        Page<Flashcard> pageData = new PageImpl<>(List.of(f), PageRequest.of(0, 10), 1);

        when(flashcardRepository.searchTitle(eq("apple"), any(PageRequest.class)))
                .thenReturn(Optional.of(pageData));
        when(modelMapper.map(any(Flashcard.class), eq(FlashcardResponse.class)))
                .thenReturn(new FlashcardResponse());

        Pagination<FlashcardResponse> result = flashcardService.searchFlashcard("apple", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get flashcard by id")
    void testGetFlashcardById() {
        UUID flashcardId = UUID.randomUUID();
        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);

        FlashcardResponse response = new FlashcardResponse();
        response.setId(flashcardId.toString());

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(modelMapper.map(flashcard, FlashcardResponse.class)).thenReturn(response);

        FlashcardResponse result = flashcardService.getFlashcardById(flashcardId.toString());

        assertNotNull(result);
        assertEquals(flashcardId.toString(), result.getId());
    }

    @Test
    @DisplayName("Should create flashcard successfully")
    void testCreateFlashcard() {
        UUID libraryId = UUID.randomUUID();
        UUID flashcardId = UUID.randomUUID();

        Library library = newLibrary(libraryId);

        FlashcardRequest request = new FlashcardRequest();
        request.setLibraryId(libraryId.toString());

        Flashcard mapped = new Flashcard();
        mapped.setId(flashcardId);

        FlashcardResponse response = new FlashcardResponse();
        response.setId(flashcardId.toString());

        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
        when(modelMapper.map(request, Flashcard.class)).thenReturn(mapped);
        when(flashcardRepository.save(any(Flashcard.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Flashcard.class), eq(FlashcardResponse.class))).thenReturn(response);

        FlashcardResponse result = flashcardService.createFlashcard(request);

        assertNotNull(result);
        assertEquals(flashcardId.toString(), result.getId());

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(captor.capture());
        assertEquals(FlashcardStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(libraryId, captor.getValue().getLibrary().getId());
    }

    @Test
    @DisplayName("Should update flashcard successfully")
    void testUpdateFlashcard() {
        UUID flashcardId = UUID.randomUUID();
        UUID oldLibraryId = UUID.randomUUID();
        UUID newLibraryId = UUID.randomUUID();

        Library oldLibrary = newLibrary(oldLibraryId);
        Library newLibrary = newLibrary(newLibraryId);

        Flashcard flashcard = newFlashcard(flashcardId, oldLibrary);

        FlashcardRequest request = new FlashcardRequest();
        request.setLibraryId(newLibraryId.toString());
        request.setStatus("ACTIVE");

        doAnswer(inv -> {
            FlashcardRequest src = inv.getArgument(0);
            Flashcard dest = inv.getArgument(1);

            return dest;
        }).when(modelMapper).map(any(FlashcardRequest.class), any(Flashcard.class));

        FlashcardResponse response = new FlashcardResponse();
        response.setId(flashcardId.toString());

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(libraryRepository.findById(newLibraryId)).thenReturn(Optional.of(newLibrary));
        when(flashcardRepository.save(any(Flashcard.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Flashcard.class), eq(FlashcardResponse.class))).thenReturn(response);

        FlashcardResponse result = flashcardService.updateFlashcard(flashcardId.toString(), request);

        assertNotNull(result);
        assertEquals(flashcardId.toString(), result.getId());

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(captor.capture());
        assertEquals(newLibraryId, captor.getValue().getLibrary().getId());
        assertEquals(FlashcardStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should delete flashcard successfully")
    void testDeleteFlashcard() {
        UUID flashcardId = UUID.randomUUID();
        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));

        flashcardService.deleteFlashcard(flashcardId.toString());

        verify(flashcardRepository, times(1)).delete(flashcard);
    }

    @Test
    @DisplayName("Should throw when library not found in getAll")
    void testGetAllFlashcardsByLibraryId_libraryNotFound() {
        UUID libraryId = UUID.randomUUID();
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> flashcardService.getAllFlashcardsByLibraryId(libraryId.toString(), 0, 10));
    }
}