package com.example.mycrop;

import android.util.Log;

public class NKRect {

	private float x; //origin x
	private float y; //origin y
	private float width;
	private float height;
	public float aspectRatio;
	public float scale = 1f;

	public NKRect(float nx, float ny, float nw, float nh) {
		set(nx,ny,nw,nh);
	}
	
	public void set(float nx, float ny, float nw, float nh){
		scale = 1f;
		width = nw;
		height = nh;
		x = nx;
		y = ny;
		aspectRatio = nw / nh;
	}
	
	public void clear(){
		scale = 1f;
		width = 0;
		height = 0;
		x = 0;
		y = 0;
		aspectRatio = 1f;
	}

	public float getCenterX(){
		return x+width/2;
	}
	
	public float getCenterY(){
		return y+height/2;
	}
	
	public float getTop(){
		return getCenterY()-height/2*scale;
	}

	public float getLeft(){
		return getCenterX()-width/2*scale;
	}

	public float getBottom(){
		return getCenterY()+height/2*scale;
	}
	
	public float getRight(){
		return getCenterX()+width/2*scale;
	}

	public float getHeight(){
		return height*scale;
	}

	public float getWidth(){
		return width*scale;
	}
	
	public void setCenterX(float fx){
		x = fx - width/2;
	}
	
	public void setCenterY(float fy){
		y = fy - height/2;
	}

	public void setCenter(float fx, float fy){
		setCenterX(fx);
		setCenterY(fy);
	}
}