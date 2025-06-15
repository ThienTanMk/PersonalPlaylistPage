package com.example.Playlist.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private String imageName; // Tên file ảnh đã lưu (để load)
    private LocalDateTime createdAt;
    private String userEmail;
    private List<TrackResponse> tracks;
}
