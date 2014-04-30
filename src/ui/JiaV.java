package ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import bean.Entity;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class JiaV extends AppActivity{
	private RelativeLayout mingpianLayout;
	private ImageView mingpianIV;
	private TextView mingpianTV;
	
	private RelativeLayout idLayout;
	private ImageView idIV;
	private TextView idTV;
	private Button mingpianButton;
	private Button idButton;
	private int type;
	String theLarge;
	
	private String mingpianFile;
	private String idFile;
	
	private String code;
	private String uploadToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jiav);
		initUI();
		code = getIntent().getStringExtra("code");
		uploadToken = getIntent().getStringExtra("token");
	}
	
	private void initUI() {
		mingpianLayout = (RelativeLayout) findViewById(R.id.mingpianLayout);
		mingpianIV = (ImageView) findViewById(R.id.mingpian);
		mingpianTV = (TextView) findViewById(R.id.mingpianProgress);
		idLayout = (RelativeLayout) findViewById(R.id.idLayout);
		idIV = (ImageView) findViewById(R.id.id);
		idTV = (TextView) findViewById(R.id.idProgress);
		mingpianButton = (Button) findViewById(R.id.mingpianbutton);
		idButton = (Button) findViewById(R.id.idbutton);
		
		int w = ImageUtils.getDisplayWidth(this) - ImageUtils.dip2px(this, 40);
		int h = w*320/568;
		
		LayoutParams layout = (LayoutParams) mingpianLayout.getLayoutParams();
		layout.width = w;
		layout.height = h;
		mingpianLayout.setLayoutParams(layout);
		
		LayoutParams layout2 = (LayoutParams) idLayout.getLayoutParams();
		layout2.width = w;
		layout2.height = h;
		idLayout.setLayoutParams(layout2);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			submit();
			break;
		case R.id.mingpianbutton:
			type = 0;
			PhotoChooseOption();
			break;
			
		case R.id.idbutton:
			type = 1;
			PhotoChooseOption();
			break;
		}
	}
	
	private void submit() {
		if (StringUtils.empty(mingpianFile) || StringUtils.empty(idFile)) {
			return;
		}
		AppClient.cardVIP(code, idFile, mingpianFile, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(String message) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		String newPhotoPath;
		switch (requestCode) {
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (StringUtils.notEmpty(theLarge)) {
				File file = new File(theLarge);
				File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + "/" + file.getName();
				try {
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 300), 90);
					newPhotoPath = imagePathAfterCompass;
					upload(type, newPhotoPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			if(data == null)  return;
			Uri thisUri = data.getData();
        	String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(thisUri);
        	if(StringUtils.empty(thePath)) {
        		newPhotoPath = ImageUtils.getAbsoluteImagePath(this,thisUri);
        	}
        	else {
        		newPhotoPath = thePath;
        	}
        	File file = new File(newPhotoPath);
			File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + "/" + file.getName();
			try {
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 300), 90);
				newPhotoPath = imagePathAfterCompass;
				upload(type, newPhotoPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	private void upload(int type, String path) {
		String key = IO.UNDEFINED_KEY; 
		PutExtra extra = new PutExtra();
		switch (type) {
		case 0:
			mingpianTV.setVisibility(View.VISIBLE);
			mingpianTV.setText("0%");
			this.imageLoader.displayImage("file://"+path, mingpianIV);
			extra.params = new HashMap<String, String>();
			Logger.i(uploadToken);
			IO.putFile(uploadToken, key, new File(path), extra, new JSONObjectRet() {
				@Override
				public void onProcess(long current, long total) {
					
					float percent = (float) (current*1.0/total)*100;
					if ((int)percent < 100) {
						mingpianTV.setText((int)percent+"%");
					}
					else if ((int)percent == 100) {
						mingpianTV.setText("处理中...");
					}
				}

				@Override
				public void onSuccess(JSONObject resp) {
					String hash = resp.optString("hash", "");
					mingpianFile = hash;
					Logger.i(hash);
					mingpianTV.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onFailure(Exception ex) {
					Logger.i(ex.toString());
				}
			});
			break;

		case 1:
			this.imageLoader.displayImage("file://"+path, idIV);
			idTV.setVisibility(View.VISIBLE);
			idTV.setText("0%");
			extra.params = new HashMap<String, String>();
			Logger.i(uploadToken);
			IO.putFile(uploadToken, key, new File(path), extra, new JSONObjectRet() {
				@Override
				public void onProcess(long current, long total) {
					
					float percent = (float) (current*1.0/total)*100;
					if ((int)percent < 100) {
						idTV.setText((int)percent+"%");
					}
					else if ((int)percent == 100) {
						idTV.setText("处理中...");
					}
				}

				@Override
				public void onSuccess(JSONObject resp) {
					String hash = resp.optString("hash", "");
					idFile = hash;
					idTV.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onFailure(Exception ex) {
					Logger.i(ex.toString());
				}
			});
			break;
		}
	}
	
	
	private void PhotoChooseOption() {
		CharSequence[] item = {"相册", "拍照"};
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star).setItems(item,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int item)
					{
						//手机选图
						if( item == 0 )
						{
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
							intent.addCategory(Intent.CATEGORY_OPENABLE); 
							intent.setType("image/*"); 
							startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
						}
						//拍照
						else if( item == 1 )
						{	
							String savePath = "";
							//判断是否挂载了SD卡
							String storageState = Environment.getExternalStorageState();		
							if(storageState.equals(Environment.MEDIA_MOUNTED)){
								savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + ImageUtils.DCIM;//存放照片的文件夹
								File savedir = new File(savePath);
								if (!savedir.exists()) {
									savedir.mkdirs();
								}
							}
							
							//没有挂载SD卡，无法保存文件
							if(StringUtils.empty(savePath)){
								UIHelper.ToastMessage(JiaV.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
								return;
							}

							String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
							String fileName = "c_" + timeStamp + ".jpg";//照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);
							
							theLarge = savePath + fileName;//该照片的绝对路径
//							
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}   
					}}).create();
			
			 imageDialog.show();
	}
	
	
}
