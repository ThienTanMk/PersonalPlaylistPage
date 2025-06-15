package com.example.Playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Playlist.entity.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre,Long> {
    
}
