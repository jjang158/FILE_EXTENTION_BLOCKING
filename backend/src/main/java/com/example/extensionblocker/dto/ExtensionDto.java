package com.example.extensionblocker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtensionDto {
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
