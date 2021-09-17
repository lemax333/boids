package com.boids;

import java.util.List;

public class Boid {
    public static final int WIDTH = 20;
    public static final float MAX_SPEED = 400f;
    public static final float MIN_SPEED = 200f;
    public static final float MAX_FORCE = 800f;

    private static final float SEPARATION_INDEX = 0.7f;
    private static final float SEPARATION_RADIUS = WIDTH * 1.8f;

    private static final float COHESION_INDEX = 0.3f;
    private static final float COHESION_RADIUS = 700f;

    private static final float ALIGNMENT_INDEX = 0.3f;
    private static final float ALIGNMENT_RADIUS = 70f;

    //private static final float BORDER_OMITTING_INDEX = 0.3f;

    private static final float FOV_ANGLE_COS = (float)Math.cos(Math.PI * 0.8);

    public static final int NPC_BOID_COLOR = 0x3caea3;
    public static final int FOCUSED_BOID_COLOR = 0xed553b;

    public PVector pos;
    public PVector vel;
    public PVector acc;
    public int width;
    public int color;
    public boolean isFocused;

    public Boid(){
    }

    public Boid(float x, float y, float velX, float velY, int width, int color, boolean isFocused){
	this.pos = new PVector(x, y);
	this.vel = new PVector(velX, velY);
	this.acc = new PVector();

	this.width = width;
	this.color = color;

	this.isFocused = isFocused;
    }

    public void move(int boundaryX, int boundaryY, float dt){
	acc.mul(MAX_FORCE);
	acc.mul(dt);

	vel.add(acc);
	vel.clamp(MIN_SPEED, MAX_SPEED);

	pos.add(PVectorUtil.mul(vel, dt));

	acc.mul(0);

	// stay inside screen
	if (pos.x < 0) pos.x = boundaryX;
	if (pos.x > boundaryX) pos.x = 0;

	if (pos.y < 0) pos.y = boundaryY;
	if (pos.y > boundaryY) pos.y = 0;

    }

    public void updateAcc(List<Boid> boids){
	PVector separation = new PVector();
	PVector cohesion = new PVector();
	PVector alignment = new PVector();

	int separationCount = 0;
	int cohesionCount = 0;
	int alignmentCount = 0;
	for (Boid boid : boids){
	    if (boid == this)
		continue;
	    PVector toBoidPos = PVectorUtil.subtract(boid.pos, pos);
	    float distance = toBoidPos.magnitude();
	    float angleCos = PVectorUtil.getCos(toBoidPos, vel);
	    toBoidPos.normalize();

	    if (angleCos >= FOV_ANGLE_COS){
		if (distance < SEPARATION_RADIUS){
		    PVector toSeparatedBoidPos = PVectorUtil.copy(toBoidPos);
		    toSeparatedBoidPos.inverse();
		    toSeparatedBoidPos.div(distance);
		    separation.add(toSeparatedBoidPos);
		    separationCount++;
		}
		if (distance < COHESION_RADIUS){
		    cohesion.add(boid.pos);
		    cohesionCount++;
		}
		if (distance < ALIGNMENT_RADIUS){
		    alignment.add(boid.vel);
		    alignmentCount++;
		}
	    }
	}

	if (separationCount > 0){
	    separation.normalize();
	    separation.mul(SEPARATION_INDEX);
	    acc.add(separation);
	}
	if (cohesionCount > 0){
	    cohesion.div(cohesionCount);
	    cohesion.subtract(pos);

	    cohesion.normalize();
	    cohesion.mul(COHESION_INDEX);
	    acc.add(cohesion);
	}
	if (alignmentCount > 0){
	    alignment.div(alignmentCount);
	    alignment.subtract(vel);

	    alignment.normalize();
	    alignment.mul(ALIGNMENT_INDEX);
	    acc.add(alignment);
	}
    }

    @Override
    public String toString(){
	return new StringBuilder().append("Pos: ").append(pos.x).append(":").append(pos.y).append("\n")
	    .append("Vel: ").append(vel.x).append(":").append(vel.y).toString();
    }
}
