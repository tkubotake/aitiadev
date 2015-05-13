package com.example.mycrop;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class TrimView extends View {

	public float m_fTouchDownedX = 0, m_fTouchDownedY = 0;
	Paint m_paintMask;
	Paint m_paintLine;
	Paint m_paintCircle;
    static String TouchMode = "NONE";
    float _distance = 0f;
    static int THRESHOLD = 20;
    static float CROP_SCALE_MAX = 1.5f;
    static float CROP_SCALE_MIN = 0.5f;
    NKRect m_Guide = new NKRect(0,0,0,0);

    NKRect m_ImgRect = new NKRect(0,0,0,0);
    final int STOP_X = 0;
    final int STOP_Y = 1;
    final int STOP_XY = 2;
    
	//-----------------------------------------------------------------------------------
    public TrimView(Context context) {
        super(context);
        m_paintMask = new Paint();
        m_paintMask.setColor(0x55000000);
        m_paintMask.setAntiAlias(true);
        
        m_paintLine = new Paint();
        m_paintLine.setStyle(Style.STROKE);
        m_paintLine.setColor(Color.LTGRAY);
        
        m_paintCircle = new Paint();
        m_paintCircle.setAntiAlias(true);
        m_paintCircle.setColor(Color.LTGRAY);
    }
        
   	//-----------------------------------------------------------------------------------        
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*
        _w = w;
        _h = h;
        sqWidth = w - 40;
        sqHeight = w - 40;
        sqX = w / 2;
        sqY = h / 2;
        */
    }

	//-----------------------------------------------------------------------------------
    public void setImgRect(){
        Log.d("TEST","init()");
        m_ImgRect.set( (getWidth() - ((FrameLayout)getParent()).findViewById(R.id.imgview).getWidth()) / 2
        		,(getHeight() - ((FrameLayout)getParent()).findViewById(R.id.imgview).getHeight()) / 2
        		,((FrameLayout)getParent()).findViewById(R.id.imgview).getWidth()
        		, ((FrameLayout)getParent()).findViewById(R.id.imgview).getHeight());

        m_Guide.setCenter(m_ImgRect.getCenterX(),m_ImgRect.getCenterY());
    }

	//-----------------------------------------------------------------------------------
    public void setGuideSize(int w, int h) {     
        m_Guide.set(0, 0, w, h);
        m_ImgRect.clear();
    }
    
	//-----------------------------------------------------------------------------------
    public NKRect getTrimData(){
        return m_Guide;
    }

	//-----------------------------------------------------------------------------------
    protected void onDraw(Canvas canvas) {
        if(m_ImgRect.getHeight() == 0) setImgRect();

        drawMask(canvas);
        canvas.drawRect(m_Guide.getLeft(), m_Guide.getTop(), m_Guide.getRight(), m_Guide.getBottom(), m_paintLine);
        canvas.drawCircle(m_Guide.getCenterX(), m_Guide.getTop(), 12, m_paintCircle);
        canvas.drawCircle(m_Guide.getCenterX(), m_Guide.getBottom(), 12, m_paintCircle);
        canvas.drawCircle(m_Guide.getLeft(), m_Guide.getCenterY(), 12, m_paintCircle);
        canvas.drawCircle(m_Guide.getRight(), m_Guide.getCenterY(), 12, m_paintCircle);
    }
    
	//-----------------------------------------------------------------------------------
    private void drawMask(Canvas canvas){
    	canvas.drawRect(0, 0, getRight(), m_Guide.getTop(), m_paintMask);
        canvas.drawRect(0, m_Guide.getTop(),m_Guide.getLeft(), m_Guide.getBottom(), m_paintMask);
        canvas.drawRect(m_Guide.getRight(), m_Guide.getTop(),m_ImgRect.getRight(), m_Guide.getBottom(), m_paintMask);
        canvas.drawRect(0, m_Guide.getBottom(), getWidth(), m_ImgRect.getBottom(), m_paintMask);
    }
    
	//-----------------------------------------------------------------------------------
    public boolean onTouchEvent(MotionEvent e) {
    	Point pBeforeCenter = new Point();
    	float fBeforeScale = m_Guide.scale;
    	pBeforeCenter.set((int)m_Guide.getCenterX(), (int)m_Guide.getCenterY());
    	
        switch (e.getAction()) {
        case MotionEvent.ACTION_DOWN:
            m_fTouchDownedX = e.getX();
            m_fTouchDownedY = e.getY();

            if (m_Guide.getLeft()+THRESHOLD < m_fTouchDownedX && m_fTouchDownedX < m_Guide.getRight()-THRESHOLD) { // ‚˜“I‚É‚¢‚¤‚ÆG‚ê‚Ä‚¢‚é
                if (m_Guide.getTop()+THRESHOLD < m_fTouchDownedY && m_fTouchDownedY < m_Guide.getBottom() - THRESHOLD) { // y“I‚É‚¢‚¤‚ÆG‚ê‚Ä‚¢‚é
                    TouchMode = "MOVE";
                }else if(m_Guide.getTop() -THRESHOLD < m_fTouchDownedY && m_fTouchDownedY < m_Guide.getBottom()+THRESHOLD ){
                    _distance = culcDistance((int)m_Guide.getCenterX(),(int)m_Guide.getCenterY(),(int)e.getX(),(int)e.getY());
                    Log.d("TEST","Scale");
                    TouchMode = "SCALE";
                }
            }else if(m_Guide.getLeft() -THRESHOLD < m_fTouchDownedX && m_fTouchDownedX < m_Guide.getRight()+THRESHOLD){
                if(m_Guide.getTop() -THRESHOLD < m_fTouchDownedY && m_fTouchDownedY < m_Guide.getBottom()+THRESHOLD){
                    _distance = culcDistance((int)m_Guide.getCenterX(),(int)m_Guide.getCenterY(),(int)e.getX(),(int)e.getY());
                    TouchMode = "SCALE";
                }
            }

            break;
        case MotionEvent.ACTION_MOVE:
            if (TouchMode == "MOVE") {
                float disX = e.getX() - m_fTouchDownedX;
                float disY = e.getY() - m_fTouchDownedY;
                m_Guide.setCenter(m_Guide.getCenterX() + disX, m_Guide.getCenterY() + disY);
   
            }else if(TouchMode == "SCALE"){                
                float _cdistance = culcDistance((int)m_Guide.getCenterX(),(int)m_Guide.getCenterY(),(int)e.getX(),(int)e.getY());
                float fScale = _cdistance/_distance;
                fScale = (fScale < 1.05)? fScale: 1.05f;
                fScale = (fScale > 0.95)? fScale: 0.95f;
                m_Guide.scale *= fScale;
            }
            
            switch (stopMovingWhenNavIsMovedAtTheEdge()) {
			case STOP_X:
            	m_Guide.setCenterX(pBeforeCenter.x);
            	m_Guide.scale = fBeforeScale;				
				break;
			case STOP_Y:
            	m_Guide.setCenterY(pBeforeCenter.y);
            	m_Guide.scale = fBeforeScale;				
				break;
			case STOP_XY:
            	m_Guide.setCenter(pBeforeCenter.x, pBeforeCenter.y);
            	m_Guide.scale = fBeforeScale;				
            	break;
			default:
				break;
			}
            
            if(m_Guide.scale < CROP_SCALE_MIN || m_Guide.scale > CROP_SCALE_MAX){
            	m_Guide.scale = fBeforeScale;            	
            }

            _distance = culcDistance((int)m_Guide.getCenterX(),(int)m_Guide.getCenterY(),(int)e.getX(),(int)e.getY());
            m_fTouchDownedX = e.getX();
            m_fTouchDownedY = e.getY();
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            TouchMode = "NONE";
            break;
        default:
            break;
        }
        return true;
    }

    //-----------------------------------------------------------------------------------
    
    private int stopMovingWhenNavIsMovedAtTheEdge(){
    	int nRes = -1;
    	
        if (m_Guide.getLeft() < m_ImgRect.getLeft()) {
        	nRes = STOP_X;
        } else if (m_Guide.getRight() >= m_ImgRect.getRight()) {
        	nRes = STOP_X;
        }
        
        if (m_Guide.getTop() < m_ImgRect.getTop()) {
        	nRes = (nRes == STOP_X) ? STOP_XY : STOP_Y;
        } else if (m_Guide.getBottom() >= m_ImgRect.getBottom()) {
        	nRes = (nRes == STOP_X) ? STOP_XY : STOP_Y;
        }
        return nRes;
    }
    
	//-----------------------------------------------------------------------------------
    private float culcDistance(int x1,int y1,int x2,int y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return FloatMath.sqrt(x * x + y * y);
    }
	//-----------------------------------------------------------------------------------


}