
package com.xaugmentedreality.arproject.firebase;


import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "desc",
        "isvideo",
        "isdeleted",
        "title",
        "uid",
        "updated",
        "url_app",
        "url_image"
})
public class Item {

    @JsonProperty("desc")
    private String desc;
    @JsonProperty("isvideo")
    private Boolean isvideo;
    @JsonProperty("isdeleted")
    private Boolean isdeleted;
    @JsonProperty("title")
    private String title;
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("updated")
    private Integer updated;
    @JsonProperty("url_app")
    private String urlApp;
    @JsonProperty("url_image")
    private String urlImage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("desc")
    public String getDesc() {
        return desc;
    }

    @JsonProperty("desc")
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @JsonProperty("isvideo")
    public Boolean getIsvideo() {
        return isvideo;
    }

    @JsonProperty("isvideo")
    public void setIsvideo(Boolean isvideo) {
        this.isvideo = isvideo;
    }

    @JsonProperty("isdeleted")
    public Boolean getIsdeleted() {
        return isdeleted;
    }

    @JsonProperty("isdeleted")
    public void setIsdeleted(Boolean isdeleted) {
        this.isdeleted = isdeleted;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("uid")
    public String getUid() {
        return uid;
    }

    @JsonProperty("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @JsonProperty("updated")
    public Integer getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(Integer updated) {
        this.updated = updated;
    }

    @JsonProperty("url_app")
    public String getUrlApp() {
        return urlApp;
    }

    @JsonProperty("url_app")
    public void setUrlApp(String urlApp) {
        this.urlApp = urlApp;
    }

    @JsonProperty("url_image")
    public String getUrlImage() {
        return urlImage;
    }

    @JsonProperty("url_image")
    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}