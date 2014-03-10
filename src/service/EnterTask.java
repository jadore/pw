/**
 * QYdonal
 */
package service;

import java.util.TimerTask;

import com.vikaa.mycontact.R;

import config.CommonValue;
import config.MyApplication;

import tools.Logger;
import ui.Welcome;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * QY
 *
 * @author donal
 *
 */
public class EnterTask extends TimerTask {
	WindowManager wm = null;
	WindowManager.LayoutParams wmParams = null;
	View view;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;
	private int state;
	private float StartX;
	private float StartY;
	
	private Context mContext;  
    String targetPackageName = "com.tencent.mm";  

    private ActivityManager mActivityManager; 
    SharedPreferences sharedPre;
    
    public EnterTask(Context context) {  
        mContext = context;  
        sharedPre = mContext.getSharedPreferences("float_flag",	Activity.MODE_PRIVATE);
        createView();
        mActivityManager = (ActivityManager) context.getSystemService("activity");  
        
        
    } 
    
	@Override
	public void run() {
		ComponentName topActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;  
        String packageName = topActivity.getPackageName();  
//        String className = topActivity.getClassName();  
//        Logger.i("packageName" + packageName);  
//        Logger.i("className" + className);  
  
        try {
        	Message message = new Message();      
        	if (targetPackageName.equals(packageName)  ) {
        		message.what = 1;      
            }
            else {
            	message.what = 0;     
            }
        	handler.sendMessage(message);
        } catch (Exception e) {
        	Logger.i(e);
        }
	}
	
	Handler handler = new Handler(){   
        public void handleMessage(Message msg) {  
        	int flag = sharedPre.getInt("float", 0);
            switch (msg.what) {      
            case 1:      
            	if (flag == 0) {
            		wm.addView(view, wmParams);
            		SharedPreferences.Editor editor = sharedPre.edit();
            		editor.putInt("float", 1);
            		editor.commit();
    			}
                break;   
            default:
            	if (flag == 1) {
    	        	wm.removeView(view);
    	        	SharedPreferences.Editor editor = sharedPre.edit();
    	    		editor.putInt("float", 0);
    	    		editor.commit();
            	}
            	break;
            }      
            super.handleMessage(msg);  
        }  
          
    };  

	private void createView() {
		view = LayoutInflater.from(mContext).inflate(R.layout.app_enter, null);
		wm = (WindowManager) mContext.getApplicationContext().getSystemService("window");
		SharedPreferences.Editor editor = sharedPre.edit();
		editor.putInt("float", 0);
		editor.commit();
		wmParams = MyApplication.getInstance().getMywmParams();
		wmParams.type = 2002;
		wmParams.flags |= 8;
		wmParams.gravity = Gravity.TOP | Gravity.LEFT;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = 1;
		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					state = MotionEvent.ACTION_DOWN;
					StartX = x;
					StartY = y;
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					state = MotionEvent.ACTION_MOVE;
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					if (state != MotionEvent.ACTION_MOVE) {
						Intent intent = new Intent(mContext, Welcome.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
						mContext.startActivity(intent);
					}
					else {
						updateViewPosition();
						mTouchStartX = mTouchStartY = 0;
					}
					state = MotionEvent.ACTION_UP;
					break;
				}
				return true;
			}
		});
	}
	
	private void updateViewPosition() {
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(view, wmParams);
	}
}
