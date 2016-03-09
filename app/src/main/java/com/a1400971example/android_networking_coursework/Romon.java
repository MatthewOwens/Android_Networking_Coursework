package com.a1400971example.android_networking_coursework;

/**
 * Created by Matthew Owens on 06/03/16.
 * Class to represent a romon
 */
public class Romon {
    private String name;
    private String nickname;
    private int drawableResource;

    public Romon(String name, String nickname, int drawableResource)
    {
        this.name = name;
        this.nickname = nickname;
        this.drawableResource = drawableResource;
    }

    public String getName() {return name;}
    public String getNickname() {return nickname;}
    public int getDrawableResource() {return drawableResource;}
}
