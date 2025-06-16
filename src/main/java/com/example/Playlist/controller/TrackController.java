package com.example.Playlist.controller;

import com.example.Playlist.dto.ApiResponse;
import com.example.Playlist.dto.request.TrackRequest;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.exception.AppException;
import com.example.Playlist.exception.ErrorCode;
import com.example.Playlist.service.TrackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
    @RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackController {
    TrackService trackService;
    private static final String UPLOAD_IMAGE_DIR = "uploads/images/";
    private static final String UPLOAD_AUDIO_DIR = "uploads/audios/";

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrackResponse>>> getAllTracks() {
        List<TrackResponse> tracks = trackService.getAllTracks();
        return ResponseEntity.ok(ApiResponse.<List<TrackResponse>>builder()
                .code(1000)
                .message("Lấy danh sách bài hát thành công")
                .data(tracks)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrackResponse>> getTrackById(@PathVariable Long id) {
        TrackResponse track = trackService.getTrackById(id);
        return ResponseEntity.ok(ApiResponse.<TrackResponse>builder()
                .code(1000)
                .message("Lấy bài hát thành công")
                .data(track)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .data("Xóa bài hát thành công")
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("mainArtist") String mainArtist,
            @RequestParam("isPublic") boolean isPublic,
            @RequestParam(value = "genreId", required = false) Long genreId,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return ResponseEntity.ok(ApiResponse.<TrackResponse>builder()
                .code(1000)
                .message("Cập nhật bài hát thành công")
                .data(trackService.updateTrackAndImage(id, title, description, mainArtist,genreId,isPublic, image))
                .build());
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable String filename) throws IOException {

        Path path = Paths.get(UPLOAD_IMAGE_DIR).resolve(filename);
        byte[] imageBytes = Files.readAllBytes(path);

        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(imageBytes.length)
                .body(resource);

    }

    @GetMapping("/audios/{filename}")
    public ResponseEntity<Resource> playAudio(@PathVariable String filename) throws IOException {
        Path path = Paths.get(UPLOAD_AUDIO_DIR).resolve(filename);
        byte[] imageBytes = Files.readAllBytes(path);

        ByteArrayResource resource = new ByteArrayResource(imageBytes);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "audio/mpeg"; // Mặc định là MP3
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(imageBytes.length)
                .body(resource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrackResponse>> createTrack(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam("mainArtist") String mainArtist,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "genreId", required = false) Long genreId,
            @RequestPart("file") MultipartFile audio,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        String contentType = audio.getContentType(); // ví dụ: "audio/mpeg"
        if (contentType == null || (!contentType.equals("audio/mpeg") && !contentType.equals("audio/wav"))) {
            throw new AppException(ErrorCode.INVALID_AUDIO_FORMAT);
        }
        // Tạo TrackRequest từ các param
        TrackRequest trackRequest = new TrackRequest();
        trackRequest.setNameTrack(name);
        trackRequest.setMainArtist(mainArtist);
        trackRequest.setDescription(description);

        if (genreId == null) {
            trackRequest.setGenreId(0);
        } else {
            trackRequest.setGenreId(genreId);
        }

        // Gọi service
        TrackResponse response = trackService.createTrack(trackRequest, audio, image);

        return ResponseEntity.ok(ApiResponse.<TrackResponse>builder()
                .code(1000)
                .message("Tạo bài hát thành công")
                .data(response)
                .build());
    }

}
