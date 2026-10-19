package com.boids;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class GameComponent extends Canvas implements Runnable {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 970;

    public static final float METER_TO_PIXELS = 14.f;
    public static final float WIDTH_METERS = WIDTH / METER_TO_PIXELS;
    public static final float HEIGHT_METERS = HEIGHT / METER_TO_PIXELS;

    public static final int BORDER = 50;
    public static final int BORDER_COLOR = 0x173f5f;

    public static final int NUMBER_OF_BOIDS = 100;//1_000_000;
    public static final int BG_COLOR = 0;

    private static final boolean limitFPS = true;

    private static final Random random = new Random();

    private BufferedImage img;
    private Renderer renderer;

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
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        renderer = new Renderer(pixels);

        initBoids();
        //initTestBoids();
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

        renderer.fillBackground(BG_COLOR);
        renderer.drawBorders();

        for (int i = 0; i < this.updatedBoids.size(); i++){
            Boid b = this.updatedBoids.get(i);

            Boid b1 = this.currentBoids.get(i);
            b.pos.x = b1.pos.x;
            b.pos.y = b1.pos.y;
            b.vel.x = b1.vel.x;
            b.vel.y = b1.vel.y;
            b.acc.x = b1.acc.x;
            b.acc.y = b1.acc.y;

            b.updateAcc(currentBoids,
                        BORDER / METER_TO_PIXELS, (WIDTH - BORDER) / METER_TO_PIXELS,
                        BORDER / METER_TO_PIXELS, (HEIGHT - BORDER) / METER_TO_PIXELS);

            b.move(WIDTH - 1, HEIGHT - 1, (float)dt);
            renderer.drawBoid(b);
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

    private void initTestBoids() {
        this.currentBoids = new ArrayList();
        this.updatedBoids = new ArrayList();
        currentBoids.add(new Boid((BORDER + 20) / METER_TO_PIXELS, HEIGHT_METERS / 2,
                                  Boid.MAX_SPEED_METERS, 0f, Boid.NPC_BOID_COLOR, false));
        currentBoids.add(new Boid((WIDTH - BORDER - 20) / METER_TO_PIXELS, HEIGHT_METERS / 2,
                                Boid.MAX_SPEED_METERS * -1.0f, 0f, Boid.FOCUSED_BOID_COLOR, false));

        updatedBoids.add(new Boid((BORDER + 20) / METER_TO_PIXELS, HEIGHT_METERS / 2,
                                  Boid.MAX_SPEED_METERS, 0, Boid.NPC_BOID_COLOR, false));
        updatedBoids.add(new Boid((WIDTH - BORDER - 20) / METER_TO_PIXELS, HEIGHT_METERS / 2,
                                      Boid.MAX_SPEED_METERS * -1.0f, 0, Boid.FOCUSED_BOID_COLOR, false));
    }

    private void initBoids(){
        Random r = new Random();
        this.currentBoids = new ArrayList();
        this.updatedBoids = new ArrayList();
        for (int i = 0; i < NUMBER_OF_BOIDS; i++){
            float x = r.nextInt((int)WIDTH_METERS);
            float y = r.nextInt((int)HEIGHT_METERS);

            boolean xPositiveDir = r.nextBoolean();
            boolean yPositiveDir = r.nextBoolean();
            float deltaX = xPositiveDir ? r.nextFloat() : r.nextFloat() * -1;
            float deltaY = yPositiveDir ? r.nextFloat() : r.nextFloat() * -1;
            PVector velocity = new PVector(deltaX, deltaY);
            velocity.normalize();
            velocity.mul(Boid.MIN_SPEED_METERS);

            int color = Boid.NPC_BOID_COLOR;
            boolean isFocused = false;
            // make the last one of different color
            if (i == NUMBER_OF_BOIDS - 1){
                color = Boid.FOCUSED_BOID_COLOR;
                isFocused = true;
            }

            currentBoids.add(new Boid(x, y, velocity.x, velocity.y, color, isFocused));
            updatedBoids.add(new Boid(x, y, velocity.x, velocity.y, color, isFocused));
        }
        System.out.println("Size: " + currentBoids.size());
    }
}
