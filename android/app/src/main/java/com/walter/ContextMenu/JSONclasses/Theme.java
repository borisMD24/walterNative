package com.walter.json;

public class Theme {
    public String name;
    public int id;
    public String img;
    public int roomID;
    
    public Theme(String name, int id, String img, int roomID) {
        this.name = name;
        this.id = id;
        this.img = img;
        this.roomID = roomID;
    }
    
    // Getters
    public String getName() { return name; }
    public int getId() { return id; }
    public String getImg() { return img; }
    
    @Override
    public String toString() {
        return "Theme{name='" + name + "', id=" + id + ", img='" + img + "'}";
    }
}