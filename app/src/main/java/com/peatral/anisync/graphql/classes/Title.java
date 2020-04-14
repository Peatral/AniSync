package com.peatral.anisync.graphql.classes;

import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name = "Title")
public class Title {
    @GraphQLProperty(name="romaji")
    private String romajiTitle;
    @GraphQLProperty(name="english")
    private String englishTitle;
    @GraphQLProperty(name="native")
    private String nativeTitle;

    public String getRomajiTitle() {
        return romajiTitle;
    }

    public void setRomajiTitle(String romajiTitle) {
        this.romajiTitle = romajiTitle;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public String getNativeTitle() {
        return nativeTitle;
    }

    public void setNativeTitle(String nativeTitle) {
        this.nativeTitle = nativeTitle;
    }

    public boolean equalsString(String title) {
        return (romajiTitle != null && romajiTitle.equals(title)) ||
                (englishTitle != null && englishTitle.equals(title)) ||
                (nativeTitle != null && nativeTitle.equals(title));
    }
}