package com.example.Playlist.controller;

import com.example.Playlist.dto.ApiResponse;
import com.example.Playlist.dto.request.PlaylistRequest;
import com.example.Playlist.dto.response.PlaylistResponse;
import com.example.Playlist.service.PlaylistService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

        private final PlaylistService playlistService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<PlaylistResponse>> createPlaylist(
                        @RequestPart("playlist") PlaylistRequest playlistRequest,
                        @RequestPart(value = "image", required = false) MultipartFile image) {
                PlaylistResponse response = playlistService.createPlaylist(playlistRequest, image);
                return ResponseEntity.ok(ApiResponse.<PlaylistResponse>builder()
                                .code(200)
                                .message("Tạo playlist thành công")
                                .data(response)
                                .build());
        }

        @PutMapping(value = "/{playlistId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<PlaylistResponse>> updatePlaylist(
                        @PathVariable Long playlistId,
                        @RequestPart("playlist") PlaylistRequest playlistRequest,
                        @RequestPart(value = "image", required = false) MultipartFile image) {
                PlaylistResponse response = playlistService.updatePlaylist(playlistId, playlistRequest, image);
                return ResponseEntity.ok(ApiResponse.<PlaylistResponse>builder()
                                .code(1000)
                                .message("Cập nhật playlist thành công")
                                .data(response)
                                .build());
        }

        @DeleteMapping("/{playlistId}")
        public ResponseEntity<ApiResponse<String>> deletePlaylist(@PathVariable Long playlistId) {
                playlistService.deletePlaylist(playlistId);
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .code(1000)
                                .message("Xóa playlist thành công")
                                .data("Playlist đã được xóa")
                                .build());
        }

        @PostMapping("/{playlistId}/tracks/{trackId}")
        public ResponseEntity<ApiResponse<PlaylistResponse>> addTrackToPlaylist(
                        @PathVariable Long playlistId,
                        @PathVariable Long trackId) {
                PlaylistResponse response = playlistService.addTrackToPlaylist(playlistId, trackId);
                return ResponseEntity.ok(ApiResponse.<PlaylistResponse>builder()
                                .code(1000)
                                .message("Thêm track vào playlist thành công")
                                .data(response)
                                .build());
        }

        @DeleteMapping("/{playlistId}/tracks/{trackId}")
        public ResponseEntity<ApiResponse<PlaylistResponse>> removeTrackFromPlaylist(
                        @PathVariable Long playlistId,
                        @PathVariable Long trackId) {
                PlaylistResponse response = playlistService.removeTrackFromPlaylist(playlistId, trackId);
                return ResponseEntity.ok(ApiResponse.<PlaylistResponse>builder()
                                .code(1000)
                                .message("Xoá track khỏi playlist thành công")
                                .data(response)
                                .build());
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getAllPlaylistsByUser() {
                List<PlaylistResponse> responses = playlistService.getAllPlaylistsByCurrentUser();
                return ResponseEntity.ok(ApiResponse.<List<PlaylistResponse>>builder()
                                .code(1000)
                                .message("Lấy tất cả playlist của người dùng thành công")
                                .data(responses)
                                .build());
        }
}
