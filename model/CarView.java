package model;

public class CarView {
    public int id;
    public String name;
    public int lane;          // 0-4 เป็น int เท่านั้น
    public boolean alive = true;
    public int dodge = 0;

    public CarView(int id, String name, int lane) {
        this.id = id; this.name = name; this.lane = lane;
    }
}