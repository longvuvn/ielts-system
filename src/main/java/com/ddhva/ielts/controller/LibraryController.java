package com.ddhva.ielts.controller;


import com.ddhva.ielts.dto.library.req.LibraryRequest;
import com.ddhva.ielts.dto.library.res.LibraryResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.service.LibraryService;
import com.ddhva.ielts.service.exception.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<ApiResponse<Pagination<LibraryResponse>>> getAllByLearnerId(
            @PathVariable String learnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pagination<LibraryResponse> response = libraryService.getAllLibrariesByLearnerId(learnerId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Libraries fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Pagination<LibraryResponse>>> search(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pagination<LibraryResponse> response = libraryService.searchLibrary(name, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Libraries fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryResponse>> getById(@PathVariable String id){
        LibraryResponse response = libraryService.getLibraryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Library fetched successfully",
                        response
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LibraryResponse>> create(@Valid @RequestBody LibraryRequest request){
        LibraryResponse response = libraryService.createLibrary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Library created successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryResponse>> update(@PathVariable String id, @RequestBody LibraryRequest request){
        LibraryResponse response = libraryService.updateLibrary(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Library updated successfully",
                        response
                )
        );
    }

    @DeleteMapping("/soft-delete/{libraryId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable String libraryId){
        libraryService.softDeleteLibrary(libraryId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Library soft deleted successfully"
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id){
        libraryService.deleteLibrary(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Library deleted successfully"
                )
        );
    }
}