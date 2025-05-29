package com.walter.json;

public class Room {
    public String name;
    public int id;
    public String img;
    
    public Room(String name, int id, String img) {
        this.name = name;
        this.id = id;
        this.img = img;
    }
    
    // Getters
    public String getName() { return name; }
    public int getId() { return id; }
    public String getImg() { return img; }
    
    @Override
    public String toString() {
        return "Room{name='" + name + "', id=" + id + ", img='" + img + "'}";
    }
}