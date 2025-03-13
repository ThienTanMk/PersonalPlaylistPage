package com.example.Playlist.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackDto {
    Long idTrack;
    String nameTrack;
    String userName;
    LocalTime duration;
    LocalDate createdAt;
    int likeCount;
    int viewCount;
    int commentCount;
    String urlTrack;
    String image;
}

