package com.peatral.anisync.graphql.enums;

public enum MediaListStatus {
    COMPLETED,
    CURRENT,
    DROPPED,
    PAUSED,
    PLANNING,
    REPEATING;

    public static MediaListStatus fromJikanId(int id){
        switch (id) {
            case 1:
                return CURRENT;
            case 2:
                return COMPLETED;
            case 3:
                return PAUSED;
            case 4:
                return DROPPED;
            case 6:
                return PLANNING;
            default:
                return PLANNING;
        }
    }

    public static String getMalClass(MediaListStatus status) {
        switch (status) {
            case CURRENT:
                return "watching";
            case COMPLETED:
                return "completed";
            case PAUSED:
                return "onhold";
            case DROPPED:
                return "dropped";
            case PLANNING:
                return "plantowatch";
            default:
                return "";
        }
    }
}
