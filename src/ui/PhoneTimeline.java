package ui;

import com.vikaa.mycontact.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import tools.AppManager;
import tools.BaseActivity;
import widget.ActionItem;
import widget.TitlePopup;

public class PhoneTimeline extends BaseActivity {
	private TitlePopup titlePopup;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_timeline);
		initUI();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			titlePopup.show(v);
			break;
		}
	}
	
	private void initUI() {
		titlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titlePopup.addAction(new ActionItem(this, "成员", R.drawable.icon_set_card));
	}
}
