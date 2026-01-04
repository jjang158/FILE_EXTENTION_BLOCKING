package com.example.extensionblocker.controller;

import com.example.extensionblocker.dto.FileValidationRequest;
import com.example.extensionblocker.dto.FileValidationResponse;
import com.example.extensionblocker.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 파일 검증 컨트롤러
 * 파일 업로드 전 확장자 차단 여부를 확인하는 API 제공
 */
@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ValidateController {

    private static final Logger log = LoggerFactory.getLogger(ValidateController.class);
    private final ExtensionService extensionService;

    /**
     * 파일 업로드 가능 여부 검증
     * 
     * @param request 파일명과 네임스페이스를 포함한 요청
     * @return 허용/차단 여부 및 사유
     */
    @PostMapping("/file")
    public ResponseEntity<FileValidationResponse> validateFile(@RequestBody FileValidationRequest request) {
        log.info("[validateFile] Validating file: {}, namespace: {}", request.getFilename(), request.getNamespace());

        // 확장자 추출
        String extension = extractExtension(request.getFilename());

        // 업로드 허용 여부 확인
        boolean allowed = extensionService.isFileAllowed(request.getFilename(), request.getNamespace());

        // 응답 생성
        FileValidationResponse response = new FileValidationResponse();
        response.setAllowed(allowed);
        response.setExtension(extension);

        if (allowed) {
            response.setReason("허용된 파일입니다");
        } else {
            response.setReason("차단된 확장자입니다: ." + extension);
        }

        log.info("[validateFile] Result: allowed={}, extension={}", allowed, extension);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일명에서 확장자를 추출
     * 
     * @param filename 파일명 (예: "test.exe")
     * @return 확장자 (점 제외, 소문자, 예: "exe")
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
