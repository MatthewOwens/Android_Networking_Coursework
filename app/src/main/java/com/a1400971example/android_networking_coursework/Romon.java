package com.a1400971example.android_networking_coursework;

/**
 * Created by Matthew Owens on 06/03/16.
 * Class to represent a romon
 */
public class Romon {
    private String name;
    private String nickname;
    private String drawableName;

    public Romon(String name, String nickname, String drawableName)
    {
        this.name = name;
        this.nickname = nickname;
        this.drawableName = drawableName;
    }

    public String getName() {return name;}
    public String getNickname() {return nickname;}
    public String getDrawableName(){return drawableName;}
}
