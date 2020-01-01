package com.peatral.anisync.graphql;

import com.peatral.anisync.lib.AnimeEntry;

public interface SyncListener {
    void message(String text, int id);
    void animeSynced(AnimeEntry anime);
}
