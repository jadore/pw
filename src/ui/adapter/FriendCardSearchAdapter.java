package ui.adapter;

import java.util.List;

import ui.WeFriendCardSearch;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.mycontact.R;

import config.CommonValue.LianXiRenType;
import bean.CardIntroEntity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendCardSearchAdapter extends BaseExpandableListAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	private DisplayImageOptions displayOption;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	static class SectionHolder {
		TextView typeView;
		View divider;
	}
	
	public FriendCardSearchAdapter(Context context, List<List<CardIntroEntity>> cards) {
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
	public Object getChild(int groupPosition, int childPosition) {
		return cards.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
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
		final CardIntroEntity model = cards.get(groupPosition).get(childPosition);
		ImageLoader.getInstance().displayImage(model.avatar, cell.avatarImageView, displayOption);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		cell.alpha.setVisibility(View.GONE);
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (model.cardSectionType.equals(LianXiRenType.mobile)) {	
					((WeFriendCardSearch)context).showMobileView(model);
				}
				else {
					((WeFriendCardSearch)context).showCardView(model);
				}
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return cards.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return cards.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return cards.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		SectionHolder section = null;
		if (convertView == null) {
			section = new SectionHolder();
			convertView = inflater.inflate(R.layout.index_section, null);
			section.typeView = (TextView) convertView.findViewById(R.id.titleView);
			section.divider = (View) convertView.findViewById(R.id.divider);
			convertView.setTag(section);
		}
		else {
			section = (SectionHolder) convertView.getTag();
		}
		
		if (getChildrenCount(groupPosition) == 0) {
			section.typeView.setVisibility(View.GONE);
			section.divider.setVisibility(View.GONE);
		}
		else {
			section.typeView.setVisibility(View.VISIBLE);
			section.divider.setVisibility(View.VISIBLE);
			section.typeView.setText(cards.get(groupPosition).get(0).cardSectionType);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
