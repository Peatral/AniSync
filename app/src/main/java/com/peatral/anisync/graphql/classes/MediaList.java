package com.peatral.anisync.graphql.classes;

import com.peatral.anisync.graphql.enums.MediaListStatus;
import io.aexp.nodes.graphql.annotations.GraphQLArgument;
import io.aexp.nodes.graphql.annotations.GraphQLArguments;

public class MediaList {
    private MediaListStatus status = MediaListStatus.PLANNING;
    @GraphQLArguments({
            @GraphQLArgument(name="format")
    })
    private float score = 0f;
    private int progress = 0;
    private Media media;

    public MediaListStatus getStatus() {
        return status;
    }

    public void setStatus(MediaListStatus status) {
        this.status = status;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
