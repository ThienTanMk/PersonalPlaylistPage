package com.example.Playlist.service;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.example.Playlist.dto.request.TrackRequest;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.entity.Genre;
import com.example.Playlist.entity.Track;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.exception.AppException;
import com.example.Playlist.exception.ErrorCode;
import com.example.Playlist.repository.GenreRepository;
import com.example.Playlist.repository.TrackRepository;
import com.example.Playlist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackServiceTest {

    @InjectMocks
    TrackService trackService;

    @Mock
    TrackRepository trackRepository;

    @Mock
    GenreRepository genreRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    private static ExtentReports extent;
    private ExtentTest test;

    @BeforeAll
    static void initReport() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("test-report/track-service-report.html");
        reporter.config().setDocumentTitle("Track Service Test Report");
        reporter.config().setReportName("JUnit Test Results");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Quang");
    }

    @AfterAll
    static void tearDownReport() {
        extent.flush();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        test = extent.createTest(testInfo.getDisplayName());
    }

    @Test
    @DisplayName("WTC01 - Tạo track với file âm thanh không hợp lệ")
    void WTC01_createTrack_invalid_audio_format() {
        try {
            TrackRequest request = TrackRequest.builder()
                .nameTrack("Test Track")
                .mainArtist("Test Artist")
                .build();

            MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "invalid".getBytes());

            AppException ex = assertThrows(AppException.class,
                    () -> trackService.createTrack(request, invalidFile, null));
            
            String actual = ex.getErrorCode().getMessage();
            String expected = ErrorCode.INVALID_AUDIO_FORMAT.getMessage();
            assertEquals(expected, actual, 
                String.format("Expected error message: '%s' but was: '%s'", expected, actual));

            test.pass("✅ Đã phát hiện đúng định dạng file âm thanh không hợp lệ.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC02 - Tạo track không có file âm thanh")
    void WTC02_createTrack_without_audio() {
        try {
            TrackRequest request = TrackRequest.builder()
                .nameTrack("Test Track")
                .mainArtist("Test Artist")
                .build();

            AppException ex = assertThrows(AppException.class,
                    () -> trackService.createTrack(request, null, null));
            assertEquals(ErrorCode.FILE_NOT_FOUND, ex.getErrorCode());

            test.pass("✅ Đã phát hiện đúng khi không có file âm thanh.");
        } catch (Throwable t) {
            String message = t.getMessage()
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br>");
            test.fail("❌ " + message);
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC03 - Tạo track thành công với đầy đủ thông tin")
    void WTC03_createTrack_success() {
        try {
            TrackRequest request = TrackRequest.builder()
                .nameTrack("Test Track")
                .mainArtist("Test Artist")
                .description("Test Description")
                .isPublic(true)
                .genreId(1L)
                .build();

            MockMultipartFile audioFile = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "audio".getBytes());
            MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image".getBytes());

            WebUser user = WebUser.builder()
                .email("test@example.com")
                .build();
            when(userRepository.findByEmail("test@example.com")).thenReturn(user);
            when(trackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TrackResponse response = trackService.createTrack(request, audioFile, imageFile);

            assertNotNull(response);
            assertEquals("Test Track", response.getNameTrack());
            assertEquals("Test Artist", response.getMainArtist());
            assertTrue(response.getIsPublic());

            test.pass("✅ Tạo track thành công với đầy đủ thông tin.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC04 - Cập nhật track không tồn tại")
    void WTC04_updateTrack_not_found() {
        try {
            Long trackId = 999L;
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> trackService.updateTrackAndImage(trackId, "title", "desc", "artist", null, true, null));
            assertEquals("Track không tồn tại", ex.getMessage());

            test.pass("✅ Đã phát hiện đúng track không tồn tại.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC05 - Cập nhật track không phải chủ sở hữu")
    void WTC05_updateTrack_not_owner() {
        try {
            Long trackId = 1L;
            Track track = Track.builder()
                .idTrack(trackId)
                .user(WebUser.builder().email("other@example.com").build())
                .build();

            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

            AppException ex = assertThrows(AppException.class,
                    () -> trackService.updateTrackAndImage(trackId, "title", "desc", "artist", null, true, null));
            assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());

            test.pass("✅ Đã phát hiện đúng khi không phải chủ sở hữu.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC06 - Cập nhật track thành công")
    void WTC06_updateTrack_success() {
        try {
            Long trackId = 1L;
            String title = "Updated Track";
            String description = "Updated Description";
            String mainArtist = "Updated Artist";
            boolean isPublic = true;

            Track existingTrack = Track.builder()
                .idTrack(trackId)
                .user(WebUser.builder().email("test@example.com").build())
                .build();

            when(trackRepository.findById(trackId)).thenReturn(Optional.of(existingTrack));
            when(trackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TrackResponse response = trackService.updateTrackAndImage(trackId, title, description, mainArtist, null, isPublic, null);

            assertNotNull(response);
            assertEquals(title, response.getNameTrack());
            assertEquals(description, response.getDescription());
            assertEquals(mainArtist, response.getMainArtist());
            assertEquals(isPublic, response.getIsPublic());

            test.pass("✅ Cập nhật track thành công.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC07 - Xóa track không tồn tại")
    void WTC07_deleteTrack_not_found() {
        try {
            Long trackId = 999L;
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> trackService.deleteTrack(trackId));
            assertEquals("Bài hát không tồn tại", ex.getMessage());

            test.pass("✅ Đã phát hiện đúng track không tồn tại.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC08 - Xóa track không phải chủ sở hữu")
    void WTC08_deleteTrack_not_owner() {
        try {
            Long trackId = 1L;
            Track track = Track.builder()
                .idTrack(trackId)
                .user(WebUser.builder().email("other@example.com").build())
                .build();

            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

            AppException ex = assertThrows(AppException.class,
                    () -> trackService.deleteTrack(trackId));
            assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());

            test.pass("✅ Đã phát hiện đúng khi không phải chủ sở hữu.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC09 - Xóa track thành công")
    void WTC09_deleteTrack_success() {
        try {
            Long trackId = 2L;
            Track track = Track.builder()
                .idTrack(trackId)
                .user(WebUser.builder().email("test@example.com").build())
                .build();

            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

            assertDoesNotThrow(() -> trackService.deleteTrack(trackId));
            verify(trackRepository).deleteById(trackId);

            test.pass("✅ Xóa track thành công.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }
} 