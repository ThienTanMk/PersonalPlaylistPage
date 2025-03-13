package com.example.Playlist.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Track")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idTrack;
    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    String nameTrack;
    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    String userName;
    @Column(nullable = false)
    LocalTime duration;
    int likeCount;
    int viewCount;
    int commentCount;
    @Column(nullable = false)
    String urlTrack;
    @Column(nullable = false)
    String image;
    @Column(name = "created_at")
    LocalDate createdAt;

}
