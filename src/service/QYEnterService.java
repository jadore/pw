/**
 * QYdonal
 */
package service;

import java.util.List;
import java.util.Timer;

import tools.Logger;
import ui.Welcome;

import com.vikaa.mycontact.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * QY
 *
 * @author donal
 *
 */
public class QYEnterService extends Service{
	private Timer mTimer;  
    public static final int FOREGROUND_ID = 0;  
	
	@Override
	public void onCreate() {
		super.onCreate();
		startForeground(FOREGROUND_ID, new Notification()); 
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {  
        startTimer();  
        return super.onStartCommand(intent, flags, startId);  
    } 
	
	private void startTimer() {  
        if (mTimer == null) {  
            mTimer = new Timer();  
            EnterTask lockTask = new EnterTask(this);  
            mTimer.schedule(lockTask, 0L, 1000L);  
        }  
    }  
	
	@Override
	public void onDestroy() {
		stopForeground(true);  
        mTimer.cancel();  
        mTimer.purge();  
        mTimer = null; 
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
