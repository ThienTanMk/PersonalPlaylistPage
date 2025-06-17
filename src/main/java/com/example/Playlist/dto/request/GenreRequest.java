package com.example.Playlist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreRequest {
    @NotBlank(message = "NAME_IS_REQUIRED")
    private String name;
    private String description;
    @NotNull(message = "IS_ACTIVE_IS_REQUIRED")
    private Boolean isActive;
}
