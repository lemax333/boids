package com.boids;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class GameComponent extends Canvas implements Runnable {

    public static final int WIDTH = 960;
    public static final int HEIGHT = 720;

    public static final int NUMBER_OF_BOIDS = 100;//1_000_000;
    //public static final int BG_COLOR = 0x173f5f;
    public static final int BG_COLOR = 0;

    private static final boolean limitFPS = true;

    private static final Random random = new Random();

    private int[] pixels;
    private BufferedImage img;

    private int[] origCircle;

    private List<Boid> currentBoids;
    private List<Boid> updatedBoids;

    public GameComponent(){
        Dimension size = new Dimension(WIDTH, HEIGHT);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        setIgnoreRepaint(true);

        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

        initBoids();
    }

    public void start(){
	Thread thread = new Thread(this);
        thread.start();
    }

    public void run(){
        long lastTime = System.nanoTime();
        double secondsFromLastTick = 0;
        double secondsPerTick = 1 / 60.0;
        int framesCount = 0;

        while(true){
            long now = System.nanoTime();
            long processedTime = now - lastTime;
            lastTime = now;
            secondsFromLastTick += processedTime / 1000000000.0;

            if (limitFPS) {
                if (secondsFromLastTick >= secondsPerTick){
                    System.out.println("FPS: " + (int)(1 / secondsFromLastTick));
                    gameTick(secondsFromLastTick);
                    secondsFromLastTick = 0;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            } else {
                //System.out.println("FPS: " + (int)(1 / secondsFromLastTick));
                System.out.println("Millis per frame: " + processedTime / 1000000.0);
                gameTick(secondsFromLastTick);

                secondsFromLastTick = 0;
            }

        }
    }

    private void gameTick(double dt){
        BufferStrategy bs = getBufferStrategy();
        if (bs == null){
            createBufferStrategy(2);
            bs = getBufferStrategy();
        }

        fillBackground(BG_COLOR);

        for (int i = 0; i < NUMBER_OF_BOIDS; i++){
            Boid b = this.updatedBoids.get(i);

            Boid b1 = this.currentBoids.get(i);
            b.pos.x = b1.pos.x;
            b.pos.y = b1.pos.y;
            b.vel.x = b1.vel.x;
            b.vel.y = b1.vel.y;
            b.acc.x = b1.acc.x;
            b.acc.y = b1.acc.y;

            b.updateAcc(currentBoids);
            b.move(WIDTH - 1, HEIGHT - 1, (float)dt);
            drawBoid(b);
        }

        List<Boid> tmp = this.currentBoids;
        this.currentBoids = this.updatedBoids;
        this.updatedBoids = tmp;

        Graphics g  = bs.getDrawGraphics();
        g.setColor(Color.RED);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        g.dispose();

        bs.show();
    }

    private void fillBackground(int color){
        for (int i = 0; i < pixels.length; i++){
            pixels[i] = color;
        }
    }

    private void initBoids(){
        Random r = new Random();
        this.currentBoids = new ArrayList();
        this.updatedBoids = new ArrayList();
        for (int i = 0; i < NUMBER_OF_BOIDS; i++){
            float x = r.nextInt(WIDTH);
            float y = r.nextInt(HEIGHT);

            boolean xPositiveDir = r.nextBoolean();
            boolean yPositiveDir = r.nextBoolean();
            float deltaX = xPositiveDir ? r.nextFloat() : r.nextFloat() * -1;
            float deltaY = yPositiveDir ? r.nextFloat() : r.nextFloat() * -1;
            PVector velocity = new PVector(deltaX, deltaY);
            velocity.normalize();
            velocity.mul(Boid.MIN_SPEED);

            int color = Boid.NPC_BOID_COLOR;
            boolean isFocused = false;
            // make the last one of different color
            if (i == NUMBER_OF_BOIDS - 1){
                //drawing other color is not working for some reason
                //color = Boid.FOCUSED_BOID_COLOR;
                isFocused = true;
            }

            currentBoids.add(new Boid(x, y, velocity.x, velocity.y, Boid.WIDTH, color, isFocused));
            updatedBoids.add(new Boid(x, y, velocity.x, velocity.y, Boid.WIDTH, color, isFocused));
        }
        System.out.println("Size: " + currentBoids.size());
    }

    private void drawBoid(Boid b){
        PVector direction = PVectorUtil.copy(b.vel);
        direction.normalize();
        drawCircle(Math.round(b.pos.x), Math.round(b.pos.y),
                   direction, b.width, b.color);
    }

    private void drawCircle(int xPos, int yPos, PVector direction, int diametr, int color){
        int radius = diametr / 2;
        //TODO: this will work only if all boids are of the same color
        if (origCircle == null) {
            origCircle = new int[diametr * diametr];

            for (int y = 0; y < radius; y++){
                int x = (int)Math.round(Math.sqrt(radius * radius - y * y));
                for (int col = 0; col < x; col++){
                    int cY = y + radius;
                    int cX = col + radius;
                    origCircle[cX + cY * diametr] = color;

                    cX = col * -1 + radius;
                    origCircle[cX + cY * diametr] = color;

                    cY = y * -1 + radius;
                    cX = col + radius;
                    origCircle[cX + cY * diametr] = color;

                    cX = col * -1 + radius;
                    origCircle[cX + cY * diametr] = color;
                }

            }
        }

        int[] circleWithLine = new int[diametr * diametr];
        for (int i = 0; i < diametr * diametr; i++) {
            circleWithLine[i] = origCircle[i];
        }



        // draw line
        direction.mul(radius);
        float m = 0;
        if (direction.y == 0f)
            m = 0;
        else if (direction.x == 0f)
            m = direction.y;
        else
            m = direction.y / direction.x;

        if (direction.x >= 0){
            for (int x = 0; x < direction.x; x++){
                float y = m * x;
                y = radius + y;
                int xCoord = x + radius;
                int yCoord = (int)y * diametr;
                circleWithLine[xCoord + yCoord] = 0xFF0000;
            }
        } else {
            for (int x = 0; x > direction.x; x--){
                float y = m * x;
                y = radius + y;
                int xCoord = x + radius;
                int yCoord = (int)y * diametr;
                circleWithLine[xCoord + yCoord] = 0xFF0000;
            }
        }


        blitImagePixels(circleWithLine, xPos, yPos, diametr, color);
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
                if (c == color)
                    pixels[pX + pY * WIDTH] = c;
            }
        }
    }
}
