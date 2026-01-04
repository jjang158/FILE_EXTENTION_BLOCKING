package com.example.extensionblocker.constrant;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 확장자 차단 관련 상수 정의
 */
public class ExtensionConst {

    /**
     * 기본 고정 확장자 목록
     * 보안상 위험한 실행 파일 확장자들
     */
    public static final List<String> DEFAULT_FIXED_EXTENSIONS = Arrays.asList(
            "bat", "cmd", "com", "cpl", "exe", "scr", "js");

    /**
     * 유효한 확장자 형식 패턴 (영문 소문자, 숫자만 허용)
     */
    public static final Pattern VALID_EXTENSION_PATTERN = Pattern.compile("^[a-z0-9]+$");

    /**
     * 최대 커스텀 확장자 개수
     */
    public static final int MAX_CUSTOM_EXTENSIONS = 200;

    /**
     * 최대 확장자 길이 (문자 수)
     */
    public static final int MAX_EXTENSION_LENGTH = 20;
}
