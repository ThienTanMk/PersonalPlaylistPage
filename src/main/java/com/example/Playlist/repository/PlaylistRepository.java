package com.example.Playlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Playlist.entity.Playlist;
import com.example.Playlist.entity.WebUser;
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist,Long>{
    List<Playlist> findAllByUser(WebUser user);

}
