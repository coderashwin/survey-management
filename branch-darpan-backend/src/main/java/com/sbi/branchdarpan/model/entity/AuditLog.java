package com.sbi.branchdarpan.model.entity;

import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog extends CreatedEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private AuditRequestType requestType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(name = "actor_pfid", nullable = false, length = 20)
    private String actorPfid;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", length = 30)
    private Role actorRole;

    @Column(name = "target_pfid", length = 20)
    private String targetPfid;

    @Lob
    private String remarks;
}
