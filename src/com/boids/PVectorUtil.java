package com.boids;

public class PVectorUtil{

    public static PVector mul(PVector v, float s){
	return new PVector(v.x * s, v.y * s);
    }

    public static PVector subtract(PVector v1, PVector v2){
	return new PVector(v1.x - v2.x, v1.y - v2.y);
    }

    public static PVector copy(PVector v){
	return new PVector(v.x, v.y);
    }

    public static float dotProd(PVector v1, PVector v2){
	return v1.x * v2.x + v1.y * v2.y;
    }

    public static float getCos(PVector v1, PVector v2){
	float mV1 = v1.magnitude();
	float mV2 = v2.magnitude();
	return dotProd(v1, v2) / (mV1*mV2);
    }
}
