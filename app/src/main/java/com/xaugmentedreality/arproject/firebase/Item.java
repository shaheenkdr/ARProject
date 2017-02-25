
package com.xaugmentedreality.arproject.firebase;


import com.google.firebase.database.PropertyName;





public class Item {

    private String desc;
    private Boolean isvideo;
    private Boolean isdeleted;
    private String title;
    private String uid;
    private Integer updated;
    private String urlApp;
    private String urlImage;


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getIsvideo() {
        return isvideo;
    }

    public void setIsvideo(Boolean isvideo) {
        this.isvideo = isvideo;
    }

    public Boolean getIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(Boolean isdeleted) {
        this.isdeleted = isdeleted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getUpdated() {
        return updated;
    }

    public void setUpdated(Integer updated) {
        this.updated = updated;
    }

    public String getUrlApp() {
        return urlApp;
    }

    public void setUrlApp(String urlApp) {
        this.urlApp = urlApp;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }


}