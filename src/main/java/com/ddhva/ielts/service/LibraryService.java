package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.library.req.LibraryRequest;
import com.ddhva.ielts.dto.library.res.LibraryResponse;
import com.ddhva.ielts.dto.pagination.Pagination;

public interface LibraryService {
    Pagination<LibraryResponse> getAllLibrariesByLearnerId(String learnerId, int page, int size);
    Pagination<LibraryResponse> searchLibrary(String title, int page, int size);
    LibraryResponse getLibraryById(String libraryId);
    LibraryResponse createLibrary(LibraryRequest libraryRequest);
    LibraryResponse updateLibrary(String libraryId, LibraryRequest libraryRequest);
    void softDeleteLibrary(String libraryId);
    void deleteLibrary(String libraryId);
}
