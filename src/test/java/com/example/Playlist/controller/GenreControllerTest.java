package com.example.Playlist.controller;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.example.Playlist.dto.request.GenreRequest;
import com.example.Playlist.dto.response.GenreResponse;
import com.example.Playlist.service.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenreControllerTest {

    private static ExtentReports extent;
    private ExtentTest test;

    @BeforeAll
    static void initReport() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("test-report/genre-report.html");
        reporter.config().setDocumentTitle("Genre Test Report");
        reporter.config().setReportName("JUnit Test Results");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Tan");
    }

    @AfterAll
    static void tearDownReport() {
        extent.flush();
    }

    @BeforeEach
    void setupEach(TestInfo testInfo) {
        test = extent.createTest(testInfo.getDisplayName());
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URL = "/api/v1/genres";
    private static final String VALID_JWT = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMkBnbWFpbC5jb20iLCJpYXQiOjE3NTAxNDU1MDcsImV4cCI6MTc1MDc1MDMwN30.L5YP9b4WJzfDoKQs8xUR3JpUX4c09H2XFPk2fmBBpvB9ibaSrEFNK6KlUM8rS5XTCyHHPJ6oJRJ5q_E9480bvA";

    private GenreRequest validGenreRequest;
    private GenreResponse validGenreResponse;

    @BeforeEach
    void setUp() {
        validGenreRequest = new GenreRequest();
        validGenreRequest.setName("Pop");
        validGenreRequest.setDescription("Pop music genre");
        validGenreRequest.setIsActive(true);

        validGenreResponse = new GenreResponse();
        validGenreResponse.setId(1L);
        validGenreResponse.setName("Pop");
        validGenreResponse.setDescription("Pop music genre");
        validGenreResponse.setIsActive(true);
    }

    // TC40: Cập nhật thành công
    @Test
    @DisplayName("Genre Edit - Cập nhật thành công -> 200 OK")
    void TC40_editGenre_success_should_return_200() throws Exception {
        try {
            when(genreService.updateGenre(anyLong(), any(GenreRequest.class), any()))
                .thenReturn(validGenreResponse);

            MockMultipartFile genreFile = new MockMultipartFile(
                "genre",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(validGenreRequest).getBytes()
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL + "/1")
                            .file(genreFile)
                            .header("Authorization", VALID_JWT)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Cập nhật thể loại thành công"));
            test.pass("TC40 - Cập nhật thành công trả về 200 OK");
        } catch (AssertionError | Exception e) {
            test.fail("TC40 - Thất bại: " + e.getMessage());
            throw e;
        }
    }

    // TC41: Không có token
    @Test
    @DisplayName("Genre Edit - Không có token -> 403 Unauthorized")
    void TC41_editGenre_without_token_should_return_401() throws Exception {
        try {
            MockMultipartFile genreFile = new MockMultipartFile(
                "genre",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(validGenreRequest).getBytes()
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL + "/1")
                            .file(genreFile)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden: Invalid or missing token"));
            test.pass("TC41 - Không có token trả về 403 như mong đợi");
        } catch (AssertionError | Exception e) {
            test.fail("TC41 - Thất bại: " + e.getMessage());
            throw e;
        }
    }

    // TC42: Tên thể loại rỗng
    @Test
    @DisplayName("Genre Edit - Tên thể loại rỗng -> 400 Bad Request")
    void TC42_editGenre_empty_name_should_return_400() throws Exception {
        try {
            validGenreRequest = new GenreRequest();
            validGenreRequest.setName("");
            validGenreRequest.setDescription("Pop music genre");
            validGenreRequest.setIsActive(true);

            MockMultipartFile genreFile = new MockMultipartFile(
                "genre",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(validGenreRequest).getBytes()
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL + "/1")
                            .file(genreFile)
                            .header("Authorization", VALID_JWT)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Name is required"));
            test.pass("TC42 - Tên thể loại rỗng trả về 400 như mong đợi");
        } catch (AssertionError | Exception e) {
            test.fail("TC42 - Thất bại: " + e.getMessage());
            throw e;
        }
    }

    // TC43: Xóa thành công
    @Test
    @DisplayName("Genre Delete - Xóa thành công -> 200 OK")
    void TC43_deleteGenre_success_should_return_200() throws Exception {
        try {
            doNothing().when(genreService).deleteGenre(anyLong());

            mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/1")
                            .header("Authorization", VALID_JWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Xóa thể loại thành công"));
            test.pass("TC43 - Xóa thành công trả về 200 OK");
        } catch (AssertionError | Exception e) {
            test.fail("TC43 - Thất bại: " + e.getMessage());
            throw e;
        }
    }

    // TC44: Không có token
    @Test
    @DisplayName("Genre Delete - Không có token -> 401/403 Unauthorized")
    void TC44_deleteGenre_without_token_should_return_401() throws Exception {
        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden: Invalid or missing token"));
            test.pass("TC44 - Không có token trả về 403 như mong đợi");
        } catch (AssertionError | Exception e) {
            test.fail("TC44 - Thất bại: " + e.getMessage());
            throw e;
        }
    }

    // TC45: Genre không tồn tại
    @Test
    @DisplayName("Genre Delete - Genre không tồn tại -> 404 Not Found")
    void TC45_deleteGenre_not_found_should_return_404() throws Exception {
        try {
            doThrow(new RuntimeException("Genre not found"))
                .when(genreService).deleteGenre(anyLong());

            mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/999")
                            .header("Authorization", VALID_JWT))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Genre not found"));
            test.pass("TC45 - Genre không tồn tại trả về 404 Not Found");
        } catch (AssertionError | Exception e) {
            test.fail("TC45 - Thất bại: " + e.getMessage());
            throw e;
        }
    }
}