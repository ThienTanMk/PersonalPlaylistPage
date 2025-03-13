package com.example.Playlist.service;

import com.example.Playlist.dto.TrackDto;
import com.example.Playlist.entity.Track;
import com.example.Playlist.repository.TrackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackService {
    TrackRepository trackRepository;

    public List<TrackDto> getAllTracks() {
        return trackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(track -> new TrackDto(
                        track.getIdTrack(),
                        track.getNameTrack(),
                        track.getUserName(),
                        track.getDuration(),
                        track.getCreatedAt(),
                        track.getLikeCount(),
                        track.getViewCount(),
                        track.getCommentCount(),
                        track.getUrlTrack(),
                        track.getImage()
                ))
                .collect(Collectors.toList());
    }
    public void updateTrack(TrackDto trackDto) {
        Track track = trackRepository.findById(trackDto.getIdTrack())
                .orElseThrow(() -> new RuntimeException("Track không tồn tại"));

        track.setNameTrack(trackDto.getNameTrack());
        track.setUserName(trackDto.getUserName());
        track.setDuration(trackDto.getDuration());
        track.setCreatedAt(trackDto.getCreatedAt());
        track.setUrlTrack(trackDto.getUrlTrack());
        track.setImage(trackDto.getImage());

        trackRepository.save(track);
    }
    public void deleteTrack(Long id) {
        if (!trackRepository.existsById(id)) {
            throw new RuntimeException("Track không tồn tại");
        }
        trackRepository.deleteById(id);
    }

}
