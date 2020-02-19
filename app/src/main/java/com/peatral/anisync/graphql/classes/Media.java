package com.peatral.anisync.graphql.classes;

import io.aexp.nodes.graphql.annotations.GraphQLArgument;
import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name="Media", arguments={ @GraphQLArgument(name = "idMal"), @GraphQLArgument(name = "type")})
public class Media {

    private int id = -1;
    private Title title;
    private int idMal = -1;
    private MediaCoverImage coverImage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public int getIdMal() {
        return idMal;
    }

    public void setIdMal(int idMal) {
        this.idMal = idMal;
    }

    public MediaCoverImage getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(MediaCoverImage coverImage) {
        this.coverImage = coverImage;
    }
}
