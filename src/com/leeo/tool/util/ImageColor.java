package com.leeo.tool.util;

/**
 * Created by leeo on 12/23/14.
 */
public class ImageColor {

    private int color1 = -1;

    private int color2 = -1;

    public int getColor1() {
        return color1;
    }

    public void setColor1(int color1) {
        this.color1 = color1;
    }

    public int getColor2() {
        return color2;
    }

    public void setColor2(int color2) {
        this.color2 = color2;
    }

    @Override
    public String toString() {
        return "ImageColor[color1: " + printColor(color1) +
                " color2: " + printColor(color2) +
                "]";
    }

    private String printColor(int color){
        int  red   = (color & 0x00ff0000) >> 16;  //取高两位
        int  green = (color & 0x0000ff00) >> 8; //取中两位
        int  blue  =  color & 0x000000ff; //取低两位
        return red + " " + green + " " + blue;
    }

}
