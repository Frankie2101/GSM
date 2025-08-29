package com.gsm.model;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    // Sửa lại name="CreatedBy" để khớp với Database
    @CreatedBy
    @Column(name = "CreatedBy", nullable = false, updatable = false)
    private Long createdBy;

    // Sửa lại name="LastModifiedBy" (DB của bạn là LastModUser)
    @LastModifiedBy
    @Column(name = "LastModUser", nullable = false)
    private Long lastModifiedBy;

    // Sửa lại name="CreatedDate" (DB của bạn là CreateDate)
    @CreatedDate
    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // Sửa lại name="LastModifiedDate" (DB của bạn là LastModTime)
    @LastModifiedDate
    @Column(name = "LastModTime", nullable = false)
    private LocalDateTime lastModifiedDate;
}