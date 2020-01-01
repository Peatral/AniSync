package com.peatral.anisync.lib;

import com.peatral.anisync.type.MediaListStatus;

public enum ListStatus {
    WATCHING("watching", "watching", "CURRENT", MediaListStatus.CURRENT, 1),
    ON_HOLD("on-hold", "onhold", "PAUSED", MediaListStatus.PAUSED, 3),
    DROPPED("dropped", "dropped", "DROPPED", MediaListStatus.DROPPED, 4),
    COMPLETED("completed", "completed", "COMPLETED", MediaListStatus.COMPLETED, 2),
    PLAN_TO_WATCH("plan to watch", "plantowatch", "PLANNING", MediaListStatus.PLANNING, 6);


    /*

    1: Watching
    2: Completed
    3: On-Hold
    4: Dropped
    6: PTW

     */

    String name;
    String malClass;
    String aniEquivalent;
    MediaListStatus mediaListStatus;
    int jikanId;

    ListStatus(String name, String malClass, String aniEquivalent, MediaListStatus mediaListStatus, int jikanId) {
        this.name = name;
        this.malClass = malClass;
        this.aniEquivalent = aniEquivalent;
        this.mediaListStatus = mediaListStatus;
        this.jikanId = jikanId;
    }

    public String getName() {
        return name;
    }

    public String getMalClass() {
        return malClass;
    }

    public String getAniEquivalent() {
        return aniEquivalent;
    }

    public MediaListStatus getMediaListStatus() {
        return mediaListStatus;
    }

    public int getJikanId() {
        return jikanId;
    }

    public static ListStatus fromMalClass(String s){
        for(ListStatus l : values()){
            if(l.malClass.equals(s)){
                return l;
            }
        }
        return PLAN_TO_WATCH;
    }

    public static ListStatus fromAnilist(String ani){
        for(ListStatus l : values()){
            if(l.aniEquivalent == ani){
                return l;
            }
        }
        return PLAN_TO_WATCH;
    }

    public static ListStatus fromJikanId(int id){
        for(ListStatus l : values()){
            if(l.jikanId == id){
                return l;
            }
        }
        return PLAN_TO_WATCH;
    }
}
