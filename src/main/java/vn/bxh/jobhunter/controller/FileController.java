package vn.bxh.jobhunter.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.bxh.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.bxh.jobhunter.service.FileService;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.FileInvalidException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")

public class FileController {
    @Value("${hao.upload-file.base-uri}")
    private String baseUri;
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    public ResponseEntity<ResUploadFileDTO> upload(@RequestParam(required = false) MultipartFile file,
                                                   @RequestParam String folder) throws URISyntaxException, IOException {
        //validate file
        if (file == null || file.isEmpty() ){
            throw new FileInvalidException("File dose not exist!");
        }
        String fileName = file.getOriginalFilename();
        // Conditional validation: for banner folder, restrict to image MIME and size < 3MB
        if ("banner".equalsIgnoreCase(folder)) {
            String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
            List<String> allowedMimes = Arrays.asList("image/jpeg", "image/png", "image/webp");
            boolean mimeOk = allowedMimes.stream().anyMatch(contentType::contains);
            if (!mimeOk) {
                throw new FileInvalidException("Invalid MIME type for banner. Only allow " + allowedMimes);
            }
            long maxBytes = 3 * 1024 * 1024; // 3MB
            if (file.getSize() > maxBytes) {
                throw new FileInvalidException("Banner image too large. Max 3MB");
            }
        } else {
            List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
            boolean isValid = allowedExtensions.stream().anyMatch(item->fileName.toLowerCase().endsWith(item));
            if(!isValid){
                throw new FileInvalidException("Invalid file extension. Only allow "+allowedExtensions.toString());
            }
            // Giới hạn kích thước file cho resume: <= 5MB
            if ("resume".equalsIgnoreCase(folder)) {
                long maxBytes = 5 * 1024 * 1024; // 5MB
                if (file.getSize() > maxBytes) {
                    throw new FileInvalidException("Resume file too large. Max 5MB");
                }
            }
        }
        //create folder if not exist
        this.fileService.createDirector(baseUri+folder);
        //save file
        String uploadFile = this.fileService.store(file,folder);
        //
        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO();
        resUploadFileDTO.setFileName(uploadFile);
        resUploadFileDTO.setUploadedAt(Instant.now());
        return ResponseEntity.ok(resUploadFileDTO);
    }

    @GetMapping("/files")
    @ApiMessage("Download a file")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder
    ) throws URISyntaxException, FileNotFoundException {

        if (fileName == null || folder == null) {
            throw new FileInvalidException("Missing required params: fileName or folder");
        }

        // Kiểm tra file có tồn tại không
        long fileLength = this.fileService.getFileLength(fileName, folder);
        if (fileLength <= 0) {
            throw new FileInvalidException("File with name = " + fileName + " not found.");
        }

        // Lấy file từ service
        InputStreamResource resource = this.fileService.getResource(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


}
