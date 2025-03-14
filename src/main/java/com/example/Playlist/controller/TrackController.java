package com.example.Playlist.controller;

import com.example.Playlist.dto.ApiResponse;
import com.example.Playlist.dto.request.TrackRequest;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.service.TrackService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackController {
    TrackService trackService;

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
    public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(@PathVariable Long id, @Valid @RequestBody TrackRequest trackRequest) {
        TrackResponse updatedTrack = trackService.updateTrack(id, trackRequest);
        return ResponseEntity.ok(ApiResponse.<TrackResponse>builder()
                .code(1000)
                .message("Cập nhật bài hát thành công")
                .data(updatedTrack)
                .build());
    }
}
