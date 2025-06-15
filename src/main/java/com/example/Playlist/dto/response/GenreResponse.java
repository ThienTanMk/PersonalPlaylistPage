package com.example.Playlist.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreResponse {
    private Long id;
    private String name;
    private String description;
    private String imageName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy; // Email của user tạo
    private List<TrackResponse> tracks; // Danh sách bài hát thuộc thể loại
}
