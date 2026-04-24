package com.sbi.branchdarpan.util;

import java.util.List;
import java.util.Map;

import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.security.UserPrincipal;

public final class RoleHierarchyUtil {

    private RoleHierarchyUtil() {
    }

    private static final Map<Role, RoleMapping> HIERARCHY = Map.of(
        Role.SUPER_ADMIN, new RoleMapping(List.of(Role.CC_MAKER, Role.CC_CHECKER), null),
        Role.CC_MAKER, new RoleMapping(List.of(Role.CIRCLE_MAKER, Role.CIRCLE_CHECKER), Role.CC_CHECKER),
        Role.CIRCLE_MAKER, new RoleMapping(List.of(Role.AO_MAKER, Role.AO_CHECKER), Role.CIRCLE_CHECKER),
        Role.AO_MAKER, new RoleMapping(List.of(Role.RBO_MAKER, Role.RBO_CHECKER), Role.AO_CHECKER),
        Role.RBO_MAKER, new RoleMapping(List.of(Role.BRANCH_MAKER, Role.BRANCH_CHECKER), Role.RBO_CHECKER)
    );

    public static List<Role> getAllowedRolesForCreation(Role makerRole) {
        if (makerRole == Role.BRANCH_CHECKER) {
            return List.of(Role.BRANCH_MAKER);
        }
        RoleMapping mapping = HIERARCHY.get(makerRole);
        return mapping == null ? List.of() : mapping.creatableRoles();
    }

    public static Role getCheckerRole(Role makerRole, Role requestedRole) {
        if (makerRole == Role.BRANCH_CHECKER && requestedRole == Role.BRANCH_MAKER) {
            return Role.RBO_CHECKER;
        }
        RoleMapping mapping = HIERARCHY.get(makerRole);
        return mapping == null ? null : mapping.approverRole();
    }

    public static boolean canAccessBranch(UserPrincipal principal, String branchCode) {
        if (principal == null || branchCode == null) {
            return false;
        }
        return switch (principal.getRole()) {
            case SUPER_ADMIN, CC_MAKER, CC_CHECKER, CIRCLE_MAKER, CIRCLE_CHECKER,
                AO_MAKER, AO_CHECKER, RBO_MAKER, RBO_CHECKER -> true;
            case BRANCH_MAKER, BRANCH_CHECKER -> branchCode.equals(principal.getBranchCode());
        };
    }

    private record RoleMapping(List<Role> creatableRoles, Role approverRole) {
    }
}
