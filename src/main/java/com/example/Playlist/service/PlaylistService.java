package com.example.Playlist.service;

import com.example.Playlist.dto.request.PlaylistRequest;
import com.example.Playlist.dto.response.PlaylistResponse;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.entity.Playlist;
import com.example.Playlist.entity.Track;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.repository.PlaylistRepository;
import com.example.Playlist.repository.TrackRepository;
import com.example.Playlist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PlaylistService {

    static String UPLOAD_IMAGE_DIR = "uploads/images/";

    PlaylistRepository playlistRepository;
    TrackRepository trackRepository;
    UserRepository webUserRepository;

    public PlaylistResponse createPlaylist(PlaylistRequest request, MultipartFile image) {
        // Lấy user từ SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WebUser user = webUserRepository.findByEmail(email);
        // if(user==null){
        //     throw new RuntimeException("Người dùng chưa đăng nhập");
        // }
        // Lưu file ảnh (nếu có)
        String imageName = null;
        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_IMAGE_DIR);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String originalFilename = image.getOriginalFilename();
                imageName = timestamp + "_" + originalFilename;
                Path filePath = uploadPath.resolve(imageName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh", e);
            }
        }
        List<Long> trackIds = request.getTrackIds();
        // Lấy danh sách track
        List<Track> tracks = new ArrayList<>();
        if (request.getTrackIds() != null && !request.getTrackIds().isEmpty()) {
            tracks = trackRepository.findAllById(request.getTrackIds());
            if (tracks.size() != trackIds.size()) {
                throw new RuntimeException("Một hoặc nhiều track không tồn tại");
            }
        }

        // Tạo playlist
        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageName(imageName)
                .createdAt(LocalDateTime.now()) 
                .user(user)
                .tracks(tracks)
                .build();

        playlistRepository.save(playlist);

        return mapToResponse(playlist);
    }

    public PlaylistResponse addTrackToPlaylist(Long playlistId, Long trackId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy playlist"));

        // Kiểm tra quyền sở hữu playlist
        if (!playlist.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền sửa playlist này");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy track"));

        // Kiểm tra nếu track đã có trong playlist
        if (playlist.getTracks().contains(track)) {
            throw new RuntimeException("Track đã tồn tại trong playlist");
        }

        playlist.getTracks().add(track);
        playlistRepository.save(playlist);

        return mapToResponse(playlist);
    }

    public PlaylistResponse removeTrackFromPlaylist(Long playlistId, Long trackId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy playlist"));

        // Kiểm tra quyền sở hữu playlist
        if (!playlist.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền sửa playlist này");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy track"));

        if (!playlist.getTracks().contains(track)) {
            throw new RuntimeException("Track không tồn tại trong playlist");
        }

        playlist.getTracks().remove(track);
        playlistRepository.save(playlist);

        return mapToResponse(playlist);
    }

    public void deletePlaylist(Long playlistId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy playlist"));

        // Kiểm tra quyền sở hữu playlist
        if (!playlist.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền xóa playlist này");
        }

        playlistRepository.delete(playlist);
    }

    public PlaylistResponse updatePlaylist(Long playlistId, PlaylistRequest request, MultipartFile imageFile) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy playlist"));

        if (!playlist.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền cập nhật playlist này");
        }

        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_IMAGE_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String originalFilename = imageFile.getOriginalFilename();
                String newFileName = timestamp + "_" + originalFilename;

                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                playlist.setImageName(newFileName);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh mới", e);
            }
        }
        if(request.getTrackIds()!=null){
            List<Track> tracks = trackRepository.findAllById(request.getTrackIds());
            if(tracks.size()!=request.getTrackIds().size()){
                throw new RuntimeException("Một hoặc nhiều track không tồn tại");
            }
            playlist.setTracks(tracks);
        }
        playlistRepository.save(playlist);
        return mapToResponse(playlist);
    }

    private PlaylistResponse mapToResponse(Playlist playlist) {
        List<TrackResponse> trackResponses = playlist.getTracks().stream().map(track -> TrackResponse.builder()
                .idTrack(track.getIdTrack())
                .nameTrack(track.getNameTrack())
                .userName(track.getUserName())
                .duration(track.getDuration())
                .createdAt(track.getCreatedAt())
                .likeCount(track.getLikeCount())
                .viewCount(track.getViewCount())
                .commentCount(track.getCommentCount())
                .urlTrack(track.getTrackAudio())
                .image(track.getImage())
                .isPublic(track.getIsPublic())
                .description(track.getDescription())
                .mainArtist(track.getMainArtist())
                .build()).collect(Collectors.toList());

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .imageName(playlist.getImageName())
                .createdAt(playlist.getCreatedAt())
                .userEmail(playlist.getUser().getEmail())
                .tracks(trackResponses)
                .build();
    }

    public List<PlaylistResponse> getAllPlaylistsByCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WebUser user = webUserRepository.findByEmail(email);
        List<Playlist> playlists = playlistRepository.findAllByUser(user);
        return playlists.stream().map(this::mapToResponse).toList();
    }

}
