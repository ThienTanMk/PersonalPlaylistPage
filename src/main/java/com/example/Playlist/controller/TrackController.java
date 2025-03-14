package com.example.Playlist.controller;

import com.example.Playlist.dto.ApiResponse;
import com.example.Playlist.dto.request.TrackRequest;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.service.TrackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("")
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Xóa bài hát thành công")
                .data("Track Deleted")
                .build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("mainArtist") String mainArtist,
            @RequestParam("isPublic") boolean isPublic,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            String imageUrl = null;

            // Nếu có file ảnh, lưu với tên chứa ngày giờ upload
            if (image != null && !image.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_IMAGE_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Lấy thời gian hiện tại và format thành chuỗi
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String originalFilename = image.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // Lấy đuôi file
                String newFileName = timestamp + "_" + originalFilename; // Đổi tên ảnh

                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                imageUrl = UPLOAD_IMAGE_DIR + newFileName; // URL để truy cập ảnh từ frontend
                trackService.uploadImage(imageUrl, id);
            }

            TrackRequest trackRequest = TrackRequest.builder()
                    .isPublic(isPublic)
                    .nameTrack(title)
                    .description(description)
                    .mainArtist(mainArtist)
                    .build();
            TrackResponse updatedTrack = trackService.updateTrack(id, trackRequest);

            return ResponseEntity.ok(ApiResponse.<TrackResponse>builder()
                    .code(1000)
                    .message("Cập nhật bài hát thành công")
                    .data(updatedTrack)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<TrackResponse>builder()
                    .code(500)
                    .message("Lỗi cập nhật bài hát: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/uploads/images/{filename}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable String filename) throws IOException{

        Path path = Paths.get(UPLOAD_IMAGE_DIR).resolve(filename);
        byte[] imageBytes = Files.readAllBytes(path);

        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(imageBytes.length)
                .body(resource);

    }

    @GetMapping("/uploads/audios/{filename}")
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

}
