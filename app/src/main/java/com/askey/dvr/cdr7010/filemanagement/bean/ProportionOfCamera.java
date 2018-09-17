package com.askey.dvr.cdr7010.filemanagement.bean;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/9/17.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public enum ProportionOfCamera {
    e_five_to_five("e_five_to_five", 1), e_six_to_four("e_six_to_four", 2), e_seven_to_three("e_seven_to_three", 3), e_ten_to_zero("e_ten_to_zero", 4);

    private String name;
    private int index;

    ProportionOfCamera(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName(){
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static ProportionOfCamera valueOfIndex(int index){
        for (ProportionOfCamera playbackFileType : ProportionOfCamera.values()) {
            if (playbackFileType.getIndex() == index) {
                return playbackFileType;
            }
        }
        return null;
    }


}
