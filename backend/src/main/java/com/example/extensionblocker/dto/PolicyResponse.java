package com.example.extensionblocker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 정책 조회 응답 DTO
 * 고정 확장자와 커스텀 확장자 목록을 클라이언트에 반환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {

    /**
     * 고정 확장자 목록
     */
    private List<FixedExtensionDto> fixed;

    /**
     * 커스텀 확장자 목록
     */
    private List<CustomExtensionDto> custom;

    /**
     * 고정 확장자 DTO
     */
    @Data
    @AllArgsConstructor
    public static class FixedExtensionDto {
        /**
         * 확장자명
         */
        private String name;

        /**
         * 체크(차단) 여부
         */
        private boolean isChecked;
    }

    /**
     * 커스텀 확장자 DTO
     */
    @Data
    @AllArgsConstructor
    public static class CustomExtensionDto {
        /**
         * 규칙 ID
         */
        private Long id;

        /**
         * 확장자명
         */
        private String name;

        /**
         * 활성화(차단) 여부
         */
        private boolean isActive;
    }
}
