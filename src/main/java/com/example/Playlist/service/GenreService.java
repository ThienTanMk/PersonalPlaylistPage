package com.example.Playlist.service;

import com.example.Playlist.dto.request.GenreRequest;
import com.example.Playlist.dto.response.GenreResponse;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.entity.Genre;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.repository.GenreRepository;
import com.example.Playlist.repository.UserRepository;
import com.example.Playlist.exception.AppException;
import com.example.Playlist.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class GenreService {

    GenreRepository genreRepository;
    UserRepository webUserRepository;


    public List<GenreResponse> getAllGenres() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WebUser user = webUserRepository.findByEmail(email);
        return genreRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    public List<GenreResponse> getActiveGenres() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WebUser user = webUserRepository.findByEmail(email);
        return genreRepository.findByUserAndIsActiveTrue(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    public GenreResponse createGenre(GenreRequest request, MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WebUser user = webUserRepository.findByEmail(email);

        // Tìm genre theo tên (bất kể active hay không)
        Optional<Genre> optionalGenre = genreRepository.findByNameIgnoreCase(request.getName());

        Genre genre;
        if (optionalGenre.isPresent()) {
            genre = optionalGenre.get();
            // Nếu genre cũ đang inactive, thì "kích hoạt lại" và cập nhật dữ liệu mới
            if (!genre.isActive()) {
                genre.setDescription(request.getDescription());
                genre.setActive(true);
                genre.setUser(user); // Gán lại user tạo (nếu bạn muốn)
                genre.setCreatedAt(LocalDateTime.now()); // Hoặc giữ nguyên createdAt cũ nếu bạn muốn

                // Nếu có ảnh mới => cập nhật
                if (image != null && !image.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                    Path imagePath = Paths.get("uploads/genres", fileName);
                    try {
                        Files.createDirectories(imagePath.getParent());
                        Files.write(imagePath, image.getBytes());
                        genre.setImageName(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException("Lỗi khi lưu ảnh thể loại", e);
                    }
                }

                Genre saved = genreRepository.save(genre);
                return mapToResponse(saved);
            } else {
                // Genre đang active => lỗi trùng tên như trước
                throw new RuntimeException("Tên thể loại đã tồn tại");
            }
        }

        // Nếu không tìm thấy genre cùng tên => tạo mới
        genre = Genre.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path imagePath = Paths.get("uploads/genres", fileName);
            try {
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, image.getBytes());
                genre.setImageName(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh thể loại", e);
            }
        }

        Genre saved = genreRepository.save(genre);
        return mapToResponse(saved);
    }


    public GenreResponse updateGenre(Long id, GenreRequest request, MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        if (!genre.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }

        genre.setName(request.getName());
        genre.setDescription(request.getDescription());
        genre.setActive(request.getIsActive());

        if (image != null && !image.isEmpty()) {
            // Sinh tên file duy nhất
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();

            // Đường dẫn lưu (tùy bạn setup)
            Path imagePath = Paths.get("uploads/genres", fileName);

            try {
                // Tạo thư mục nếu chưa có
                Files.createDirectories(imagePath.getParent());
                // Ghi file
                Files.write(imagePath, image.getBytes());

                // Lưu tên ảnh vào genre
                genre.setImageName(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh thể loại", e);
            }
        }

        return mapToResponse(genreRepository.save(genre));
    }

    public void deleteGenre(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        if (!genre.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        genre.setActive(false);
        genreRepository.save(genre);
    }

    private GenreResponse mapToResponse(Genre genre) {
        List<TrackResponse> trackResponses = new ArrayList<>();
        if (genre.getTracks() != null && !genre.getTracks().isEmpty()) {
            trackResponses = genre.getTracks().stream().map(track -> TrackResponse.builder()
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
        }
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .imageName(genre.getImageName())
                .isActive(genre.isActive())
                .createdAt(genre.getCreatedAt())
                .createdBy(genre.getUser().getEmail())
                .tracks(trackResponses)
                .build();
    }


}
