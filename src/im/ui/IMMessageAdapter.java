/**
 * QYdonal
 */
package im.ui;

import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;

import java.util.List;

import tools.Logger;
import tools.StringUtils;

import bean.ChatterEntity;
import bean.Entity;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.MyApplication;
import config.AppClient.ClientCallback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * QY
 *
 * @author donal
 *
 */
public class IMMessageAdapter extends BaseAdapter{

	static class ViewHoler {
		TextView timeTV;
		
		RelativeLayout leftLayout;
		ImageView leftAvatar;
		TextView leftNickname;
		TextView leftText;
		
		RelativeLayout rightLayout;
		RelativeLayout rightFrame;
		ImageView rightAvatar;
		TextView rightNickname;
		TextView rightText;
		ProgressBar rightProgress;
	}
	
	private Context context;
	private LayoutInflater inflater;
	private List<IMMessage> items;
	private ImageLoader imageLoader;
	DisplayImageOptions options;
	
	public IMMessageAdapter(Context context, List<IMMessage> items, ImageLoader imageLoader) {
		this.context = context;
		this.items = items;
		inflater = LayoutInflater.from(context);
		this.imageLoader = imageLoader;
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.avatar_placeholder)
		.showImageForEmptyUri(R.drawable.avatar_placeholder)
		.showImageOnFail(R.drawable.avatar_placeholder)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHoler cell = null;
		if (convertView == null) {
			cell = new ViewHoler();
			convertView = inflater.inflate(R.layout.listviewcell_chat_normal, null);
			cell.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
			cell.leftLayout = (RelativeLayout) convertView.findViewById(R.id.layout_left);
			cell.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
			cell.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
			cell.leftText = (TextView) convertView.findViewById(R.id.textview_content_l);
					
			cell.rightLayout = (RelativeLayout) convertView.findViewById(R.id.layout_right);
			cell.rightFrame = (RelativeLayout) convertView.findViewById(R.id.layout_content_r);
			cell.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
			cell.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
			cell.rightText = (TextView) convertView.findViewById(R.id.textview_content_r);
			cell.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
			convertView.setTag(cell);
		}
		else {
			cell = (ViewHoler) convertView.getTag();
		}
		final IMMessage message = items.get(position);
		cell.leftLayout.setVisibility(message.msgType == IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming? View.VISIBLE:View.INVISIBLE);
		cell.rightLayout.setVisibility(message.msgType == IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming? View.INVISIBLE:View.VISIBLE);
		
		switch (message.msgType) {
		case IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming:
			getChatterFromCache(message.openId, cell.leftAvatar);
			break;

		case IMMessage.JSBubbleMessageType.JSBubbleMessageTypeOutgoing:
			imageLoader.displayImage(MyApplication.getInstance().getUserAvatar(), cell.rightAvatar, options);
			break;
		}
		cell.leftText.setText(message.content);
		cell.rightText.setText(message.content);
		cell.rightProgress.setVisibility(message.msgStatus==JSBubbleMessageStatus.JSBubbleMessageStatusDelivering? View.VISIBLE:View.INVISIBLE);
		cell.rightFrame.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if (message.msgStatus == JSBubbleMessageStatus.JSBubbleMessageStatusDelivering) {
					show1OptionsDialog(new String[]{"重新发送"}, message);
				}
				return false;
			}
		});
		return convertView;
	}

	private void show1OptionsDialog(final String[] arg ,final IMMessage model){
		new AlertDialog.Builder(context).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					try {
						((Chating)context).sendMessage(model);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}).show();
	}
	
	private void getChatterFromCache(String openId, ImageView avatar) {
		String key = String.format("%s-%s", CommonValue.CacheKey.ChatterInfo+"-"+openId, MyApplication.getInstance().getLoginUid());
		ChatterEntity entity = (ChatterEntity) MyApplication.getInstance().readObject(key);
		if(entity != null){
			if (StringUtils.notEmpty(entity.avatar)) {
				imageLoader.displayImage(entity.avatar, avatar, CommonValue.DisplayOptions.default_options);
			}
		}
		else {
			getChatter(openId, avatar);
		}
	}
	
	private synchronized void getChatter(String openId, final ImageView avatar) {
		AppClient.getChaterBy(MyApplication.getInstance(), openId, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				ChatterEntity chatter = (ChatterEntity) data;
				if (StringUtils.notEmpty(chatter.avatar)) {
					imageLoader.displayImage(chatter.avatar, avatar, CommonValue.DisplayOptions.default_options);
				}
			}
			
			@Override
			public void onFailure(String message) {
				
			}
			
			@Override
			public void onError(Exception e) {
				
			}
		});
	}
}
