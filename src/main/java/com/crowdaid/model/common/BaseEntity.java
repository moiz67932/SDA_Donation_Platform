package com.crowdaid.model.common;

import java.time.LocalDateTime;

/**
 * Base Entity class providing common fields for all domain entities.
 * Contains id and timestamp tracking fields.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public abstract class BaseEntity {
    
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    
    /**
     * Default constructor initializing timestamps to current time.
     */
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with ID.
     * 
     * @param id the entity ID
     */
    public BaseEntity(Long id) {
        this();
        this.id = id;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Updates the updatedAt timestamp to current time.
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
