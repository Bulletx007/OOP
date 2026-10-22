package ui;

import model.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;

public class GamePanel extends RetroPanel {

    private ViewState state = new ViewState();
    private int tick = 0;
    
    private Preferences prefs;
    private int highScore = 0;

    public GamePanel() {
        prefs = Preferences.userNodeForPackage(GamePanel.class);
        highScore = prefs.getInt("HIGH_SCORE", 0);

        state.myId = 1;
        state.cars.add(new CarView(1, "YOU",  2));
        state.cars.add(new CarView(2, "NAPAT", 4));
        state.cars.add(new CarView(3, "MINT",  0));
        state.cars.add(new CarView(4, "BOSS",  3));

        // Game Loop (30 FPS)
        new Timer(33, e -> {
            if (state.status == ViewState.ST_PLAY) {
                tick++;
                state.time += 0.033;

                // ระบบความยากตามเวลา
                double difficultyMultiplier = 1.0 + (state.time / 20.0);
                double currentSpeed = 1.2 * difficultyMultiplier;
                if (currentSpeed > 3.5) currentSpeed = 3.5;

                for (MeteorView m : state.meteors) {
                    m.y += currentSpeed;
                    m.frame = (tick / 6) % 4;

                    if (m.y > GB.TRACK_BOT) {
                        m.y = GB.TRACK_TOP;
                        m.lane = (int) (Math.random() * GB.LANES);
                        for (CarView c : state.cars) {
                            if (c.alive) c.dodge++;
                        }
                    }

                    // เช็กการชน
                    for (CarView c : state.cars) {
                        if (!c.alive) continue;
                        boolean sameLane = (m.lane == c.lane);
                        boolean hitY = (m.y >= GB.CAR_Y && m.y <= GB.CAR_Y + GB.CAR_H);

                        if (sameLane && hitY) {
                            c.alive = false;
                        }
                    }
                }

                // AI คู่แข่งสุ่มขยับเลนเล่น ๆ ให้ดูมีชีวิตชีวา
                if (tick % 45 == 0) {
                    for (CarView c : state.cars) {
                        if (c.id != state.myId && c.alive) {
                            int dir = (Math.random() > 0.5) ? 1 : -1;
                            c.lane = Math.max(0, Math.min(GB.LANES - 1, c.lane + dir));
                        }
                    }
                }

                // เช็กเงื่อนไขจบเกม (ถ้าผู้เล่นตาย)
                CarView me = state.find(state.myId);
                if (me != null && !me.alive) {
                    state.status = ViewState.ST_GAMEOVER;
                    
                    if (me.dodge > highScore) {
                        highScore = me.dodge;
                        prefs.putInt("HIGH_SCORE", highScore);
                    }
                }
            }
            repaint();
        }).start();

        // ควบคุมปุ่มกดตามสถานะเกม
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (state.status == ViewState.ST_START) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        state.reset();
                        state.meteors.add(new MeteorView(0, 30));
                        state.meteors.add(new MeteorView(2, 70));
                        state.meteors.add(new MeteorView(4, 50));
                        state.status = ViewState.ST_PLAY;
                    }
                } else if (state.status == ViewState.ST_PLAY) {
                    CarView me = state.find(state.myId);
                    if (me == null || !me.alive) return;
                    if (e.getKeyCode() == KeyEvent.VK_LEFT)  me.lane = Math.max(0, me.lane - 1);
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) me.lane = Math.min(GB.LANES - 1, me.lane + 1);
                } else if (state.status == ViewState.ST_GAMEOVER) {
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        state.status = ViewState.ST_START;
                    }
                }
                repaint();
            }
        });
    }

    @Override
    protected void drawPixels(Graphics2D g) {
        if (state.status == ViewState.ST_START) {
            drawStartScreen(g);
        } else if (state.status == ViewState.ST_PLAY) {
            drawRoad(g);
            drawMeteors(g);
            drawCars(g);
            drawTopHud(g);
            drawBottomHud(g);
        } else if (state.status == ViewState.ST_GAMEOVER) {
            drawRoad(g);
            drawGameOverScreen(g);
        }
    }

    private void drawStartScreen(Graphics2D g) {
        g.setColor(GB.DARKEST);
        g.fillRect(0, 0, GB.W, GB.H);

        textCenter(g, "PIXEL METEOR", 35, GB.LIGHTEST);
        textCenter(g, "F1 SURVIVAL", 47, GB.LIGHT);

        textCenter(g, "HIGH SCORE: " + highScore, 68, GB.LIGHTEST);

        g.setColor(GB.DARK);
        g.drawRect(20, 80, GB.W - 40, 32);
        textCenter(g, "PRESS [ENTER]", 93, GB.LIGHTEST);
        textCenter(g, "TO START GAME", 103, GB.LIGHT);
        
        textCenter(g, "USE LEFT / RIGHT ARROWS", 130, GB.LIGHT);
    }

    // ★ หน้าจอสรุปผลแบบตารางอันดับ (Leaderboard) ตอนจบเกม
    private void drawGameOverScreen(Graphics2D g) {
        g.setColor(new Color(0x0F, 0x38, 0x0F, 240));
        g.fillRect(8, 10, GB.W - 16, 124);

        g.setColor(GB.LIGHTEST);
        g.drawRect(8, 10, GB.W - 16, 124);

        textCenter(g, "== RANKING BOARD ==", 22, GB.LIGHTEST);
        
        // คัดลอกและเรียงลำดับผู้เล่นตามคะแนน Dodge จากมากไปน้อย
        List<CarView> ranking = new ArrayList<>(state.cars);
        ranking.sort((a, b) -> Integer.compare(b.dodge, a.dodge));

        int startY = 35;
        int rank = 1;
        for (CarView c : ranking) {
            String line = String.format("%d. %-5s [DG:%3d] %s", rank, c.name, c.dodge, (c.alive ? "WIN" : "OUT"));
            Color textColor = (c.id == state.myId) ? GB.LIGHTEST : GB.LIGHT;
            
            // วาดแถบไฮไลต์ถ้าเป็นไอดีของเราเอง
            if (c.id == state.myId) {
                g.setColor(GB.DARK);
                g.fillRect(12, startY - 8, GB.W - 24, 11);
            }
            
            text(g, line, 16, startY, textColor);
            startY += 12;
            rank++;
        }

        // แสดงเวลาที่ใช้ทั้งหมด
        String timeStr = String.format("TIME: %d:%02d", (int) state.time / 60, (int) state.time % 60);
        textCenter(g, timeStr, 110, GB.LIGHTEST);

        textCenter(g, "PRESS [R] TO RETRY", 126, GB.LIGHT);
    }

    private void drawRoad(Graphics2D g) {
        int h = GB.TRACK_BOT - GB.TRACK_TOP;
        g.setColor(GB.LIGHTEST);
        g.fillRect(GB.ROAD_X, GB.TRACK_TOP, GB.ROAD_W, h);

        g.setColor(GB.LIGHT);
        for (int i = 1; i < GB.LANES; i++) {
            int x = GB.ROAD_X + i * GB.LANE_W;
            for (int y = GB.TRACK_TOP; y < GB.TRACK_BOT; y += 8)
                g.fillRect(x, y, 1, 4);
        }

        int off = (tick / 2) % 6;
        for (int y = GB.TRACK_TOP - 6 + off; y < GB.TRACK_BOT; y += 6) {
            int yy = Math.max(y, GB.TRACK_TOP);
            int hh = Math.min(6, GB.TRACK_BOT - yy);
            if (hh <= 0) continue;
            g.setColor((((y + 600) / 6) % 2 == 0) ? GB.DARKEST : GB.DARK);
            g.fillRect(GB.ROAD_X - 5, yy, 4, hh);
            g.fillRect(GB.ROAD_X + GB.ROAD_W + 1, yy, 4, hh);
        }
    }

    private void drawMeteors(Graphics2D g) {
        for (MeteorView m : state.meteors) {
            int cx = GB.laneCenterX(m.lane);
            int y  = (int) m.y, r = GB.METEOR_SZ / 2;

            g.setColor(GB.LIGHT);
            g.fillRect(cx - 1, y - r - 4, 2, 4);

            g.setColor(GB.DARKEST);
            g.fillOval(cx - r, y - r, GB.METEOR_SZ, GB.METEOR_SZ);
            g.setColor(GB.DARK);
            g.fillOval(cx - r + 2, y - r + 2, GB.METEOR_SZ - 5, GB.METEOR_SZ - 5);
        }
    }

    private void drawCars(Graphics2D g) {
        for (CarView c : state.cars)
            if (c.id != state.myId && c.alive) drawOneCar(g, c, false);
        for (CarView c : state.cars)
            if (c.id == state.myId && c.alive) drawOneCar(g, c, true);
    }

    private void drawOneCar(Graphics2D g, CarView c, boolean isMe) {
        int off = isMe ? 0 : ((c.id % 2 == 0) ? -4 : 4);
        int x = GB.laneCenterX(c.lane) + off - GB.CAR_W / 2;
        int y = GB.CAR_Y;

        Color body  = isMe ? GB.DARKEST : GB.DARK;
        Color panel = isMe ? GB.LIGHT   : GB.LIGHTEST;

        if (isMe) drawF1Solid(g, x, y, body, panel);
        else      drawF1Hollow(g, x, y, body);

        if (!isMe) text(g, "P" + c.id, x + 2, y - 3, GB.DARK);
    }

    private void drawF1Solid(Graphics2D g, int x, int y, Color body, Color panel) {
        g.setColor(body);
        g.fillRect(x + 1,  y,      14, 2);
        g.fillRect(x,      y + 2,  3,  3);
        g.fillRect(x + 13, y + 2,  3,  3);
        g.fillRect(x + 6,  y + 2,  4,  8);
        g.fillRect(x + 3,  y + 10, 10, 4);
        g.fillRect(x + 5,  y + 14, 6,  3);
        g.fillRect(x,      y + 15, 3,  4);
        g.fillRect(x + 13, y + 15, 3,  4);
        g.fillRect(x + 1,  y + 18, 14, 2);

        g.setColor(panel);
        g.fillRect(x + 7,  y + 6,  2,  3);
        g.fillRect(x + 5,  y + 11, 2,  2);
        g.fillRect(x + 9,  y + 11, 2,  2);

        g.setColor(GB.LIGHTEST);
        g.drawRect(x - 2, y - 2, GB.CAR_W + 3, GB.CAR_H + 3);
    }

    private void drawF1Hollow(Graphics2D g, int x, int y, Color body) {
        g.setColor(body);
        g.drawRect(x + 1,  y,      13, 1);
        g.drawRect(x,      y + 2,  2,  2);
        g.drawRect(x + 13, y + 2,  2,  2);
        g.drawRect(x + 6,  y + 3,  3,  11);
        g.drawRect(x + 3,  y + 10, 9,  3);
        g.drawRect(x,      y + 15, 2,  3);
        g.drawRect(x + 13, y + 15, 2,  3);
        g.drawRect(x + 1,  y + 18, 13, 1);
    }

    private void drawTopHud(Graphics2D g) {
        g.setColor(GB.DARKEST);
        g.fillRect(0, 0, GB.W, 11);
        text(g, String.format("%d:%02d", (int) state.time / 60, (int) state.time % 60), 3, 8, GB.LIGHTEST);
        text(g, "ALIVE " + state.aliveCount() + "/" + state.cars.size(), 46, 8, GB.LIGHTEST);
        
        CarView me = state.find(state.myId);
        if (me != null) text(g, "DG " + me.dodge, 120, 8, GB.LIGHTEST);
    }

    private void drawBottomHud(Graphics2D g) {
        g.setColor(GB.DARKEST);
        g.fillRect(0, GB.TRACK_BOT + 1, GB.W, GB.H - GB.TRACK_BOT);
        int x = 4;
        for (CarView c : state.cars) {
            text(g, "P" + c.id + (c.alive ? " O" : " X"), x, GB.H - 4,
                 c.alive ? GB.LIGHTEST : GB.DARK);
            x += 39;
        }
    }
}