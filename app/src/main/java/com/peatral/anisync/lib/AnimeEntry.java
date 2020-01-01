package com.peatral.anisync.lib;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnimeEntry implements Serializable {
    public String name = "";
    public int id = -1;
    public int malId = -1;
    public int score = 0;
    public int progress = 0;
    public String image_url = "";

    public boolean synced = false;

    public ListStatus status = ListStatus.PLAN_TO_WATCH;

    @Override
    public String toString() {
        String s = name + " (" + id + "): " + score + ", " + progress + ", " + status.name;
        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        AnimeEntry entry = (AnimeEntry) obj;
        return entry.progress == progress && entry.status.ordinal() == status.ordinal() && entry.score == score && entry.malId == malId;
    }

    public static List<AnimeEntry> findDifference(List<AnimeEntry> l1, List<AnimeEntry> l2){
        List<AnimeEntry> difference = new ArrayList<>();
        for(AnimeEntry entryMal : l1){
            boolean found = false;
            for(AnimeEntry entryAnilist : l2){
                if(entryMal.equals(entryAnilist)) found = true;
            }
            if(!found) difference.add(entryMal);
        }
        return difference;
    }

    public String getIdPreferenceKey() {
        return "MalID_" + malId;
    }

}
