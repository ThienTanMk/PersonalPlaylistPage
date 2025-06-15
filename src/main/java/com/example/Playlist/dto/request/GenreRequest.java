package com.example.Playlist.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreRequest {
    private String name;
    private String description;
    private Boolean isActive;
}
