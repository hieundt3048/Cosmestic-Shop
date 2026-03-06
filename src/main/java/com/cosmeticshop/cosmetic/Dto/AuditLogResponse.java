package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String actor;
    private String role;
    private String action;
    private String target;
    private String details;
    private LocalDateTime at;

    public AuditLogResponse() {
    }

    public AuditLogResponse(Long id, String actor, String role, String action, String target, String details, LocalDateTime at) {
        this.id = id;
        this.actor = actor;
        this.role = role;
        this.action = action;
        this.target = target;
        this.details = details;
        this.at = at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getAt() {
        return at;
    }

    public void setAt(LocalDateTime at) {
        this.at = at;
    }
}
