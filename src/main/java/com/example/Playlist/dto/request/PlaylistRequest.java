package com.example.Playlist.dto.request;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistRequest {
    private String name;
    private String description;
    private List<Long> trackIds; // Danh sách ID bài hát để thêm lúc tạo playlist
}
