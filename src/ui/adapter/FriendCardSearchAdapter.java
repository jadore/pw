package ui.adapter;

import im.ui.Chating;

import java.util.List;
import java.util.concurrent.ExecutionException;

import tools.Logger;
import tools.StringUtils;
import ui.CardView;
import ui.QYWebView;
import ui.WeFriendCard;
import ui.WeFriendCardSearch;
import ui.adapter.FriendCardAdapter.CellHolder;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.CommonValue.LianXiRenType;
import bean.CardIntroEntity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendCardSearchAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	
	private ImageLoader imageLoader;
	private DisplayImageOptions displayOptions ;
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	public FriendCardSearchAdapter(Context context, List<CardIntroEntity> cards, ImageLoader imageLoader) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
		this.imageLoader = imageLoader;
		this.displayOptions = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
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
	
	private void showCardView(CardIntroEntity entity) {
		Intent intent = new Intent(context, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
		((WeFriendCardSearch)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void showMobileView(CardIntroEntity entity) {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		Uri personUri = ContentUris.withAppendedId(uri, Integer.valueOf(entity.code));
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_VIEW);
		intent2.setData(personUri);
		context.startActivity(intent2);
	}

	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
		final CardIntroEntity model = cards.get(position);
		this.imageLoader.displayImage(model.avatar, cell.avatarImageView, this.displayOptions);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		String currentStr = model.cardSectionType;
		String previewStr = (position - 1) >= 0 ? cards.get(position - 1).cardSectionType : " ";
		if (!previewStr.equals(currentStr)) {
			cell.alpha.setVisibility(View.VISIBLE);
			cell.alpha.setText(currentStr);
		}
		else {
			cell.alpha.setVisibility(View.GONE);
		}
		return convertView;
	}
	
}
