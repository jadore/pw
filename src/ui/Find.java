package ui;

import com.google.zxing.client.android.CaptureActivity;
import com.vikaa.mycontact.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Find extends AppActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.scanit:
			showScan();
			break;

		}
	}
	
	private void showScan() {
		startActivity(new Intent(this, CaptureActivity.class));
	}
}
