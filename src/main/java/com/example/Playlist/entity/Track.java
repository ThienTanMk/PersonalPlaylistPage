package com.example.Playlist.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    @NotBlank(message = "Tên bài hát không được để trống")
    @Size(max = 255, message = "Tên bài hát không được dài quá 255 ký tự")
    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    String nameTrack;

    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Tên nghệ sĩ không được để trống")
    @Size(max = 255, message = "Tên nghệ sĩ không được dài quá 255 ký tự")
    String userName;

    @Column(nullable = false)
    LocalTime duration;

    @Min(value = 0, message = "Lượt thích không thể âm")
    int likeCount;
    
    @Min(value = 0, message = "Lượt xem không thể âm")
    int viewCount;

    @Min(value = 0, message = "Lượt bình luận không thể âm")
    int commentCount;

    @NotBlank(message = "Tên bài hát không được để trống")
    @Column(nullable = false,columnDefinition = "NVARCHAR(128)")
    String trackAudio;

    @Column(nullable = true,columnDefinition = "NVARCHAR(128)")
    String image;

    @Column(name = "created_at", nullable = true)
    LocalDate createdAt;

    @Column(columnDefinition = "NVARCHAR(255)")
    String description;

    @Column(name = "main_artist",columnDefinition = "NVARCHAR(255)")
    String mainArtist;

    @Column(name = "is_public", nullable = true)
    Boolean isPublic;

    @ManyToOne
    @JoinColumn(name = "genre_id", nullable = true)
    Genre genre;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    WebUser user;


    @ManyToMany(mappedBy = "tracks")
    List<Playlist> playlists;
}
