package com.example.Playlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Playlist.entity.Playlist;
import com.example.Playlist.entity.WebUser;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist,Long>{
    List<Playlist> findAllByUser(WebUser user);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM PLAYLIST_TRACK WHERE TRACK_ID = :trackId", nativeQuery = true)
    int deleteAllByTrackId(@Param("trackId") Long trackId);
}
