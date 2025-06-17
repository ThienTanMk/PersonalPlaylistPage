package com.example.Playlist.controller;

import com.example.Playlist.dto.request.GenreRequest;
import com.example.Playlist.dto.response.GenreResponse;
import com.example.Playlist.dto.ApiResponse;
import com.example.Playlist.service.GenreService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class GenreController {

    GenreService genreService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        List<GenreResponse> genres = genreService.getAllGenres();
        return ResponseEntity.ok(ApiResponse.<List<GenreResponse>>builder()
                .code(1000)
                .message("Lấy danh sách thể loại thành công")
                .data(genres)
                .build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(
            @RequestPart("genre") GenreRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        GenreResponse response = genreService.createGenre(request, image);
        return ResponseEntity.ok(ApiResponse.<GenreResponse>builder()
                .code(1000)
                .message("Tạo thể loại thành công")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable Long id,
            @RequestPart("genre") GenreRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        GenreResponse response = genreService.updateGenre(id, request, image);

        return ResponseEntity.ok(ApiResponse.<GenreResponse>builder()
                .code(1000)
                .message("Cập nhật thể loại thành công")
                .data(response)
                .build());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Xóa thể loại thành công")
                .data("Đã xóa")
                .build());
    }
}
