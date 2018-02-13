package v2015.oasis.pilani.bits.com.home.selfiecontest;


import java.util.ArrayList;

public class Selfie {
    public String caption;
    public String description;
    public String thumbnailUrl;
    public String fullImageUrl;
    public Long noOfLikes;
    public ArrayList<String> likes;
    public String selfieID;


    public Selfie() {
    }

    public Selfie(String caption, String description, String thumbnailUrl, String fullImageUrl, Long noOfLikes, ArrayList<String> likes, String selfieID) {
        this.caption = caption;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.fullImageUrl = fullImageUrl;
        this.noOfLikes = noOfLikes;
        this.likes = likes;
        this.selfieID = selfieID;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFullImageUrl() {
        return fullImageUrl;
    }

    public void setFullImageUrl(String fullImageUrl) {
        this.fullImageUrl = fullImageUrl;
    }

    public Long getNoOfLikes() {
        return noOfLikes;
    }

    public void setNoOfLikes(Long noOfLikes) {
        this.noOfLikes = noOfLikes;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public String getSelfieID() {
        return selfieID;
    }

    public void setSelfieID(String selfieID) {
        this.selfieID = selfieID;
    }
}
