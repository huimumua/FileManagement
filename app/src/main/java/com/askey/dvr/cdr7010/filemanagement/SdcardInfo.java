package com.askey.dvr.cdr7010.filemanagement;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/17 16:39
 * 修改人：skysoft
 * 修改时间：2018/4/17 16:39
 * 修改备注：
 */
public class SdcardInfo {

    private String totalSize;
    private String normalSize;
    private String normalCurrentSize;
    private String eventSize;
    private String eventCurrentSize;
    private String parkingSize;
    private String parkingCurrentSize;
    private String pictureSize;
    private String pictureCurrentSize;

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getNormalSize() {
        return normalSize;
    }

    public void setNormalSize(String normalSize) {
        this.normalSize = normalSize;
    }

    public String getNormalCurrentSize() {
        return normalCurrentSize;
    }

    public void setNormalCurrentSize(String normalCurrentSize) {
        this.normalCurrentSize = normalCurrentSize;
    }

    public String getEventSize() {
        return eventSize;
    }

    public void setEventSize(String eventSize) {
        this.eventSize = eventSize;
    }

    public String getEventCurrentSize() {
        return eventCurrentSize;
    }

    public void setEventCurrentSize(String eventCurrentSize) {
        this.eventCurrentSize = eventCurrentSize;
    }

    public String getParkingSize() {
        return parkingSize;
    }

    public void setParkingSize(String parkingSize) {
        this.parkingSize = parkingSize;
    }

    public String getParkingCurrentSize() {
        return parkingCurrentSize;
    }

    public void setParkingCurrentSize(String parkingCurrentSize) {
        this.parkingCurrentSize = parkingCurrentSize;
    }

    public String getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(String pictureSize) {
        this.pictureSize = pictureSize;
    }

    public String getPictureCurrentSize() {
        return pictureCurrentSize;
    }

    public void setPictureCurrentSize(String pictureCurrentSize) {
        this.pictureCurrentSize = pictureCurrentSize;
    }
}
