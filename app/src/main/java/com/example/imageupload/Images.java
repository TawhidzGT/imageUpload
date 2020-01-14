package com.example.imageupload;

import com.google.gson.annotations.SerializedName;

public class Images {


    @SerializedName("data")
    String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }




}
