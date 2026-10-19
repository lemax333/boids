package com.boids;

import static com.boids.GameComponent.WIDTH;
import static com.boids.GameComponent.HEIGHT;
import static com.boids.GameComponent.BORDER;
import static com.boids.GameComponent.BORDER_COLOR;
import static com.boids.GameComponent.METER_TO_PIXELS;
import static com.boids.GameComponent.BG_COLOR;

public class Renderer {


    public int[] pixels;
    private int[] filledCircle;


    public Renderer(int[] pixels) {
        this.pixels = pixels;
    }

    public void fillBackground(int color){
        for (int i = 0; i < pixels.length; i++){
            pixels[i] = color;
        }
    }


    public void drawBorders() {
        for (int i = 0; i < WIDTH; i++) {
            pixels[(BORDER*WIDTH) + i] = BORDER_COLOR;
            pixels[((HEIGHT - BORDER) * WIDTH) + i] = BORDER_COLOR;
        }
        for (int i = 0; i < HEIGHT; i++){
            pixels[(i*WIDTH) + BORDER] = BORDER_COLOR;
            pixels[(i*WIDTH) + WIDTH - BORDER] = BORDER_COLOR;
        }
    }


    public void drawBoid(Boid b){
        PVector direction = PVectorUtil.copy(b.vel);
        direction.normalize();
        drawCircle(Math.round(b.pos.x * METER_TO_PIXELS), Math.round(b.pos.y * METER_TO_PIXELS),
                   direction, Math.round(b.size * METER_TO_PIXELS), b.color);
    }


    private void drawCircle(int xPos, int yPos, PVector direction, int diametr, int color){
        int radius = diametr / 2;
        //TODO: don't generate the same cirlce every time, create one per color and reuse it
        if (filledCircle == null) {
            filledCircle = new int[diametr * diametr];
            for (int y = 0; y < radius; y++){
                int x = (int)Math.round(Math.sqrt(radius * radius - y * y));
                for (int col = 0; col < x; col++){
                    int cY = y + radius;
                    int cX = col + radius;
                    filledCircle[cX + cY * diametr] = 0xFFFFFF;

                    cX = col * -1 + radius;
                    filledCircle[cX + cY * diametr] = 0xFFFFFF;

                    cY = y * -1 + radius;
                    cX = col + radius;
                    filledCircle[cX + cY * diametr] = 0xFFFFFF;

                    cX = col * -1 + radius;
                    filledCircle[cX + cY * diametr] = 0xFFFFFF;
                }

            }
        }

        blitImagePixels(filledCircle, xPos, yPos, diametr, color);

        direction.mul(radius);
        PVector dirEnd = direction.addNew(new PVector(xPos, yPos));
        drawLine(new ScreenPoint(xPos, yPos), new ScreenPoint((int)dirEnd.x, (int)dirEnd.y), BG_COLOR);
    }

    public static class ScreenPoint{
        public int x;
        public int y;

        public ScreenPoint(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    private void drawLine(ScreenPoint p1, ScreenPoint p2, int color) {
        int dx = Math.abs(p2.x - p1.x);
        int dy = Math.abs(p2.y - p1.y);
        boolean directionX = dx > dy;
        if (directionX) {
            ScreenPoint beg = (p1.x <= p2.x) ? p1 : p2;
            ScreenPoint end = (p1.x <= p2.x) ? p2 : p1;
            float slope = (float) (end.y - beg.y) / (float) (end.x - beg.x);
            float err = 0.5f;
            int yIncr = slope > 0 ? 1 : -1;
            int y = beg.y;
            for (int x = beg.x; x <= end.x; x++) {
                err += Math.abs(slope);
                if (err > 1.0) {
                    y += yIncr;
                    err -= 1.0f;
                }
                int index = x + y * WIDTH;
                if (index > 0 && index < HEIGHT * WIDTH) {
                    pixels[x + y * WIDTH] = color;
                }
            }
        } else {
            ScreenPoint beg = (p1.y < p2.y) ? p1 : p2;
            ScreenPoint end = (p1.y < p2.y) ? p2 : p1;
            float slope = (float) (end.x - beg.x) / (float) (end.y - beg.y);
            float err = 0.5f;
            int xIncr = slope > 0 ? 1 : -1;
            int x = beg.x;
            for (int y = beg.y; y <= end.y; y++) {
                err += Math.abs(slope);
                if (err > 1.0) {
                    x += xIncr;
                    err -= 1.0f;
                }
                int index = x + y * WIDTH;
                if (index > 0 && index < HEIGHT * WIDTH) {
                    pixels[x + y * WIDTH] = color;
                }
            }
        }
    }

    private void blitImagePixels(int[] img, int x, int y, int width, int color){
        for (int i = 0; i < width; i++){
            int pY = i + y - width/2;
            if (pY < 0 || pY >= HEIGHT)
                continue;
            for (int j = 0; j < width; j++){
                int pX = j + x - width/2 ;
                if (pX < 0 || pX >= WIDTH)
                    continue;
                int c = img[j + i * width];
                if (c == 0xFFFFFF)
                    pixels[pX + pY * WIDTH] = color;
            }
        }
    }
}
