package com.example.Playlist.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackRequest {
    String nameTrack;
    String userName;
    LocalTime duration;
    LocalDate createdAt;
    String urlTrack;
    String description;
    String mainArtist;
    Boolean isPublic;
}

