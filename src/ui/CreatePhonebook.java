package ui;

import com.vikaa.mycontact.R;

import tools.ImageUtils;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

public class CreatePhonebook extends AppActivity {
	private EditText edit;
	private int INSERTIMG_CODE = 501;
	private int PICTUREBUTTONCODE = 502;
	private int RECORDBUTTONCODE = 503;
	private int screeHeight;
	private float MAXSIZE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_phonebook);
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		MAXSIZE = maxMemory / 8;
		initApp();
	}

	private void initApp() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screeWidth = displayMetrics.widthPixels;
		screeHeight = displayMetrics.heightPixels;

		edit = (EditText) findViewById(R.id.chat_content);

	}

//	private void setListener() {
//		Button insertImg = (Button) findViewById(R.id.insert_img);
//		insertImg.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setType("image/*");
//				intent.setAction(Intent.ACTION_GET_CONTENT);
//				startActivityForResult(intent, INSERTIMG_CODE);
//			}
//		});
//
//		Button takePhoto = (Button) findViewById(R.id.takePhoto);
//		takePhoto.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				startActivityForResult(intent, PICTUREBUTTONCODE);
//			}
//		});
//
//		Button record = (Button) findViewById(R.id.record);
//		record.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// ����Android�Դ����Ƶ¼��Ӧ��
//				Intent intent = new Intent(
//						MediaStore.Audio.Media.RECORD_SOUND_ACTION);
//				startActivityForResult(intent, RECORDBUTTONCODE);
//			}
//		});
//
//		Button save = (Button) findViewById(R.id.save);
//		save.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				/**
//				 * 1,����ļ����� 2,���ļ�д���ڴ濨���ļ��� 3,�´�����ʱ,���ļ�������
//				 * */
//				if (beyoundMaxSize(edit)) {
//					dialog("����ʧ��", "�ļ����,�޷�����", "����");
//				} else {
//					saveFile(edit);
//					dialog("��ʾ", "����ɹ�", "ȷ��");
//				}
//			}
//		});
//	}
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.cameraButton:
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, INSERTIMG_CODE);
			
			break;

		default:
			break;
		}
	}

//	protected void saveFile(EditText edit2) {
//		String conent = edit2.getText().toString();
//		Editable eb = edit2.getEditableText();
//		conent = Html.toHtml(eb);
//		System.out.println("����:" + conent);
//		writeFileData(StartActivity.filename, conent);
//	}
//
//	public void writeFileData(String filename, String message) {
//		try {
//			// ���FileOutputStream
//			FileOutputStream fout = openFileOutput(filename, MODE_APPEND);
//			// ��Ҫд����ַ�ת��Ϊbyte����
//			byte[] bytes = message.getBytes();
//			fout.write(bytes);// ��byte����д���ļ�
//			fout.close();// �ر��ļ������
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("д��ʧ��");
//		}
//	}

	protected void dialog(String string, String string2, String string3) {
		new AlertDialog.Builder(this)
				.setTitle(string)
				.setMessage(string2)
				.setPositiveButton(string3,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

	protected boolean beyoundMaxSize(EditText edit2) {
		float size = edit2.getTextSize();
		System.out.println("size:" + size);
		if (size >= MAXSIZE) {
			return true;
		}
		return false;
	}

	private Drawable getMyDrawable(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);

//		int imgHeight = drawable.getIntrinsicHeight();
//		int imgWidth = drawable.getIntrinsicWidth();
		int width = ImageUtils.getDisplayWidth(this);
        int height = width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
        drawable.setBounds(-1, 0, width, height);
//		System.out.println("setWidth:" + imgWidth);
//		drawable.setBounds(0, 0, imgWidth, imgHeight);
		return drawable;
	}

	private ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			String f = source.substring(0, 1);
			String url = source.substring(2);
			if (f.equals("1")) {
				try {
					ContentResolver cr = CreatePhonebook.this.getContentResolver();
					Uri uri = Uri.parse(url);
					Bitmap bitmap = getimage(cr, uri);
					System.out.println("bitmap:" + bitmap);
					return getMyDrawable(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return null;
			}
		}
	};
	private int screeWidth;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == INSERTIMG_CODE) {
			Uri uri = data.getData();
			Editable eb = edit.getEditableText();
			int startPosition = edit.getSelectionStart();
			eb.insert(
					startPosition,
					Html.fromHtml("<br/><img src='1:" + uri.toString()
							+ "'/><br/>", imageGetter, null));
		} else if (resultCode == RESULT_OK && requestCode == PICTUREBUTTONCODE) {
			Uri uri = data.getData();
			Editable eb = edit.getEditableText();
			int startPosition = edit.getSelectionStart();
			eb.insert(
					startPosition,
					Html.fromHtml("<br/><img src='1:" + uri.toString()
							+ "'/><br/>", imageGetter, null));
		}
	}

	private Bitmap getimage(ContentResolver cr, Uri uri) {
		try {
			Bitmap bitmap = null;
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			newOpts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(cr.openInputStream(uri), null, newOpts);

			newOpts.inJustDecodeBounds = false;
			int imgWidth = newOpts.outWidth;
			int imgHeight = newOpts.outHeight;
			int scale = 1;

			if (imgWidth > imgHeight && imgWidth > screeWidth) {
				scale = (int) (imgWidth / screeWidth);
			} else if (imgHeight > imgWidth && imgHeight > screeHeight) {
				scale = (int) (imgHeight / screeHeight);
			}
			newOpts.inSampleSize = scale;
			bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null,
					newOpts);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
