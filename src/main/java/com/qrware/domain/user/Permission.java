package com.qrware.domain.user;

import com.qrware.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Permission entity for fine-grained access control
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name"),
    @Index(name = "idx_permission_resource", columnList = "resource")
})
public class Permission extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    private String name;

    @Column(name = "description", length = 255)
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Column(name = "resource", nullable = false, length = 50)
    @NotBlank(message = "Resource is required")
    @Size(max = 50, message = "Resource must not exceed 50 characters")
    private String resource;

    @Column(name = "action", nullable = false, length = 20)
    @NotBlank(message = "Action is required")
    @Size(max = 20, message = "Action must not exceed 20 characters")
    private String action;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

    // Constructors
    public Permission() {}

    public Permission(String name, String description, String resource, String action) {
        this.name = name;
        this.description = description;
        this.resource = resource;
        this.action = action;
    }

    // Business methods
    public String getFullPermission() {
        return resource + ":" + action;
    }

    public boolean matches(String resource, String action) {
        return this.resource.equals(resource) && this.action.equals(action);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission permission = (Permission) o;
        return name != null && name.equals(permission.getName());
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", active=" + active +
                '}';
    }
}