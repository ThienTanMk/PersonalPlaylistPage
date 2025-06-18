package com.example.Playlist.service;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.example.Playlist.dto.request.GenreRequest;
import com.example.Playlist.dto.response.GenreResponse;
import com.example.Playlist.entity.Genre;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.exception.AppException;
import com.example.Playlist.exception.ErrorCode;
import com.example.Playlist.repository.GenreRepository;
import com.example.Playlist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreServiceTest {

    @InjectMocks
    GenreService genreService;

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
        ExtentSparkReporter reporter = new ExtentSparkReporter("test-report/genre-service-report.html");
        reporter.config().setDocumentTitle("Genre Service Test Report");
        reporter.config().setReportName("JUnit Test Results");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Tấn");
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
    @DisplayName("WTC01 - Cập nhật genre thành công")
    void WTC01_updateGenre_success() {
        try {
            Long genreId = 1L;
            GenreRequest request = GenreRequest.builder()
                .name("Pop")
                .description("Pop music genre")
                .isActive(true)
                .build();

            Genre existingGenre = Genre.builder()
                .id(genreId)
                .user(WebUser.builder().email("test@example.com").build())
                .build();

            when(genreRepository.findById(genreId)).thenReturn(Optional.of(existingGenre));
            when(genreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            GenreResponse response = genreService.updateGenre(genreId, request, null);

            assertNotNull(response);
            assertEquals("Pop", response.getName());
            assertEquals("Pop music genre", response.getDescription());
            assertTrue(response.getIsActive());

            test.pass("✅ Cập nhật genre thành công.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC02 - Cập nhật genre không tồn tại")
    void WTC02_updateGenre_not_found() {
        try {
            Long genreId = 999L;
            GenreRequest request = GenreRequest.builder()
                .name("Pop")
                .description("Pop music genre")
                .isActive(true)
                .build();

            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> genreService.updateGenre(genreId, request, null));
            assertEquals("Genre not found", ex.getMessage());

            test.pass("✅ Đã phát hiện đúng genre không tồn tại.");
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
    @DisplayName("WTC03 - Cập nhật genre không phải chủ sở hữu")
    void WTC03_updateGenre_not_owner() {
        try {
            Long genreId = 1L;
            GenreRequest request = GenreRequest.builder()
                .name("Pop")
                .description("Pop music genre")
                .isActive(true)
                .build();

            Genre existingGenre = Genre.builder()
                .id(genreId)
                .user(WebUser.builder().email("other@example.com").build())
                .build();

            when(genreRepository.findById(genreId)).thenReturn(Optional.of(existingGenre));

            AppException ex = assertThrows(AppException.class,
                    () -> genreService.updateGenre(genreId, request, null));
            assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());

            test.pass("✅ Đã phát hiện đúng khi không phải chủ sở hữu.");
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
    @DisplayName("WTC04 - Xóa genre thành công")
    void WTC04_deleteGenre_success() {
        try {
            Long genreId = 1L;
            Genre genre = Genre.builder()
                    .id(genreId).name("genre1")
                    .isActive(true)
                    .user(WebUser.builder().email("test@example.com").build())
                    .build();

            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            assertDoesNotThrow(() -> genreService.deleteGenre(genreId));

            // ✅ Kiểm tra trạng thái
            assertFalse(genre.isActive());

            test.pass("✅ Xóa genre thành công.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC05 - Xóa genre không tồn tại")
    void WTC05_deleteGenre_not_found() {
        try {
            Long genreId = 999L;
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> genreService.deleteGenre(genreId));
            assertEquals("Genre not found", ex.getMessage());

            test.pass("✅ Đã phát hiện đúng genre không tồn tại.");
        } catch (Throwable t) {
            test.fail("❌ Test failed: " + t.getMessage());
            fail(t);
        }
    }

    @Test
    @DisplayName("WTC06 - Xóa genre không phải chủ sở hữu")
    void WTC06_deleteGenre_not_owner() {
        try {
            Long genreId = 1L;
            Genre genre = Genre.builder()
                .id(genreId)
                .user(WebUser.builder().email("other@example.com").build())
                .build();

            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            AppException ex = assertThrows(AppException.class,
                    () -> genreService.deleteGenre(genreId));
            assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());

            test.pass("✅ Đã phát hiện đúng khi không phải chủ sở hữu.");
        } catch (Throwable t) {
            String message = t.getMessage()
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br>");
            test.fail("❌ " + message);
            fail(t);
        }
    }
} 