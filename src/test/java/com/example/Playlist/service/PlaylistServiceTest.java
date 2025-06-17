package com.example.Playlist.service;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.example.Playlist.dto.request.PlaylistRequest;
import com.example.Playlist.dto.response.PlaylistResponse;
import com.example.Playlist.entity.Playlist;
import com.example.Playlist.entity.Track;
import com.example.Playlist.entity.WebUser;
import com.example.Playlist.repository.PlaylistRepository;
import com.example.Playlist.repository.TrackRepository;
import com.example.Playlist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaylistServiceTest {

    @InjectMocks
    PlaylistService playlistService;

    @Mock
    PlaylistRepository playlistRepository;

    @Mock
    TrackRepository trackRepository;

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
        ExtentSparkReporter reporter = new ExtentSparkReporter("test-report/playlist-report.html");
        reporter.config().setDocumentTitle("Playlist Service Test Report");
        reporter.config().setReportName("JUnit Test Results");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Phương");
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

        String UPLOAD_IMAGE_DIR = System.getProperty("user.dir") + "/uploads/test-images/";
        PlaylistService.UPLOAD_IMAGE_DIR = UPLOAD_IMAGE_DIR;
        File uploadImageFolder = new File(UPLOAD_IMAGE_DIR);
        if (!uploadImageFolder.exists()) {
            uploadImageFolder.mkdirs();
            System.out.println("Created upload folder: " + UPLOAD_IMAGE_DIR);
        }
        else System.out.println("Folder existed: " + UPLOAD_IMAGE_DIR);
        test = extent.createTest(testInfo.getDisplayName());
    }

    @Test
    @DisplayName("Tạo playlist hợp lệ, người dùng tồn tại, có ảnh và bài hát tồn tại")
    void WTC01_createPlaylist_valid_with_image_and_tracks() {
        try {
            PlaylistRequest request = new PlaylistRequest("My Playlist", "My Description", List.of(1L, 2L));
            MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "img".getBytes());

            WebUser user = new WebUser();
            user.setEmail("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(user);

            Track t1 = new Track(); t1.setIdTrack(1L);
            Track t2 = new Track(); t2.setIdTrack(2L);
            when(trackRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(t1, t2));
            when(playlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PlaylistResponse response = playlistService.createPlaylist(request, image);

            assertNotNull(response);
            assertEquals("My Playlist", response.getName());
            assertEquals(2, response.getTracks().size());

            test.pass("Tạo playlist thành công với ảnh và bài hát hợp lệ.");
        } catch (Throwable t) {
            test.fail(" Test thất bại: Thông tin hợp lệ nhưng không lưu được" );
            fail(t);
        }
    }

    @Test
    @DisplayName("Tạo playlist không có ảnh")
    void WTC02_createPlaylist_without_image() {
        try {
            PlaylistRequest request = new PlaylistRequest("No Img", "No Desc", List.of(1L, 2L));

            WebUser user = new WebUser(); user.setEmail("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(user);

            Track t1 = new Track(); t1.setIdTrack(1L);
            Track t2 = new Track(); t2.setIdTrack(2L);
            when(trackRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(t1, t2));
            when(playlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PlaylistResponse response = playlistService.createPlaylist(request, null);

            assertNotNull(response);
            assertNull(response.getImageName());

            test.pass("Tạo playlist không có ảnh thành công.");
        } catch (Throwable t) {
            test.fail(" Test thất bại: Người dùng gửi thông tin hợp lệ nhưng không lưu được " );
            fail(t);
        }
    }

    @Test
    @DisplayName("Tạo playlist không có track")
    void WTC03_createPlaylist_without_tracks() {
        try {
            PlaylistRequest request = new PlaylistRequest("No Track", "Empty", new ArrayList<>());

            WebUser user = new WebUser(); user.setEmail("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(user);
            when(playlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PlaylistResponse response = playlistService.createPlaylist(request, null);

            assertNotNull(response);
            assertEquals(0, response.getTracks().size());

            test.pass("Tạo playlist không có track thành công.");
        } catch (Throwable t) {
            test.fail(" Test thất bại: Người dùng gửi thông tin hợp lệ nhưng không lưu được" );
            fail(t);
        }
    }

    @Test
    @DisplayName("Tạo playlist với track không tồn tại")
    void WTC04_createPlaylist_with_nonexistent_track_should_throw() {
        try {
            PlaylistRequest request = new PlaylistRequest("Fail Track", "Desc", List.of(999L));

            WebUser user = new WebUser(); user.setEmail("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(user);
            when(trackRepository.findAllById(List.of(999L))).thenReturn(List.of());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> playlistService.createPlaylist(request, null));
            assertEquals("Một hoặc nhiều track không tồn tại", ex.getMessage());

            test.pass("Đã phát hiện đúng track không tồn tại.");
        } catch (Throwable t) {
            test.fail(" Test thất bại: Không thông báo 'Một hoặc nhiều bài hát không tồn tại' khi đưa vào bài hát không tồn tại cho người dùng" );
            fail(t);
        }
    }

    @Test
    @DisplayName("Lỗi IO khi lưu ảnh")
    void WTC05_createPlaylist_with_imageIOException_should_throw() {
        try {
            PlaylistRequest request = new PlaylistRequest("IO Err", "Desc", null);

            WebUser user = new WebUser(); user.setEmail("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(user);

            MockMultipartFile image = mock(MockMultipartFile.class);
            when(image.isEmpty()).thenReturn(false);
            when(image.getOriginalFilename()).thenReturn("error.jpg");
            when(image.getInputStream()).thenThrow(new IOException("Fake IO"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> playlistService.createPlaylist(request, image));
            assertEquals("Lỗi khi lưu ảnh", ex.getMessage());

            test.pass("Đã xử lý đúng lỗi IOException khi lưu ảnh.");
        } catch (Throwable t) {
            test.fail(" Test thất bại: ảnh lỗi nhưng ko đưa ra lỗi 'Lỗi khi lưu ảnh' cho người dùng " );
            fail(t);
        }
    }

    @Test
    @DisplayName("Người dùng chưa đăng nhập")
    void WTC06_createPlaylist_without_user_should_throw() {
        try {
            when(authentication.getName()).thenReturn(null);
            when(userRepository.findByEmail(null)).thenReturn(null);

            PlaylistRequest request = new PlaylistRequest("Null User", "Desc", null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> playlistService.createPlaylist(request, null));

            assertEquals("Người dùng chưa đăng nhập", ex.getMessage());

            test.pass("Đã phát hiện lỗi người dùng chưa đăng nhập. Không thể tạo playlist khi chưa đăng nhập");
        } catch (Throwable t) {
            test.fail(" Test thất bại: " + "Người dùng chưa đăng nhập nhưng không báo lỗi chưa đăng nhập " );
            fail(t);
        }
    }

    @Test
    @DisplayName("Xóa playlist thành công khi là chủ sở hữu")
    void WTC07_deletePlaylist_with_permission_should_succeed() {
        try {
            WebUser user = new WebUser(); user.setEmail("test@example.com");
            Playlist playlist = new Playlist(); playlist.setId(1L); playlist.setUser(user);

            when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
            when(authentication.getName()).thenReturn("test@example.com");

            assertDoesNotThrow(() -> playlistService.deletePlaylist(1L));
            verify(playlistRepository).delete(playlist);

            test.pass("Xóa playlist thành công khi người dùng là chủ sở hữu.");
        } catch (Throwable t) {
            test.fail("Test thất bại: " +"Chủ playlist nhưng không xóa được playlist của mình " );
            fail(t);
        }
    }

    @Test
    @DisplayName("Không thể xóa nếu không phải chủ sở hữu")
    void WTC08_deletePlaylist_without_permission_should_throw() {
        try {
            WebUser owner = new WebUser(); owner.setEmail("owner@example.com");
            Playlist playlist = new Playlist(); playlist.setId(2L); playlist.setUser(owner);

            when(playlistRepository.findById(2L)).thenReturn(Optional.of(playlist));
            when(authentication.getName()).thenReturn("test@example.com");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> playlistService.deletePlaylist(2L));
            assertEquals("Bạn không có quyền xóa playlist này", ex.getMessage());
            verify(playlistRepository, never()).delete(any());

            test.pass("Không cho phép xóa playlist khi không phải chủ sở hữu. Thông tin chủ sở hữu là: email" 
                        + owner.getEmail()
                        + ".Thông tin của bạn là: email" + "test@example.com"
                        );
        } catch (Throwable t) {
            test.fail("Test thất bại: Người dùng hiện tại 'test@example.com' Không phải chủ playlist nhưng vẫn có thể xóa được playlist của người dùng: owner@example.com "  );
            fail(t);
        }
    }
    @Test
    @DisplayName("Không tìm thấy playlist để xóa")
    void WTC09_deletePlaylist_not_found_should_throw() {
        try {
            when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> playlistService.deletePlaylist(99L));

            assertEquals("Không tìm thấy playlist", ex.getMessage());
            test.pass("Đã xử lý đúng khi không tìm thấy playlist để xóa.");
        } catch (Throwable t) {
            test.fail("Playlist không tồn tại nhưng không thông báo lỗi cho người dùng ");
            fail(t);
        }
    }


}
