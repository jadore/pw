package ui.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.CommonValue.PhoneSectionType;
import bean.PhoneIntroEntity;
import ui.Index;
import ui.MyCard;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IndexPhoneAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<PhoneIntroEntity> phones;
	public IndexPhoneAdapter(Context context, List<PhoneIntroEntity> phones) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.phones = phones;
	}

	static class CellHolder {
		ImageView avatarView;
		TextView titleView;
		TextView desView;
		TextView timeView;
	}

	@Override
	public int getCount() {
		return phones.size();
	}
	
	@Override
	public Object getItem(int arg0) {
		return phones.get(arg0);
	}
	
	@Override
	public long getItemId(int arg0) {
		return phones.get(arg0).getId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.index_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.avatarView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.timeView = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final PhoneIntroEntity model = phones.get(position);
		ImageLoader.getInstance().displayImage(model.logo, cell.avatarView, CommonValue.DisplayOptions.default_options);
		cell.titleView.setText(model.title);
		cell.desView.setText(String.format("人数:%s 点击数:%s", model.member, model.hits));
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (model.phoneSectionType.equals(PhoneSectionType.OwnedSectionType) 
						|| model.phoneSectionType.equals(PhoneSectionType.JoinedSectionType)) {
					((Index)context).showPhoneViewWeb(model);
				}
				else {
					((Index)context).showActivityViewWeb(model);
				}
			}
		});
		return convertView;
	}

}
