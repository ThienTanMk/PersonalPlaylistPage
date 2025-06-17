package com.example.Playlist.service;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.example.Playlist.controller.TrackController;
import com.example.Playlist.dto.response.TrackResponse;
import com.example.Playlist.dto.response.GenreResponse;
import com.example.Playlist.exception.AppException;
import com.example.Playlist.exception.ErrorCode;
import com.example.Playlist.service.TrackService;
import com.example.Playlist.configuration.JwtTokenProvider;
import com.example.Playlist.configuration.Security;
import com.example.Playlist.configuration.JwtAuthenticationFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebMvcTest(TrackController.class)
@Import(Security.class)
class TrackControllerTest {
    private static ExtentReports extent;
    private ExtentTest test;
    @Configuration
    class TestConfig {
        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        private UserDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .cors()
                    .and()
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/users/register", "/api/v1/users/login", "/api/v1/tracks/images/**", "/api/v1/tracks/audios/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .sessionManagement(sess -> sess
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .addFilterBefore(testJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }

        @Bean
        public JwtAuthenticationFilter testJwtAuthenticationFilter() {
            return new TestJwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        }
    }
    @BeforeAll
    static void initReport() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("test-report/track-report.html");
        reporter.config().setDocumentTitle("Track Test Report");
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
    void setupEach(TestInfo testInfo) {
        test = extent.createTest(testInfo.getDisplayName());
    }
    class TestJwtAuthenticationFilter extends JwtAuthenticationFilter {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserDetailsService userDetailsService;

        public TestJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
            this.jwtTokenProvider = jwtTokenProvider;
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String path = request.getRequestURI();
            if (path.equals("/api/v1/users/register") || path.equals("/api/v1/users/login") ||
                    path.contains("/api/v1/tracks/images/") || path.contains("/api/v1/tracks/audios/")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = request.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            jwt = jwt.substring(7);
            if (!jwtTokenProvider.validateToken(jwt)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String username = jwtTokenProvider.getUserNameFromJWT(jwt);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (user != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrackService trackService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        // Mock JwtTokenProvider behavior
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUserNameFromJWT(anyString())).thenReturn("user@gmail.com");

        // Mock UserDetailsService behavior
        UserDetails userDetails = User.builder()
                .username("user@gmail.com")
                .password("useruser")
                .authorities("user")
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    }

    private final String URL = "/api/v1/tracks";
    private final String VALID_JWT = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMkBnbWFpbC5jb20iLCJpYXQiOjE3NTAxNDU1MDcsImV4cCI6MTc1MDc1MDMwN30.L5YP9b4WJzfDoKQs8xUR3JpUX4c09H2XFPk2fmBBpvB9ibaSrEFNK6KlUM8rS5XTCyHHPJ6oJRJ5q_E9480bvA";

    // TC01: Người dùng chưa đăng nhập (không có JWT)
    @Test
    @DisplayName("Track Creation - Không đăng nhập -> 403 Forbidden")
    void TC01_createTrack_without_auth_should_return_403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", "music".getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart(URL)
                        .file(file)
                        .file(image)
                        .param("name", "Test Track")
                        .param("mainArtist", "Artist"))
                .andDo(print())
                .andExpect(status().isForbidden());
        test.pass("TC01 - Request không có JWT trả về 403 như mong đợi");
    }

    // TC02: Thiếu name -> 400
    @Test
    @DisplayName("Track Creation - Thiếu name -> 400 Bad Request")
    void TC02_createTrack_missing_name_should_return_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", "music".getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart(URL)
                        .file(file)
                        .file(image)
                        .param("mainArtist", "Artist")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name is required"));
        test.pass("TC02 - Thiếu name trả về 400 như mong đợi");
    }

    // TC03: Không có ảnh -> OK
    @Test
    @DisplayName("Track Creation - Không có ảnh -> 200 OK")
    void TC03_createTrack_without_image_should_return_200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", "music".getBytes());

        when(trackService.createTrack(any(), any(), any())).thenReturn(
                new TrackResponse()
        );

        mockMvc.perform(multipart(URL)
                        .file(file)
                        .param("name", "Test Track")
                        .param("mainArtist", "Artist")
                        .header("Authorization", VALID_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạo bài hát thành công"));
        test.pass("TC03 - Không có ảnh nhưng vẫn trả về 200 OK");
    }

    // TC04: Thiếu file âm thanh -> 400
    @Test
    @DisplayName("Track Creation - Thiếu file âm thanh -> 400 Bad Request")
    void TC04_createTrack_without_file_should_return_400() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart(URL)
                        .file(image)
                        .param("name", "Track")
                        .param("mainArtist", "Artist")
                        .header("Authorization", VALID_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("file is required"));
        test.pass("TC04 - Không có file âm thanh -> 400 như mong đợi");
    }

    // TC05: File âm thanh sai định dạng -> 400
    @Test
    @DisplayName("Track Creation - File âm thanh sai định dạng -> 400 Bad Request")
    void TC05_createTrack_invalid_audio_format_should_return_400() throws Exception {
        try {
            MockMultipartFile file = new MockMultipartFile("file", "song.txt", "text/plain", "invalid".getBytes());
            MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", "img".getBytes());

            mockMvc.perform(multipart(URL)
                            .file(file)
                            .file(image)
                            .param("name", "Test Track")
                            .param("mainArtist", "Artist")
                            .header("Authorization", VALID_JWT))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid audio file format"));

            test.pass("TC05 - Định dạng âm thanh sai -> 400");
        } catch (AssertionError | Exception e) {
            test.fail("TC05 - Thất bại: " + e.getMessage());
            throw e;
        }
    }


    // TC06: Request hợp lệ đầy đủ thông tin -> 200
    @Test
    @DisplayName("Track Creation - Request hợp lệ đầy đủ thông tin -> 200 OK")
    void TC06_createTrack_valid_request_should_return_200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", "music".getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", "img".getBytes());

        when(trackService.createTrack(any(), any(), any())).thenReturn(
                new TrackResponse()
        );

        mockMvc.perform(multipart(URL)
                        .file(file)
                        .file(image)
                        .param("name", "Test Track")
                        .param("mainArtist", "Artist")
                        .header("Authorization", VALID_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạo bài hát thành công"));
        test.pass("TC06 - Request đầy đủ -> Tạo thành công (200 OK)");
    }

    // TC07: Người chưa đăng nhập
    @Test
    @DisplayName("Track Edit - Người chưa đăng nhập -> 403 Forbidden")
    void TC07_editTrack_without_auth_should_return_403() throws Exception {
        mockMvc.perform(put(URL + "/15")
                        .param("title", "Updated Track")
                        .param("description", "Updated description")
                        .param("mainArtist", "Updated Artist")
                        .param("isPublic", "true"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden: Invalid or missing token"));
        test.pass("TC07 - Request không có JWT trả về 403 như mong đợi");
    }

    // TC08: Người đăng nhập nhưng không phải chủ sở hữu
    @Test
    @DisplayName("Track Edit - Không phải chủ sở hữu -> 403 Forbidden")
    void TC08_editTrack_not_owner_should_return_403() throws Exception {
        when(trackService.updateTrackAndImage(anyLong(), anyString(), anyString(), anyString(), any(), anyBoolean(), any()))
                .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(put(URL + "/15")
                        .param("title", "Updated Track")
                        .param("description", "Updated description")
                        .param("mainArtist", "Updated Artist")
                        .param("isPublic", "true")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You dont have permission"));
        test.pass("TC08 - Không phải chủ sở hữu trả về 403 như mong đợi");
    }

    // TC09: Thiếu trường title
    @Test
    @DisplayName("Track Edit - Thiếu title -> 400 Bad Request")
    void TC09_editTrack_missing_title_should_return_400() throws Exception {
        mockMvc.perform(put(URL + "/2")
                        .param("description", "Updated description")
                        .param("mainArtist", "Updated Artist")
                        .param("isPublic", "true")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title is required"));
        test.pass("TC09 - Thiếu title trả về 400 như mong đợi");
    }

    // TC10: Thiếu description
    @Test
    @DisplayName("Track Edit - Thiếu description -> 200 OK")
    void TC10_editTrack_missing_description_should_return_200() throws Exception {
        when(trackService.updateTrackAndImage(anyLong(), anyString(), anyString(), anyString(), any(), anyBoolean(), any()))
                .thenReturn(new TrackResponse());

        mockMvc.perform(put(URL + "/2")
                        .param("title", "Updated Track")
                        .param("mainArtist", "Updated Artist")
                        .param("isPublic", "true")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật bài hát thành công"));
        test.pass("TC10 - Thiếu description vẫn cập nhật thành công");
    }

    // TC11: Cập nhật thành công
    @Test
    @DisplayName("Track Edit - Cập nhật thành công -> 200 OK")
    void TC11_editTrack_success_should_return_200() throws Exception {
        when(trackService.updateTrackAndImage(anyLong(), anyString(), anyString(), anyString(), any(), anyBoolean(), any()))
                .thenReturn(new TrackResponse());

        mockMvc.perform(put(URL + "/2")
                        .param("title", "Updated Track")
                        .param("description", "Updated description")
                        .param("mainArtist", "Updated Artist")
                        .param("isPublic", "true")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật bài hát thành công"));
        test.pass("TC11 - Cập nhật thành công với đầy đủ thông tin");
    }

    // TC12: Người chưa đăng nhập
    @Test
    @DisplayName("Track Delete - Người chưa đăng nhập -> 403 Forbidden")
    void TC12_deleteTrack_without_auth_should_return_403() throws Exception {
        mockMvc.perform(delete(URL + "/15"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden: Invalid or missing token"));
        test.pass("TC12 - Request không có JWT trả về 403 như mong đợi");
    }

    // TC13: Người đăng nhập nhưng không phải chủ sở hữu
    @Test
    @DisplayName("Track Delete - Không phải chủ sở hữu -> 403 Forbidden")
    void TC13_deleteTrack_not_owner_should_return_403() throws Exception {
        doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(trackService).deleteTrack(anyLong());

        mockMvc.perform(delete(URL + "/15")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You dont have permission"));
        test.pass("TC13 - Không phải chủ sở hữu trả về 403 như mong đợi");
    }

    // TC14: Xóa thành công
    @Test
    @DisplayName("Track Delete - Xóa thành công -> 200 OK")
    void TC14_deleteTrack_success_should_return_200() throws Exception {
        doNothing().when(trackService).deleteTrack(anyLong());

        mockMvc.perform(delete(URL + "/15")
                        .header("Authorization", VALID_JWT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Xóa bài hát thành công"));
        test.pass("TC14 - Xóa thành công trả về 200 OK");
    }

    // TC15: Track không tồn tại
    @Test
    @DisplayName("Track Delete - Track không tồn tại -> 404 Not Found")
    void TC15_deleteTrack_not_found_should_return_404() throws Exception {
        try {
            doThrow(new RuntimeException("Bài hát không tồn tại"))
                    .when(trackService).deleteTrack(anyLong());

            mockMvc.perform(delete(URL + "/15")
                            .header("Authorization", VALID_JWT))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Bài hát không tồn tại"));

            test.pass("TC15 - Track không tồn tại trả về 404 Not Found");
        } catch (AssertionError | Exception e) {
            test.fail("TC15 - Thất bại với lỗi: " + e.getMessage());
            throw e; // để JUnit đánh dấu là thất bại
        }
    }

}
