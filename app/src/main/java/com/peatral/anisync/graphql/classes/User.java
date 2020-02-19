package com.peatral.anisync.graphql.classes;

import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name = "User")
public class User {
    private UserAvatar avatar;
    private String bannerImage;
    private String donatorBadge;
    private int donatorTier;
    private int id;
    private String moderatorStatus;
    private String name;
    private String siteUrl;
    private int unreadNotificationCount;
    private int updatedAt;

    public UserAvatar getAvatar() {
        return avatar;
    }

    public void setAvatar(UserAvatar avatar) {
        this.avatar = avatar;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String getDonatorBadge() {
        return donatorBadge;
    }

    public void setDonatorBadge(String donatorBadge) {
        this.donatorBadge = donatorBadge;
    }

    public int getDonatorTier() {
        return donatorTier;
    }

    public void setDonatorTier(int donatorTier) {
        this.donatorTier = donatorTier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModeratorStatus() {
        return moderatorStatus;
    }

    public void setModeratorStatus(String moderatorStatus) {
        this.moderatorStatus = moderatorStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public int getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(int unreadNotificationCount) {
        this.unreadNotificationCount = unreadNotificationCount;
    }

    public int getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(int updatedAt) {
        this.updatedAt = updatedAt;
    }
}
