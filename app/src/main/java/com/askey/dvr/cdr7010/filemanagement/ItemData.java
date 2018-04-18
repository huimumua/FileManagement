package com.askey.dvr.cdr7010.filemanagement;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/4/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/

public class ItemData implements Parcelable {
    private int itemTextID;
    private boolean isDir;
    private String filePath;
    private String fileName;
    private long fileTime;
    private List<ItemData> dirFileItem = new ArrayList<>();

    public ItemData() { }

    public ItemData(int itemTextID) {
        this.itemTextID = itemTextID;
    }

    public ItemData(String fileName) {
        this.fileName = fileName;
    }

    public int getItemTextID() {
        return itemTextID;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileTime() {
        return fileTime;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    public List<ItemData> getDirFileItem() {
        return dirFileItem;
    }

    public void setDirFileItem(List<ItemData> dirFileItem) {
        this.dirFileItem = dirFileItem;
    }

    /**
     * Parcelable
     */
    private ItemData(Parcel in) {
        itemTextID = in.readInt();
        isDir = in.readByte() != 0; //isDir == true if byte != 0
        filePath = in.readString();
        fileName = in.readString();
        fileTime = in.readLong();

        List<ItemData> parts = new ArrayList<>();
        in.readTypedList(parts, ItemData.CREATOR);
        dirFileItem = parts;
    }

    public static final Creator<ItemData> CREATOR = new Creator<ItemData>() {
        @Override
        public ItemData createFromParcel(Parcel in) {
            return new ItemData(in);
        }

        @Override
        public ItemData[] newArray(int size) {
            return new ItemData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemTextID);
        dest.writeByte((byte) (isDir ? 1 : 0)); //if isDir == true, byte == 1
        dest.writeString(filePath);
        dest.writeString(fileName);
        dest.writeLong(fileTime);
        dest.writeTypedList(dirFileItem);
    }
}
