package com.example.extensionblocker.service;

import java.util.List;
import com.example.extensionblocker.constrant.ExtensionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

import com.example.extensionblocker.dto.ExtensionDto;
import com.example.extensionblocker.dto.PolicyResponse;
import com.example.extensionblocker.model.ExtensionPolicy;
import com.example.extensionblocker.model.ExtensionRule;
import com.example.extensionblocker.type.ExtensionType;
import com.example.extensionblocker.mapper.ExtensionPolicyMapper;
import com.example.extensionblocker.mapper.ExtensionRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 확장자 차단 서비스 구현체
 * 정책 관리, 고정/커스텀 확장자 CRUD, 파일 검증 등의 기능 제공
 */
@Service
@RequiredArgsConstructor
public class ExtensionServiceImpl implements ExtensionService {

    private static final Logger log = LoggerFactory.getLogger(ExtensionServiceImpl.class);

    private final ExtensionPolicyMapper policyMapper;
    private final ExtensionRuleMapper ruleMapper;

    /**
     * 특정 네임스페이스의 정책을 조회하거나, 없으면 새로 생성하여 반환합니다.
     * (Lazy Initialization / On-demand)
     * 
     * @param namespace 정책 네임스페이스
     * @return 정책 객체
     */
    private ExtensionPolicy getOrCreatePolicy(String namespace) {
        return policyMapper.getPolicyByNamespace(namespace)
                .orElseGet(() -> {
                    ExtensionPolicy newPolicy = new ExtensionPolicy(namespace, namespace + " Policy");
                    policyMapper.regExtensionPolicy(newPolicy);
                    log.info("[getOrCreatePolicy] Created new policy: namespace={}, id={}", namespace,
                            newPolicy.getId());
                    return newPolicy;
                });
    }

    /**
     * 정책 조회
     * 고정 확장자와 커스텀 확장자 목록 반환
     * 
     * @param namespace 정책 네임스페이스
     * @return 고정/커스텀 확장자 목록
     */
    @Transactional
    public PolicyResponse getPolicy(String namespace) {
        Optional<ExtensionPolicy> policyOpt = policyMapper.getPolicyByNamespace(namespace);
        List<ExtensionRule> rules = policyOpt.isPresent()
                ? ruleMapper.getRulesByPolicyId(policyOpt.get().getId())
                : java.util.Collections.emptyList();

        // 고정 확장자 처리
        // DB에 있으면 차단(isActive=true), 없으면 허용(isActive=false)
        List<ExtensionDto> fixed = ExtensionConst.DEFAULT_FIXED_EXTENSIONS.stream()
                .map(ext -> {
                    Optional<ExtensionRule> match = rules.stream()
                            .filter(r -> r.getType() == ExtensionType.FIXED && r.getExtension().equals(ext))
                            .findFirst();

                    if (match.isPresent()) {
                        return new ExtensionDto(match.get().getId(), ext, true);
                    } else {
                        return new ExtensionDto(null, ext, false);
                    }
                })
                .collect(Collectors.toList());

        // 커스텀 확장자 목록 생성 (DB에 있는 것만)
        List<ExtensionDto> custom = rules.stream()
                .filter(r -> r.getType() == ExtensionType.CUSTOM)
                .map(r -> new ExtensionDto(r.getId(), r.getExtension(), true))
                .collect(Collectors.toList());

        return new PolicyResponse(fixed, custom);
    }

    @Override
    public void regExtensionRule(String namespace, ExtensionType type, String rawExtension) {
        ExtensionPolicy policy = getOrCreatePolicy(namespace);
        log.debug("[regExtensionRule] Found policy id={}", policy.getId());

        // 1. Normalize
        String extension = normalize(rawExtension);

        // 2. Validate Check
        if (type == ExtensionType.CUSTOM) {
            if (extension.length() > ExtensionConst.MAX_EXTENSION_LENGTH) {
                throw new IllegalArgumentException(
                        "Extension length cannot exceed " + ExtensionConst.MAX_EXTENSION_LENGTH + " characters");
            }
            if (!ExtensionConst.VALID_EXTENSION_PATTERN.matcher(extension).matches()) {
                throw new IllegalArgumentException("Invalid extension format (only a-z, 0-9 allowed)");
            }

            // Check if max limit reached
            long customCount = ruleMapper.getCountByPolicyIdAndType(policy.getId(), ExtensionType.CUSTOM);
            if (customCount >= ExtensionConst.MAX_CUSTOM_EXTENSIONS) {
                throw new IllegalArgumentException(
                        "Max custom extensions limit (" + ExtensionConst.MAX_CUSTOM_EXTENSIONS + ") reached");
            }
        }

        // Check DB for duplicates (Fixed or Custom)
        if (ruleMapper.getRuleByPolicyIdAndExtension(policy.getId(), extension).isPresent()) {
            throw new IllegalArgumentException("Extension already exists");
        }

        // Save new rule
        ExtensionRule newRule = new ExtensionRule(policy.getId(), extension, type);
        int result = ruleMapper.regExtensionRule(newRule);
        if (result != 1) {
            throw new RuntimeException("Failed to save extension");
        }
        log.info("[regExtensionRule] SUCCESS - Saved extension={} with id={}", extension, newRule.getId());
    }

    @Override
    public void delExtensionRule(Long id) {
        ruleMapper.delExtensionRuleById(id);
        log.info("[delExtensionRule] SUCCESS - Deleted extension={}", id);
    }

    @Transactional(readOnly = true)
    public boolean isFileAllowed(String filename, String namespace) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // Extract extension
        String extension = extractExtension(filename);
        if (extension.isEmpty()) {
            return true; // No extension = allowed
        }

        // Get policy
        ExtensionPolicy policy = policyMapper.getPolicyByNamespace(namespace).orElse(null);
        if (policy == null) {
            return true; // No policy = allowed
        }

        // Check if extension is actively blocked (Presence in DB = Blocked)
        boolean isBlocked = ruleMapper.getRuleByPolicyIdAndExtension(policy.getId(), extension).isPresent();

        return !isBlocked; // Blocked면 false(Not Allowed), 아니면 true(Allowed)
    }

    private String normalize(String input) {
        if (input == null)
            return "";
        String clean = input.trim().toLowerCase();
        if (clean.startsWith(".")) {
            clean = clean.substring(1);
        }
        return clean;
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

}
