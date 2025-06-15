package com.example.Playlist.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "Genre")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @Id
    @Column(name = "genre_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,columnDefinition = "NVARCHAR(100)")
    private String name; // tên

    private String description;// mô tả của thể loại
    @Column(columnDefinition = "NVARCHAR(255)")
    private String imageName;// ảnh

    @Column(nullable = false)
    private boolean isActive;// có được sử dụng ko

    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Track> tracks;

    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    WebUser user;
}
