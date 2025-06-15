package com.example.Playlist.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String imageName;// ảnh

    @Column(nullable = false,columnDefinition = "NVARCHAR(100)")
    String name;

    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    String description;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
        name = "playlist_track",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    List<Track> tracks;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    WebUser user;
}
