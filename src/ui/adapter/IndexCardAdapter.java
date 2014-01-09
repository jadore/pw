package ui.adapter;

import java.util.List;

import bean.ActivityIntroEntity;
import bean.CardIntroEntity;

import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ui.CardView;
import ui.Index;
import ui.adapter.IndexActivityAdapter.CellHolder;
import ui.adapter.IndexActivityAdapter.SectionView;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;

public class IndexCardAdapter extends SectionedBaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	public IndexCardAdapter(Context context, List<List<CardIntroEntity>> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	static class SectionView {
		TextView titleView;
	}
	
	@Override
	public Object getItem(int section, int position) {
		return cards.get(section).get(position);
	}

	@Override
	public long getItemId(int section, int position) {
		return position;
	}

	@Override
	public int getSectionCount() {
		return cards.size();
	}

	@Override
	public int getCountForSection(int section) {
		return cards.get(section).size();
	}

	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.index_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(section).get(position);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		if (model.cardSectionType.equals(CommonValue.CardSectionType.OwnedSectionType)) {
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((Index)context).showCardViewWeb(model);
				}
			});
		}
		else if (model.cardSectionType.equals(CommonValue.CardSectionType.BarcodeSectionType)) {
			if (position == 0) {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Index)context).showMyBarcode();
					}
				});
			}
			else {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Index)context).showScan();
					}
				});
			}
		}
		else if (model.cardSectionType.equals(CommonValue.CardSectionType.FeedbackSectionType)) {
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((Index)context).showFeedback();
				}
			});
		}
		return convertView;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		SectionView sect = null;
		if (convertView == null) {
			sect = new SectionView();
			convertView = inflater.inflate(R.layout.index_section, null);
			sect.titleView = (TextView) convertView.findViewById(R.id.titleView);
			convertView.setTag(sect);
		}
		else {
			sect = (SectionView) convertView.getTag();
		}
		sect.titleView.setText(cards.get(section).get(0).cardSectionType);
		return convertView;
	}
	
}
