package ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.mycontact.R;

import config.AppClient;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadAvatar extends AppActivity{
	private String code;
	private String sign;
	private String uploadToken;
	private String avatar;
	private String hash;
	private ImageView avatarIV;
	private TextView avatarTV;
	
	private String theLarge;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_avatar);
		code = getIntent().getStringExtra("code");
		uploadToken = getIntent().getStringExtra("token");
		avatar = getIntent().getStringExtra("avatar");
		sign = getIntent().getStringExtra("sign");
		initUI();
	}
	
	private void initUI() {
		avatarIV = (ImageView) findViewById(R.id.mingpian);
		avatarTV = (TextView) findViewById(R.id.mingpianProgress);
		if (StringUtils.empty(avatar)) {
			this.imageLoader.displayImage("drawable://"+ R.drawable.content_image_loading, avatarIV, avatar_options);
		}
		else {
			Logger.i(avatar);
			this.imageLoader.displayImage(avatar, avatarIV, avatar_options);
		}
		
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
			PhotoChooseOption();
			break;
		}
	}
	
	private void submit() {
		if (StringUtils.empty(hash)) {
			UIHelper.ToastMessage(this, "请上传新的头像", Toast.LENGTH_SHORT);
			return;
		}
		AppClient.setAvatar(code, sign, hash);
		setResult(RESULT_OK);
		AppManager.getAppManager().finishActivity(this);
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
								UIHelper.ToastMessage(UploadAvatar.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
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
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 100), 80);
					newPhotoPath = imagePathAfterCompass;
					upload(newPhotoPath);
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
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 100), 80);
				newPhotoPath = imagePathAfterCompass;
				upload(newPhotoPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	public DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
	.bitmapConfig(Bitmap.Config.RGB_565)
	.cacheInMemory(true)
	.cacheOnDisc(true)
	.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
	.displayer(new RoundedBitmapDisplayer(ImageUtils.dip2px(AppManager.getAppManager().currentActivity(), 72)))
	.build();
	
	private void upload(String path) {
		String key = IO.UNDEFINED_KEY; 
		PutExtra extra = new PutExtra();
		avatarTV.setVisibility(View.VISIBLE);
		avatarTV.setText("0%");
		this.imageLoader.displayImage("file://"+path, avatarIV, avatar_options);
		extra.params = new HashMap<String, String>();
		IO.putFile(uploadToken, key, new File(path), extra, new JSONObjectRet() {
			@Override
			public void onProcess(long current, long total) {
				
				float percent = (float) (current*1.0/total)*100;
				if ((int)percent < 100) {
					avatarTV.setText((int)percent+"%");
				}
				else if ((int)percent == 100) {
					avatarTV.setText("处理中...");
				}
			}

			@Override
			public void onSuccess(JSONObject resp) {
				hash = resp.optString("key", "");
				avatarTV.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onFailure(Exception ex) {
				Logger.i(ex.toString());
			}
		});
	}
}
