package com.example.Playlist.service;

import com.example.Playlist.dto.request.TrackRequest;
import com.example.Playlist.dto.response.GenreResponse;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.entity.Genre;
import com.example.Playlist.entity.Track;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.exception.AppException;
import com.example.Playlist.exception.ErrorCode;
import com.example.Playlist.repository.GenreRepository;
import com.example.Playlist.repository.TrackRepository;
import com.example.Playlist.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackService {
    private static final String UPLOAD_IMAGE_DIR = "uploads/images/";

    TrackRepository trackRepository;
    GenreRepository genreRepository;
    UserRepository userRepository;

    public List<TrackResponse> getAllTracks() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return trackRepository.findAllByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TrackResponse getTrackById(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track không tồn tại"));

        return mapToResponse(track);
    }

    public void deleteTrack(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại"));

        if (!track.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Xóa file audio nếu có
        if (track.getTrackAudio() != null) {
            Path audioPath = Paths.get("uploads/audios", track.getTrackAudio());
            try {
                Files.deleteIfExists(audioPath);
            } catch (IOException e) {
                // Ghi log nhưng không dừng chương trình
                System.err.println("Không thể xóa file audio: " + audioPath + " - " + e.getMessage());
            }
        }

        // Xóa file ảnh nếu có
        if (track.getImage() != null) {
            Path imagePath = Paths.get("uploads/images", track.getImage());
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Không thể xóa file ảnh: " + imagePath + " - " + e.getMessage());
            }
        }

        // Xóa track khỏi database
        trackRepository.deleteById(id);
    }

    public TrackResponse updateTrack(Long id, TrackRequest trackRequest) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track không tồn tại"));

        track.setNameTrack(trackRequest.getNameTrack());
        track.setDescription(trackRequest.getDescription());
        track.setIsPublic(trackRequest.getIsPublic());
        track.setMainArtist(trackRequest.getMainArtist());
        if(trackRequest.getGenreId()!=0){
            Genre genre = genreRepository.findByIdAndIsActiveTrue(trackRequest.getGenreId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_GENRE));
            track.setGenre(genre);
        }
        trackRepository.save(track);
        return mapToResponse(track);
    }

    public TrackResponse updateTrackAndImage(Long id,
            String title,
            String description,
            String mainArtist,
            Long genreId,
            boolean isPublic,
            MultipartFile image) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track không tồn tại"));
        if(!track.getUser().getEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        try {

            // Nếu có file ảnh, lưu với tên chứa ngày giờ upload
            if (image != null && !image.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_IMAGE_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                // Lấy thời gian hiện tại và format thành chuỗi
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String originalFilename = image.getOriginalFilename();
                String newFileName = timestamp + "_" + originalFilename; // Đổi tên ảnh

                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                uploadImage(newFileName, id);
            }

            TrackRequest trackRequest = TrackRequest.builder()
                    .isPublic(isPublic)
                    .nameTrack(title)
                    .description(description)
                    .mainArtist(mainArtist)
                    .genreId(Optional.ofNullable(genreId).orElse(0L))
                    .build();

            TrackResponse updatedTrack = updateTrack(id, trackRequest);

            return updatedTrack;
        } catch (Exception e) {
            return null;
        }
    }
    private GenreResponse mapToGenreResponse(Genre genre){
        if(genre==null) return null;
        return GenreResponse.builder()
                            .id(genre.getId())
                            .name(genre.getName())
                            .build();
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
                .urlTrack("http://localhost:8080/api/v1/tracks/audios/" + track.getTrackAudio())
                .image("http://localhost:8080/api/v1/tracks/images/" + track.getImage())
                .isPublic(track.getIsPublic())
                .description(track.getDescription())
                .mainArtist(track.getMainArtist())
                .genre(mapToGenreResponse(track.getGenre()))
                .build();
    }

    public TrackResponse uploadImage(String imageName, Long id) {
        Track track = trackRepository.findById(id).orElseThrow(() -> new RuntimeException("Can not find track"));
        track.setImage(imageName);
        trackRepository.save(track);
        return mapToResponse(track);
    }

    public TrackResponse createTrack(
            TrackRequest trackRequest,
            MultipartFile audio,
            MultipartFile image) throws IOException {
        if (audio == null) throw new AppException(ErrorCode.UNCATEGORIZED);
        String contentType = audio.getContentType(); // ví dụ: "audio/mpeg"
        if (contentType == null || (!contentType.equals("audio/mpeg") && !contentType.equals("audio/wav"))) {
            throw new AppException(ErrorCode.INVALID_AUDIO_FORMAT);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm người dùng hiện tại
        WebUser user = userRepository.findByEmail(email);
        if (user == null)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        Genre genre = null;
        if (trackRequest.getGenreId() > 0) {
            genre = genreRepository.findByIdAndIsActiveTrue(trackRequest.getGenreId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_GENRE));
        }

        // Tạo track mới
        Track track = Track.builder()
                .nameTrack(trackRequest.getNameTrack())
                .userName(user.getName()) // hoặc email/tên tuỳ app
                .duration(LocalTime.of(0, 3, 30)) // giả định hoặc truyền từ client
                .likeCount(0)
                .viewCount(0)
                .commentCount(0)
                .createdAt(LocalDate.now())
                .description(trackRequest.getDescription())
                .mainArtist(trackRequest.getMainArtist())
                .isPublic(trackRequest.getIsPublic())
                .genre(genre) // cần truyền genre hoặc tìm theo id
                .user(user)
                .build();

        // Lưu audio
        if (audio != null && !audio.isEmpty()) {
            Path audioPath = Paths.get("uploads/audios");
            if (!Files.exists(audioPath))
                Files.createDirectories(audioPath);

            String audioFilename = System.currentTimeMillis() + "_" + audio.getOriginalFilename();
            Files.copy(audio.getInputStream(), audioPath.resolve(audioFilename), StandardCopyOption.REPLACE_EXISTING);
            track.setTrackAudio(audioFilename);
        }

        // Lưu image
        if (image != null && !image.isEmpty()) {
            Path imagePath = Paths.get("uploads/images");
            if (!Files.exists(imagePath))
                Files.createDirectories(imagePath);

            String imageFilename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), imagePath.resolve(imageFilename), StandardCopyOption.REPLACE_EXISTING);
            track.setImage(imageFilename);
        }

        trackRepository.save(track);
        return mapToResponse(track);
    }

}
