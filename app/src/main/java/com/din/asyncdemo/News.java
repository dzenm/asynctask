package com.din.asyncdemo;

public class News {
    private int id;
    private String pic;
    private String title;

    public News(int id, String pic, String title) {
        this.id = id;
        this.pic = pic;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}