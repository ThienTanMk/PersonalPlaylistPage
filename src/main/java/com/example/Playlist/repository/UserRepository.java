package com.example.Playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Playlist.entity.WebUser;

@Repository
public interface UserRepository extends JpaRepository<WebUser,Long> {
    WebUser findByEmail(String email);   
}
