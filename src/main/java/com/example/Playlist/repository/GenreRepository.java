package com.example.Playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Playlist.entity.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre,Long> {
    List<Genre> findByIsActiveTrue();
    Optional<Genre> findByIdAndIsActiveTrue(Long id);
}
