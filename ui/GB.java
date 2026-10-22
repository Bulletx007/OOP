package ui;

import java.awt.Color;

public final class GB {
    public static final Color DARKEST  = new Color(0x0F380F);
    public static final Color DARK     = new Color(0x306230);
    public static final Color LIGHT    = new Color(0x8BAC0F);
    public static final Color LIGHTEST = new Color(0x9BBC0F);

    public static final int W = 160, H = 144, SCALE = 4;

    public static final int LANES     = 5;
    public static final int ROAD_X    = 15;
    public static final int ROAD_W    = 130;
    public static final int LANE_W    = ROAD_W / LANES;
    public static final int TRACK_TOP = 12;
    public static final int TRACK_BOT = 131;
    public static final int CAR_Y     = 108;
    public static final int CAR_W = 16, CAR_H = 20;
    public static final int METEOR_SZ = 12;
    public static final int HIT_W = 6;



    public static int laneCenterX(int lane) {
        return ROAD_X + lane * LANE_W + LANE_W / 2;
    }
    private GB() {}
}