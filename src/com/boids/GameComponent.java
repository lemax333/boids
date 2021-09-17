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
    public static final int BG_COLOR = 0x173f5f;

    private int[] pixels;
    private BufferedImage img;

    public GameComponent(){
	Dimension size = new Dimension(WIDTH, HEIGHT);
	setSize(size);
	setPreferredSize(size);
	setMinimumSize(size);
	setMaximumSize(size);

	setIgnoreRepaint(true);

	img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }

    public void start(){
	Thread thread = new Thread(this);
	thread.start();
    }

    public void run(){
	Random r = new Random();

	long lastTime = System.nanoTime();
	double secondsFromLastTick = 0;
	double secondsPerTick = 1 / 60.0;
	int framesCount = 0;

	List<Boid> boids;
	boids = new ArrayList<>();
	boids = initBoids(r);

	while(true){
	    long now = System.nanoTime();
	    long processedTime = now - lastTime;
	    lastTime = now;
	    secondsFromLastTick += processedTime / 1000000000.0;

	    if (secondsFromLastTick >= secondsPerTick){
		System.out.println("FPS: " + (int)(1 / secondsFromLastTick));
		gameTick(boids, (float) secondsFromLastTick);
		secondsFromLastTick = 0;
	    } else {
		try {
		    Thread.sleep(1);
		} catch (InterruptedException e){
		    e.printStackTrace();
		}
	    }
	}
    }

    private void gameTick(List<Boid> boids, float dt){
	BufferStrategy bs = getBufferStrategy();
	if (bs == null){
	    createBufferStrategy(2);
	    bs = getBufferStrategy();
	}

	fillBackground(BG_COLOR);

	for (int i = 0; i < NUMBER_OF_BOIDS; i++){
	    Boid b = boids.get(i);
	    b.updateAcc(boids);
	    b.move(WIDTH - 1, HEIGHT - 1, dt);
	    drawBoid(b);
	}

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

    private List<Boid> initBoids(Random r){
	List<Boid> boids = new ArrayList();
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
		color = Boid.FOCUSED_BOID_COLOR;
		isFocused = true;
	    }

	    boids.add(new Boid(x, y, velocity.x, velocity.y, Boid.WIDTH, color, isFocused));
	}
	return boids;
    }

    private void drawBoid(Boid b){
	PVector direction = PVectorUtil.copy(b.vel);
	direction.normalize();
	drawCircle(Math.round(b.pos.x), Math.round(b.pos.y),
		   direction, b.width, b.color);
    }

    private void drawCircle(int xPos, int yPos, PVector direction, int diametr, int color){
	int radius = diametr / 2;
	int[] circle = new int[diametr * diametr];

	for (int y = 0; y < radius; y++){
	    int x = (int)Math.round(Math.sqrt(radius * radius - y * y));
	    for (int col = 0; col < x; col++){
		int cY = y + radius;
		int cX = col + radius;
		circle[cX + cY * diametr] = color;

		cX = col * -1 + radius;
		circle[cX + cY * diametr] = color;

		cY = y * -1 + radius;
		cX = col + radius;
		circle[cX + cY * diametr] = color;

		cX = col * -1 + radius;
		circle[cX + cY * diametr] = color;
	    }

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
		circle[xCoord + yCoord] = 0xFF0000;
	    }
	} else {
	    for (int x = 0; x > direction.x; x--){
		float y = m * x;
		y = radius + y;
		int xCoord = x + radius;
		int yCoord = (int)y * diametr;
		circle[xCoord + yCoord] = 0xFF0000;
	    }
	}


	blitImagePixels(circle, xPos, yPos, diametr, color);
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
