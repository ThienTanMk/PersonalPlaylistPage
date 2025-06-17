package com.example.Playlist.dto.request;

import lombok.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistRequest {
    @NotBlank(message = "Tên playlist không được để trống")
    @Size(max = 100, message = "Tên playlist không được vượt quá 100 ký tự")
    private String name;
    private String description;
    private List<Long> trackIds; // Danh sách ID bài hát để thêm lúc tạo playlist
}
