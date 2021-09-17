package com.boids;

public class PVector{

    public float x;
    public float y;

    public PVector(){
    }

    public PVector(float x, float y){
	this.x = x;
	this.y = y;
    }

    public void add(PVector v){
	this.x += v.x;
	this.y += v.y;
    }

    public void subtract(PVector v){
	this.x -= v.x;
	this.y -= v.y;
    }

    public void mul(float s){
	this.x *= s;
	this.y *= s;
    }

    public void div(float s){
	this.x /= s;
	this.y /= s;
    }

    public void limit(float max){
	float magn = Math.abs(magnitude());
	if (magn > max){
	    normalize();
	    mul(max);
	}
    }

    public void inverse(){
	mul(-1);
    }

    public void clamp(float min, float max){
	float magn = Math.abs(magnitude());
	if (magn < min){
	    normalize();
	    mul(min);
	} else if (magn > max){
	    normalize();
	    mul(max);
	}
    }

    public float magnitude(){
	return (float)Math.sqrt(x*x + y*y);
    }

    public void normalize(){
	float magnitude = magnitude();
	div(magnitude);
    }
}
