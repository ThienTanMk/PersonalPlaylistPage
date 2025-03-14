package com.example.Playlist.service;

import com.example.Playlist.dto.request.TrackRequest;
import com.example.Playlist.dto.response.TrackResponse;
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

    public List<TrackResponse> getAllTracks() {
        return trackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TrackResponse getTrackById(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track không tồn tại"));

        return mapToResponse(track);
    }

    public String deleteTrack(Long id) {
        if (!trackRepository.existsById(id)) {
            throw new RuntimeException("Bài hát không tồn tại");
        }
        trackRepository.deleteById(id);
        return "Bài hát đã được xóa thành công";
    }

    public TrackResponse updateTrack(Long id, TrackRequest trackRequest) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track không tồn tại"));

        track.setNameTrack(trackRequest.getNameTrack());
        track.setDescription(trackRequest.getDescription());
        track.setIsPublic(trackRequest.getIsPublic());
        track.setMainArtist(trackRequest.getMainArtist());
        trackRepository.save(track);
        return mapToResponse(track);
    }

    private TrackResponse mapToResponse(Track track) {
        return TrackResponse.builder()
                .idTrack(track.getIdTrack())
                .nameTrack(track.getNameTrack())
                .userName(track.getUserName())
                .duration(track.getDuration())
                .createdAt(track.getCreatedAt())
                .likeCount(track.getLikeCount())
                .viewCount(track.getViewCount())
                .commentCount(track.getCommentCount())
                .urlTrack(track.getUrlTrack())
                .image(track.getImage())
                .isPublic(track.getIsPublic())
                .description(track.getDescription())
                .mainArtist(track.getMainArtist())
                .build();
    }
    public TrackResponse uploadImage(String imageUrl, Long id)  {
        Track track =  trackRepository.findById(id).orElseThrow(() -> new RuntimeException("Can not find track"));
        track.setImage("http://localhost:8181/" + imageUrl);
        trackRepository.save(track);
        return mapToResponse(track);
    }
}
