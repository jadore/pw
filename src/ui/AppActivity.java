package ui;

import android.os.Bundle;
import config.MyApplication;
import tools.AppContext;
import tools.BaseActivity;
import tools.Logger;

public class AppActivity extends BaseActivity {
	protected MyApplication appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext =  (MyApplication)getApplication();
	}
}
