package com.boids;

import java.util.List;

public class Boid {

    public static final float SIZE_METERS = 1f;
    public static final float MIN_SPEED_METERS = 3.2f;
    public static final float MAX_SPEED_METERS = 9.5f;
    public static final float MAX_ACC_METERS = 40.0f;

    private static final float SEPARATION_INDEX = 0.5f;
    private static final float SEPARATION_RADIUS = SIZE_METERS * 1.4f;

    private static final float COHESION_INDEX = 0.05f;
    private static final float COHESION_RADIUS = SIZE_METERS * 5.0f;

    private static final float ALIGNMENT_INDEX = 0.1f;
    private static final float ALIGNMENT_RADIUS = SIZE_METERS * 2.0f;

    private static final float FOV_ANGLE_COS = (float)Math.cos(Math.PI * 1.0);

    //public static final int NPC_BOID_COLOR = 0x3caea3;
    public static final int NPC_BOID_COLOR = 0xffffff;
    public static final int FOCUSED_BOID_COLOR = 0xed553b;

    public PVector pos;
    public PVector vel;
    public PVector acc;
    public float size;
    public int color;
    public boolean isFocused;

    public Boid(){
    }

    public Boid(float x, float y, float velX, float velY, int color, boolean isFocused){
	this.pos = new PVector(x, y);
	this.vel = new PVector(velX, velY);
	this.acc = new PVector();

	this.size = SIZE_METERS;
	this.color = color;

	this.isFocused = isFocused;
    }

    public void move(int boundaryX, int boundaryY, float dt){
	acc.mul(MAX_ACC_METERS);
	acc.mul(dt);

	vel.add(acc);
	vel.clamp(MIN_SPEED_METERS, MAX_SPEED_METERS);

	pos.add(PVectorUtil.mul(vel, dt));

	acc.mul(0);
    }

    private void stayInsideBoundaries(float boundaryXMin, float boundaryXMax,
                                     float boundaryYMin, float boundaryYMax) {
        if (this.pos.x < boundaryXMin) {
            acc.add(new PVector(0.3f, 0));
        } else if (this.pos.x > boundaryXMax) {
            acc.add(new PVector(-0.3f, 0));
        }

        if (this.pos.y < boundaryYMin) {
            acc.add(new PVector(0, 0.3f));
        } else if (this.pos.y > boundaryYMax) {
            acc.add(new PVector(0, -0.3f));
        }
    }

    public void updateAcc(List<Boid> boids,
                          float boundaryXMin, float boundaryXMax,
                          float boundaryYMin, float boundaryYMax){
	PVector separation = new PVector();
	PVector cohesion = new PVector();
	PVector alignment = new PVector();

        stayInsideBoundaries(boundaryXMin, boundaryXMax, boundaryYMin, boundaryYMax);

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
