package ui.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.mycontact.R;

import config.CommonValue.DisplayOptions;
import config.CommonValue.LianXiRenType;
import bean.CardIntroEntity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendCardAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	private DisplayImageOptions displayOption;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	public FriendCardAdapter(Context context, List<CardIntroEntity> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
		this.displayOption = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.avatar_placeholder)
		.showImageForEmptyUri(R.drawable.avatar_placeholder)
		.showImageOnFail(R.drawable.avatar_placeholder)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) 
		.displayer(new BitmapDisplayer() {
			@Override
			public void display(Bitmap bitmap, ImageAware imageAware,
					LoadedFrom loadedFrom) {
				imageAware.setImageBitmap(bitmap);
			}
		})
		.build();
	}
	
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int arg0) {
		return cards.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int groupPosition, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.friend_card_cell, null);
			cell.alpha = (TextView) convertView.findViewById(R.id.alpha);
			cell.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.callButton = (Button) convertView.findViewById(R.id.call);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(groupPosition);

		ImageLoader.getInstance().displayImage(model.avatar, cell.avatarImageView, displayOption);
		
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		String currentStr = model.py;
		String previewStr = (groupPosition - 1) >= 0 ? cards.get(groupPosition - 1).py : " ";
		if (!previewStr.equals(currentStr)) {
			cell.alpha.setVisibility(View.VISIBLE);
			cell.alpha.setText(currentStr);
			if (currentStr.equals("~")) {
				cell.alpha.setText("#");
			}
		}
		else {
			cell.alpha.setVisibility(View.GONE);
		}
		return convertView;
	}
	
}
