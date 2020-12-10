package com.teno.teno.models;

public class post {
    private String imgUrl,cap,postId;
    private int like;
    private boolean isVerified;

    public post(String imgUrl, String cap, boolean isVerified, int like,String postId) {
        this.imgUrl = imgUrl;
        this.cap = cap;
        this.isVerified = isVerified;
        this.like = like;
        this.postId=postId;
    }
    public post(){

    }
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
    @Override
    public String toString() {
        return "post{" +
                "imgUrl='" + imgUrl + '\'' +
                ", cap='" + cap + '\'' +
                ", isVerified='" + isVerified + '\'' +
                ", like='" + like + '\'' +
                ", postId='" + postId + '\'' +

                '}';
    }
}
