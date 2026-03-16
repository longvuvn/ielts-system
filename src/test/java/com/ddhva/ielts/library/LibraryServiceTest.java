package com.ddhva.ielts.library;

import com.ddhva.ielts.dto.library.req.LibraryRequest;
import com.ddhva.ielts.dto.library.res.LibraryResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.enums.LibraryStatus;
import com.ddhva.ielts.enums.UserStatus;
import com.ddhva.ielts.model.Learner;
import com.ddhva.ielts.model.Library;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.repositories.LibraryRepository;
import com.ddhva.ielts.service.impl.LibraryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

        @Mock
        private LibraryRepository libraryRepository;

        @Mock
        private LearnerRepository learnerRepository;

        @Mock
        private ModelMapper modelMapper;

        @InjectMocks
        private LibraryServiceImpl libraryService;

        private Learner newLearner(UUID id, String fullName) {
                Learner learner = new Learner();
                // fields inherited from User
                learner.setId(id);
                learner.setFullName(fullName);
                learner.setEmail("example@gmail.com");
                learner.setUsername(fullName.toLowerCase().replace(" ", ""));
                learner.setPassword("password");
                learner.setStatus(UserStatus.ACTIVE);
                return learner;
        }

        private Library newLibrary(UUID id, String name, Learner learner, LibraryStatus status) {
                Library library = new Library();
                library.setId(id);
                library.setName(name);
                library.setLearner(learner);
                library.setStatus(status);
                return library;
        }

        @Test
        @DisplayName("Should return libraries for a specific learner")
        void testGetAllLibrariesByLearnerId() {
                UUID learnerId = UUID.randomUUID();
                String learnerIdString = learnerId.toString();
                int page = 0;
                int size = 10;

                Learner learner = newLearner(learnerId, "John Doe");

                Library library1 = newLibrary(UUID.randomUUID(), "English Book 1", learner, LibraryStatus.ACTIVE);
                Library library2 = newLibrary(UUID.randomUUID(), "English Book 2", learner, LibraryStatus.ACTIVE);

                Page<Library> libraryPage = new PageImpl<>(List.of(library1, library2), PageRequest.of(page, size), 2);

                LibraryResponse response1 = new LibraryResponse();
                response1.setId(library1.getId().toString());
                response1.setName(library1.getName());
                response1.setLearnerId(learnerIdString);

                LibraryResponse response2 = new LibraryResponse();
                response2.setId(library2.getId().toString());
                response2.setName(library2.getName());
                response2.setLearnerId(learnerIdString);

                when(learnerRepository.findById(learnerId)).thenReturn(Optional.of(learner));
                when(libraryRepository.findByLearner_Id(eq(learnerId), any(PageRequest.class)))
                                .thenReturn(Optional.of(libraryPage));
                when(modelMapper.map(library1, LibraryResponse.class)).thenReturn(response1);
                when(modelMapper.map(library2, LibraryResponse.class)).thenReturn(response2);

                Pagination<LibraryResponse> result = libraryService.getAllLibrariesByLearnerId(learnerIdString, page,
                                size);

                assertNotNull(result);
                assertEquals(2, result.getTotalElements());
                assertEquals(page, result.getPage());
                assertEquals(size, result.getSize());
        }

        @Test
        @DisplayName("Should throw exception when learner not found")
        void testGetAllLibrariesByLearnerIdNotFound() {
                UUID learnerId = UUID.randomUUID();
                when(learnerRepository.findById(learnerId)).thenReturn(Optional.empty());

                assertThrows(IllegalArgumentException.class,
                                () -> libraryService.getAllLibrariesByLearnerId(learnerId.toString(), 0, 10));
        }

        @Test
        @DisplayName("Should search libraries by name")
        void testSearchLibrary() {
                String name = "English";
                int page = 0;
                int size = 10;

                Library library = newLibrary(UUID.randomUUID(), "English Grammar", null, LibraryStatus.ACTIVE);
                Page<Library> libraryPage = new PageImpl<>(List.of(library), PageRequest.of(page, size), 1);

                LibraryResponse response = new LibraryResponse();
                response.setId(library.getId().toString());
                response.setName(library.getName());

                when(libraryRepository.searchLibrary(name, PageRequest.of(page, size)))
                                .thenReturn(Optional.of(libraryPage));
                when(modelMapper.map(library, LibraryResponse.class)).thenReturn(response);

                Pagination<LibraryResponse> result = libraryService.searchLibrary(name, page, size);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                assertEquals(page, result.getPage());
                assertEquals(size, result.getSize());
        }

        @Test
        @DisplayName("Should get library by id")
        void testGetLibraryById() {
                UUID libraryId = UUID.randomUUID();
                UUID learnerId = UUID.randomUUID();

                Learner learner = newLearner(learnerId, "Jane Smith");
                Library library = newLibrary(libraryId, "Test Library", learner, LibraryStatus.ACTIVE);

                LibraryResponse response = new LibraryResponse();
                response.setId(libraryId.toString());
                response.setName("Test Library");
                response.setLearnerId(learnerId.toString());

                when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
                when(modelMapper.map(library, LibraryResponse.class)).thenReturn(response);

                LibraryResponse result = libraryService.getLibraryById(libraryId.toString());

                assertNotNull(result);
                assertEquals(libraryId.toString(), result.getId());
                assertEquals("Test Library", result.getName());
                assertEquals(learnerId.toString(), result.getLearnerId());
        }

        @Test
        @DisplayName("Should create library successfully")
        void testCreateLibrary() {
                UUID learnerId = UUID.randomUUID();
                UUID libraryId = UUID.randomUUID();

                Learner learner = newLearner(learnerId, "Test Learner");

                LibraryRequest request = new LibraryRequest();
                request.setLearnerId(learnerId.toString());
                request.setName("New Library");

                Library mappedLibrary = newLibrary(libraryId, "New Library", learner, LibraryStatus.ACTIVE);

                LibraryResponse response = new LibraryResponse();
                response.setId(libraryId.toString());
                response.setName("New Library");
                response.setLearnerId(learnerId.toString());

                when(learnerRepository.findById(learnerId)).thenReturn(Optional.of(learner));
                when(modelMapper.map(request, Library.class)).thenReturn(mappedLibrary);
                when(libraryRepository.save(mappedLibrary)).thenReturn(mappedLibrary);
                when(modelMapper.map(mappedLibrary, LibraryResponse.class)).thenReturn(response);

                LibraryResponse result = libraryService.createLibrary(request);

                assertNotNull(result);
                assertEquals(libraryId.toString(), result.getId());
                assertEquals("New Library", result.getName());
                verify(libraryRepository, times(1)).save(mappedLibrary);
        }

        @Test
        @DisplayName("Should update library successfully")
        void testUpdateLibrary() {
                UUID libraryId = UUID.randomUUID();
                UUID learnerId = UUID.randomUUID();

                Learner learner = newLearner(learnerId, "Update Test");
                Library library = newLibrary(libraryId, "Old Name", learner, LibraryStatus.ACTIVE);

                LibraryRequest request = new LibraryRequest();
                request.setName("Updated Name");

                doAnswer(inv -> {
                        LibraryRequest src = inv.getArgument(0);
                        Library dest = inv.getArgument(1);
                        dest.setName(src.getName());
                        return null;
                }).when(modelMapper).map(any(LibraryRequest.class), any(Library.class));

                LibraryResponse response = new LibraryResponse();
                response.setId(libraryId.toString());
                response.setName("Updated Name");

                when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
                when(libraryRepository.save(any(Library.class))).thenAnswer(inv -> inv.getArgument(0));

                // ép đúng overload map(source, Class<D>)
                when(modelMapper.map(any(Library.class), org.mockito.ArgumentMatchers.<Class<LibraryResponse>>eq(LibraryResponse.class)))
                        .thenReturn(response);

                LibraryResponse result = libraryService.updateLibrary(libraryId.toString(), request);

                assertNotNull(result);
                assertEquals("Updated Name", result.getName());

                var captor = org.mockito.ArgumentCaptor.forClass(Library.class);
                verify(libraryRepository, times(1)).save(captor.capture());
                assertEquals("Updated Name", captor.getValue().getName());
        }

        @Test
        @DisplayName("Should soft delete library")
        void testSoftDeleteLibrary() {
                UUID libraryId = UUID.randomUUID();
                Library library = newLibrary(libraryId, "Test Library", null, LibraryStatus.ACTIVE);

                when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

                libraryService.softDeleteLibrary(libraryId.toString());

                assertEquals(LibraryStatus.INACTIVE, library.getStatus());
                assertNotNull(library.getDeletedAt());
                verify(libraryRepository, times(1)).save(library);
        }

        @Test
        @DisplayName("Should hard delete library")
        void testDeleteLibrary() {
                UUID libraryId = UUID.randomUUID();
                Library library = newLibrary(libraryId, "Test Library", null, LibraryStatus.ACTIVE);

                when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

                libraryService.deleteLibrary(libraryId.toString());

                verify(libraryRepository, times(1)).delete(library);
        }
}