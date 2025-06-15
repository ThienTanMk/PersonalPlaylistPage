package com.example.Playlist.configuration;

import java.io.File;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FolderConfig {
    private static final String UPLOAD_IMAGE_DIR = "uploads/images/";
    private static final String UPLOAD_AUDIO_DIR = "uploads/audios/";
    @PostConstruct
    public void initFolder(){
        File uploadImageFolder = new File(UPLOAD_IMAGE_DIR);
        if (!uploadImageFolder.exists()) {
            uploadImageFolder.mkdirs();
            System.out.println("Created upload folder: " + UPLOAD_IMAGE_DIR);
        }
        else System.out.println("Folder existed: " + UPLOAD_IMAGE_DIR);
        File uploadAudioFolder = new File(UPLOAD_AUDIO_DIR);
        if (!uploadAudioFolder.exists()) {
            uploadAudioFolder.mkdirs();
            System.out.println("Created upload folder: " + UPLOAD_AUDIO_DIR);
        }
        else System.out.println("Folder existed: " + UPLOAD_AUDIO_DIR);
    }
}
