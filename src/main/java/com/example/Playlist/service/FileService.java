package com.example.Playlist.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    private static final String UPLOAD_IMAGE_DIR = "uploads/images/";

    public String saveImage(MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                // Tạo thư mục nếu chưa tồn tại
                Path uploadPath = Paths.get(UPLOAD_IMAGE_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Đổi tên file theo thời gian
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String originalFilename = image.getOriginalFilename();
                String newFileName = timestamp + "_" + originalFilename;

                // Lưu file
                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Trả về đường dẫn tương đối (để lưu trong DB hoặc trả ra client)
                return newFileName;
            }
        } catch (Exception e) {
            e.printStackTrace(); // ghi log lỗi
        }

        return null; // Trả về null nếu thất bại
    }
}
