package com.sbi.branchdarpan.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sbi.branchdarpan.model.entity.User;
import com.sbi.branchdarpan.model.enums.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String pfid;
    private final String displayName;
    private final Role role;
    private final String circleCode;
    private final String aoCode;
    private final String rboCode;
    private final String branchCode;
    private final String branchName;

    public static UserPrincipal from(User user) {
        return UserPrincipal.builder()
            .id(user.getId())
            .pfid(user.getPfid())
            .displayName(user.getName())
            .role(user.getRole())
            .circleCode(user.getCircleCode())
            .aoCode(user.getAoCode())
            .rboCode(user.getRboCode())
            .branchCode(user.getBranchCode())
            .branchName(user.getBranchName())
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return pfid;
    }
}
