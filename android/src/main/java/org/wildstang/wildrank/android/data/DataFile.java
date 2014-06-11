package org.wildstang.wildrank.android.data;

public abstract class DataFile {

    private Type type;
    private String content;

    public enum Type {
        MATCH,
        PIT,
        TEAM_PICTURE
    }

    public DataFile(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public abstract String getRelativeFile();
}
