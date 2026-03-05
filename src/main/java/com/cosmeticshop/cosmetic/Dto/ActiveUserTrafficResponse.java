package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class ActiveUserTrafficResponse {

    private Integer activeUsers;
    private Integer trackedUsers;
    private Integer activeWindowMinutes;
    private LocalDateTime serverTime;

    public ActiveUserTrafficResponse(
            Integer activeUsers,
            Integer trackedUsers,
            Integer activeWindowMinutes,
            LocalDateTime serverTime) {
        this.activeUsers = activeUsers;
        this.trackedUsers = trackedUsers;
        this.activeWindowMinutes = activeWindowMinutes;
        this.serverTime = serverTime;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Integer getTrackedUsers() {
        return trackedUsers;
    }

    public void setTrackedUsers(Integer trackedUsers) {
        this.trackedUsers = trackedUsers;
    }

    public Integer getActiveWindowMinutes() {
        return activeWindowMinutes;
    }

    public void setActiveWindowMinutes(Integer activeWindowMinutes) {
        this.activeWindowMinutes = activeWindowMinutes;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }
}
