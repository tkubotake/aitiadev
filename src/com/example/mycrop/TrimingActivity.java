package com.example.mycrop;

import java.io.File;
import java.io.OutputStream;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class TrimingActivity extends Activity {
	TrimView m_tview;
    Bitmap _bmOriginal;
    float m_fScalePicToDisplay;

	//-----------------------------------------------------------------------------------    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cropview);
		
        System.gc();
        _bmOriginal = BitmapFactory.decodeResource(getResources(),R.drawable.meiga074); //BitmapHolder._holdedBitmap.copy(Bitmap.Config.ARGB_8888, true);
        BitmapHolder._holdedBitmap = null;
        
    	m_tview = new TrimView(getApplicationContext());
        ((FrameLayout)findViewById(R.id.baseview)).addView(m_tview);

        ((Button)findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NKRect crop = m_tview.getTrimData();
                Log.d("TEST","crop "+(int)crop.getLeft()+" "+(int)crop.getTop()+" "+(int)crop.getWidth()+" "+(int)crop.getHeight());
                crop.setCenterX(crop.getCenterX() - (int)(findViewById(R.id.imgview)).getLeft());
                crop.setCenterY(crop.getCenterY() - (int)(findViewById(R.id.imgview)).getTop());
                crop.scale /= m_fScalePicToDisplay;
                crop.setCenter(crop.getCenterX()/m_fScalePicToDisplay, crop.getCenterY()/m_fScalePicToDisplay);
//                Log.d("TEST","_bmOriginal w:"+_bmOriginal.getWidth()+" h:"+_bmOriginal.getHeight());
//                Log.d("TEST","crop "+(int)crop.getLeft()+" "+(int)crop.getTop()+" "+(int)crop.getWidth()+" "+(int)crop.getHeight());
//                Log.d("TEST","ImageView "+(int)((ImageView)findViewById(R.id.imgview)).getLeft()+" "+(int)((ImageView)findViewById(R.id.imgview)).getTop());
//                Log.d("TEST","baseview "+(int)((FrameLayout)findViewById(R.id.baseview)).getLeft()+" "+(int)((FrameLayout)findViewById(R.id.baseview)).getTop());
                
                BitmapHolder._holdedBitmap = Bitmap.createBitmap(_bmOriginal, (int)crop.getLeft(), (int)crop.getTop(), (int)crop.getWidth(), (int)crop.getHeight(), null, true);
                
                String strSaveImageFName = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
                saveOnExternalSD(getApplicationContext(), strSaveImageFName, BitmapHolder._holdedBitmap);
                
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
	//-----------------------------------------------------------------------------------
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
	//-----------------------------------------------------------------------------------
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	Log.d("TEST","onWindowFocusChanged() >>>");

        super.onWindowFocusChanged(hasFocus);
       	
        float fScaleW = (float) ((FrameLayout)findViewById(R.id.baseview)).getWidth() / (float) _bmOriginal.getWidth();
        float fScaleH = (float) ((FrameLayout)findViewById(R.id.baseview)).getHeight() / (float) _bmOriginal.getHeight();
        m_fScalePicToDisplay = Math.min(fScaleW, fScaleH);
        Matrix matrix = new Matrix();
        matrix.postScale(m_fScalePicToDisplay, m_fScalePicToDisplay);
        
        Bitmap _bm = Bitmap.createBitmap(_bmOriginal, 0, 0, _bmOriginal.getWidth(),_bmOriginal.getHeight(), matrix, true);
        ((ImageView)findViewById(R.id.imgview)).setImageBitmap(_bm);
    
        setTrimView(1);
    }

	//-----------------------------------------------------------------------------------------------------------------
    private void setTrimView(int nType){
        
        int nPicW = (int)(_bmOriginal.getWidth()*m_fScalePicToDisplay);
        int nPicH = (int)(_bmOriginal.getHeight()*m_fScalePicToDisplay);
        float fPicAspect = (float)nPicW / (float)nPicH;
        int nMaskWidth = 0,nMaskHeight = 0;
        switch (nType) {
		case 0: //square
			int nGuideSize = (nPicW < nPicH) ? nPicW : nPicH;
			m_tview.setGuideSize(nGuideSize,nGuideSize);
			break;
		case 1:	//3:4			
			if(fPicAspect < 0.75){
				nMaskWidth = nPicW;
				nMaskHeight = nPicW * 4 / 3;									
			}else{
				nMaskHeight = nPicH;
				nMaskWidth = nMaskHeight * 3 / 4;								
			}
			/*
			if(nPicW < nPicH){
				nMaskHeight = nPicH;
				nMaskWidth = nMaskHeight * 3 / 4;				
				if(nMaskWidth > findViewById(R.id.baseview).getWidth()){
					nMaskWidth = nPicW;
					nMaskHeight = nMaskWidth * 4 / 3;					
				}
			}else{
				nMaskWidth = nPicW;
				nMaskHeight = nMaskWidth * 4 / 3;
			}			
			*/
			m_tview.setGuideSize(nMaskWidth,nMaskHeight);			
			break;
		case 2:	//4:3
			if(fPicAspect < 1.333){
				nMaskWidth = nPicW;
				nMaskHeight = nPicW * 3 / 4;									
			}else{
				nMaskHeight = nPicH;
				nMaskWidth = nPicH * 4 / 3;								
			}

			/*
			if(nPicW < nPicH){
				nMaskWidth = nPicW;
				nMaskHeight = nMaskWidth * 3 / 4;
			}else{
				nMaskHeight = nPicH;
				nMaskWidth = nMaskHeight * 4 / 3;
			}			
			*/
			m_tview.setGuideSize(nMaskWidth,nMaskHeight);

			break;
		default:
			break;
		}
        m_tview.invalidate();
    }
	//-----------------------------------------------------------------------------------------------------------------
	public boolean saveOnExternalSD(Context content,String strSaveImageFName, Bitmap bitmap) {
		
		if(strSaveImageFName == null) strSaveImageFName = System.currentTimeMillis() + ".jpg";
		
		File file = new File(
				Environment.getExternalStorageDirectory().toString() + "/Pictures/",
				strSaveImageFName);

		ContentResolver contentResolver = content.getContentResolver();
		ContentValues values = new ContentValues(7);
		values.put(MediaStore.Images.Media.TITLE, strSaveImageFName);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, strSaveImageFName);
		values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.Images.Media.ORIENTATION, 0);
		values.put(MediaStore.Images.Media.DATA, file.getPath());
		values.put(MediaStore.Images.Media.SIZE, file.length());
		Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		
		try {
			OutputStream outputStream = contentResolver.openOutputStream(uri);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.close();
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	//-----------------------------------------------------------------------------------
	public void onClick(View v){
		switch(v.getId()){
		case R.id.btn0:
			setTrimView(1);
			break;
		case R.id.btn1:	//3:4
			setTrimView(0);
			break;
		case R.id.btn2:
			setTrimView(2);
			break;
		default:
			break;
		}
	}
	//-----------------------------------------------------------------------------------
}