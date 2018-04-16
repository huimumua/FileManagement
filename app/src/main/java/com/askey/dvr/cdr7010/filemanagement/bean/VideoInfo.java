package com.askey.dvr.cdr7010.filemanagement.bean;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/13 17:38
 * 修改人：skysoft
 * 修改时间：2018/4/13 17:38
 * 修改备注：
 */
public class VideoInfo {

    private int ID;
    private String NAME;
    private String PATH;
    private String TYPE;
    private String SIZE;
    private String CREATE_TIME;
    private String SPEED;
    private String LANGITUDE;
    private String LATITUDE;
    private String LOCATION;
    private String IS_LOCK;
    private String IS_UPLOAD;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getPATH() {
        return PATH;
    }

    public void setPATH(String PATH) {
        this.PATH = PATH;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public String getSIZE() {
        return SIZE;
    }

    public void setSIZE(String SIZE) {
        this.SIZE = SIZE;
    }

    public String getCREATE_TIME() {
        return CREATE_TIME;
    }

    public void setCREATE_TIME(String CREATE_TIME) {
        this.CREATE_TIME = CREATE_TIME;
    }

    public String getSPEED() {
        return SPEED;
    }

    public void setSPEED(String SPEED) {
        this.SPEED = SPEED;
    }

    public String getLANGITUDE() {
        return LANGITUDE;
    }

    public void setLANGITUDE(String LANGITUDE) {
        this.LANGITUDE = LANGITUDE;
    }

    public String getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(String LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public String getLOCATION() {
        return LOCATION;
    }

    public void setLOCATION(String LOCATION) {
        this.LOCATION = LOCATION;
    }

    public String getIS_LOCK() {
        return IS_LOCK;
    }

    public void setIS_LOCK(String IS_LOCK) {
        this.IS_LOCK = IS_LOCK;
    }

    public String getIS_UPLOAD() {
        return IS_UPLOAD;
    }

    public void setIS_UPLOAD(String IS_UPLOAD) {
        this.IS_UPLOAD = IS_UPLOAD;
    }
}
