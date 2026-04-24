package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.AuthResponse;
import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.MessageResponse;
import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.SsoLoginRequest;
import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.UserProfile;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.config.SsoProperties;
import com.sbi.branchdarpan.model.entity.User;
import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.security.JwtTokenProvider;
import com.sbi.branchdarpan.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final HrmsService hrmsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SsoProperties ssoProperties;

    public AuthResponse login(SsoLoginRequest request) {
        User user = resolveOrProvisionUser(request.ssoToken());
        return buildAuthResponse(user);
    }

    public AuthResponse callback(String ssoToken) {
        User user = resolveOrProvisionUser(ssoToken);
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfile validate(UserPrincipal principal) {
        User user = userRepository.findByPfid(principal.getPfid()).orElseThrow();
        return toProfile(user);
    }

    public MessageResponse logout() {
        return new MessageResponse("Logout handled on client side by dropping the JWT.");
    }

    public String getSsoLoginUrl() {
        return ssoProperties.loginUrl();
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtTokenProvider.generateToken(UserPrincipal.from(user));
        return new AuthResponse(token, toProfile(user));
    }

    private User resolveOrProvisionUser(String ssoToken) {
        DemoIdentity identity = parseIdentity(ssoToken);
        return userRepository.findByPfid(identity.pfid())
            .orElseGet(() -> {
                var hrms = hrmsService.fetch(identity.pfid());
                User user = User.builder()
                    .pfid(hrms.pfid())
                    .name(hrms.name())
                    .email(hrms.email())
                    .mobile(hrms.mobile())
                    .designation(hrms.designation())
                    .role(identity.role())
                    .circleCode(hrms.circleCode())
                    .circleName(hrms.circleName())
                    .aoCode(hrms.aoCode())
                    .aoName(hrms.aoName())
                    .rboCode(hrms.rboCode())
                    .rboName(hrms.rboName())
                    .branchCode(hrms.branchCode())
                    .branchName(hrms.branchName())
                    .active(true)
                    .build();
                return userRepository.save(user);
            });
    }

    private UserProfile toProfile(User user) {
        return new UserProfile(
            user.getId(),
            user.getPfid(),
            user.getName(),
            user.getRole().name(),
            user.getCircleCode(),
            user.getCircleName(),
            user.getAoCode(),
            user.getAoName(),
            user.getRboCode(),
            user.getRboName(),
            user.getBranchCode(),
            user.getBranchName()
        );
    }

    private DemoIdentity parseIdentity(String token) {
        if (token == null || token.isBlank()) {
            return new DemoIdentity("12345678", Role.BRANCH_MAKER);
        }
        String[] parts = token.split(":", 2);
        if (parts.length == 2) {
            return new DemoIdentity(parts[1], Role.valueOf(parts[0].trim().toUpperCase(Locale.ROOT)));
        }
        return new DemoIdentity(token.trim(), Role.BRANCH_MAKER);
    }

    private record DemoIdentity(String pfid, Role role) {
    }
}
