package com.gsm.model;

import com.gsm.config.JpaAuditingConfiguration;
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

/**
 * An abstract base class for entities that require auditing information.
 * By extending this class, an entity will automatically inherit fields for tracking
 * creation and modification details.
 * <p>
 * This is enabled by the {@link JpaAuditingConfiguration}.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    /**
     * The ID of the user who created this entity.
     * Set automatically on creation by JPA Auditing.
     */
    @CreatedBy
    @Column(name = "CreatedBy", nullable = false, updatable = false)
    protected Long createdBy;

    /**
     * The ID of the user who last modified this entity.
     * Set automatically on update by JPA Auditing.
     */
    @LastModifiedBy
    @Column(name = "LastModUser", nullable = false)
    private Long lastModifiedBy;

    /**
     * The timestamp when this entity was created.
     * Set automatically on creation.
     */
    @CreatedDate
    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * The timestamp when this entity was last modified.
     * Set automatically on update.
     */
    @LastModifiedDate
    @Column(name = "LastModTime", nullable = false)
    private LocalDateTime lastModifiedDate;

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}