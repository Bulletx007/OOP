package model;

import java.util.ArrayList;
import java.util.List;

public class ViewState {
    public static final int ST_START = 0;
    public static final int ST_PLAY = 1;
    public static final int ST_GAMEOVER = 2;

    public int status = ST_START;
    public double time = 0;
    public boolean suddenDeath = false;
    public int myId = 1;
    public List<CarView> cars = new ArrayList<>();
    public List<MeteorView> meteors = new ArrayList<>();

    public int aliveCount() {
        int n = 0;
        for (CarView c : cars) if (c.alive) n++;
        return n;
    }
    
    public CarView find(int id) {
        for (CarView c : cars) if (c.id == id) return c;
        return null;
    }
    
    public void reset() {
        time = 0;
        suddenDeath = false;
        meteors.clear();
        for (CarView c : cars) {
            c.alive = true;
            c.dodge = 0;
        }
    }
}