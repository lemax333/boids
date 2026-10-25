package com.boids;

import java.util.List;

public class Boid {

    public static final float SIZE = 1f;
    public static final float MIN_SPEED = 5.f;
    public static final float MAX_SPEED = 9.f;
    public static final float MAX_FORCE = 9.f;

    private static final float BOUNDARY_FORCE_INDEX = 1.5f;

    private static final float SEPARATION_INDEX = 2.f;
    private static final float SEPARATION_RADIUS = SIZE * 2.f;

    private static final float COHESION_INDEX = 0.5f;
    private static final float COHESION_RADIUS = SIZE * 5.0f;

    private static final float ALIGNMENT_INDEX = 0.7f;
    private static final float ALIGNMENT_RADIUS = SIZE * 2.0f;

    private static final float FOV_ANGLE_COS = (float)Math.cos(Math.PI * 1.0);

    public static final int NPC_BOID_COLOR = 0xffffff;
    public static final int FOCUSED_BOID_COLOR = 0xed553b;

    public PVector pos;
    public PVector vel;
    public PVector acc;
    public int color;
    public boolean isFocused;

    public Boid(){
    }

    public Boid(float x, float y, float velX, float velY, int color, boolean isFocused){
	this.pos = new PVector(x, y);
	this.vel = new PVector(velX, velY);
	this.acc = new PVector();
	this.color = color;

	this.isFocused = isFocused;
    }

    public void move(float dt){
	acc.mul(dt);

	vel.add(acc);
	vel.clamp(MIN_SPEED, MAX_SPEED);

	pos.add(PVectorUtil.mul(vel, dt));

	acc.mul(0);
    }

    private PVector stayInsideBoundaries(float boundaryXMin, float boundaryXMax,
                                     float boundaryYMin, float boundaryYMax) {

        PVector boundaryVector = new PVector(0.f, 0.f);
        boolean boundaryForceApplied = false;
        if (this.pos.x < boundaryXMin) {
            boundaryVector.add(new PVector(1.f, 0));
            boundaryForceApplied = true;
        } else if (this.pos.x > boundaryXMax) {
            boundaryVector.add(new PVector(-1.f, 0));
            boundaryForceApplied = true;
        }

        if (this.pos.y < boundaryYMin) {
            boundaryVector.add(new PVector(0, 1.f));
            boundaryForceApplied = true;
        } else if (this.pos.y > boundaryYMax) {
            boundaryVector.add(new PVector(0, -1.f));
            boundaryForceApplied = true;
        }

        return boundaryForceApplied ? steerTowards(boundaryVector) : boundaryVector;
    }

    public void updateAcc(List<Boid> boids,
                          float boundaryXMin, float boundaryXMax,
                          float boundaryYMin, float boundaryYMax){
        acc.mul(0);
	PVector separation = new PVector();
	PVector cohesion = new PVector();
	PVector alignment = new PVector();

        PVector boundaryForce = stayInsideBoundaries(boundaryXMin, boundaryXMax, boundaryYMin, boundaryYMax);
        boundaryForce.mul(BOUNDARY_FORCE_INDEX);
        acc.add(boundaryForce);

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
            separation = steerTowards(separation);
            separation.mul(SEPARATION_INDEX);
	    acc.add(separation);
	}
	if (cohesionCount > 0){
	    cohesion.div(cohesionCount);
	    cohesion.subtract(pos);
            cohesion = steerTowards(cohesion);
            cohesion.mul(COHESION_INDEX);
	    acc.add(cohesion);
	}
	if (alignmentCount > 0){
	    alignment.div(alignmentCount);
	    alignment.subtract(vel);
            alignment = steerTowards(alignment);
            alignment.mul(ALIGNMENT_INDEX);
	    acc.add(alignment);
	}
    }

    private PVector steerTowards(PVector desiredVelocity) {
        desiredVelocity.normalize();
        desiredVelocity.mul(MAX_SPEED);
        desiredVelocity.subtract(vel);
        desiredVelocity.clamp(0.f, MAX_FORCE);
        return desiredVelocity;
    }

    @Override
    public String toString(){
	return new StringBuilder().append("Pos: ").append(pos.x).append(":").append(pos.y).append("\n")
	    .append("Vel: ").append(vel.x).append(":").append(vel.y).toString();
    }
}
