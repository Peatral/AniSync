package com.peatral.anisync.graphql.classes;

import com.peatral.anisync.graphql.enums.MediaListStatus;
import io.aexp.nodes.graphql.annotations.GraphQLArgument;
import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name = "SaveMediaListEntry", arguments = {@GraphQLArgument(name = "mediaId"), @GraphQLArgument(name = "status"), @GraphQLArgument(name = "progress"), @GraphQLArgument(name = "score")})
public class SaveMediaListEntry {
    private MediaListStatus status;
    private int progress;
    private float score;
    private int mediaId;

    public MediaListStatus getStatus() {
        return status;
    }

    public void setStatus(MediaListStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }
}
