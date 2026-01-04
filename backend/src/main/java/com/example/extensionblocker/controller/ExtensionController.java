package com.example.extensionblocker.controller;

import com.example.extensionblocker.dto.ExtensionRequest;
import com.example.extensionblocker.dto.PolicyResponse;
import com.example.extensionblocker.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 확장자 차단 정책 관리 컨트롤러
 * 정책 조회, 고정/커스텀 확장자 추가/삭제 API 제공
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발 환경을 위한 CORS 허용
public class ExtensionController {

    private final ExtensionService extensionService;

    /**
     * 특정 네임스페이스의 정책 조회
     * 
     * @param namespace 정책 네임스페이스 (예: "chat", "work")
     * @return 고정 확장자 및 커스텀 확장자 목록
     */
    @GetMapping("/policies/{namespace}")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable String namespace) {
        return ResponseEntity.ok(extensionService.getPolicy(namespace));
    }

    /**
     * 고정 확장자의 차단 상태 토글
     * 
     * @param namespace 정책 네임스페이스
     * @param extension 토글할 확장자명
     * @return 성공 응답
     */
    @PostMapping("/policies/{namespace}/fixed/{extension}/toggle")
    public ResponseEntity<Void> toggleFixed(@PathVariable String namespace, @PathVariable String extension) {
        extensionService.toggleFixed(namespace, extension);
        return ResponseEntity.ok().build();
    }

    /**
     * 커스텀 확장자 추가
     * 
     * @param namespace 정책 네임스페이스
     * @param request   추가할 확장자 정보
     * @return 성공 응답
     */
    @PostMapping("/policies/{namespace}/custom")
    public ResponseEntity<Void> addCustom(@PathVariable String namespace, @RequestBody ExtensionRequest request) {
        extensionService.addCustom(namespace, request.getExtension());
        return ResponseEntity.ok().build();
    }

    /**
     * 커스텀 확장자 삭제
     * 
     * @param id 삭제할 규칙 ID
     * @return 성공 응답
     */
    @DeleteMapping("/extensions/{id}")
    public ResponseEntity<Void> deleteCustom(@PathVariable Long id) {
        extensionService.deleteCustom(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 잘못된 요청 예외 처리
     * 비즈니스 로직 검증 실패 시 400 오류 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
