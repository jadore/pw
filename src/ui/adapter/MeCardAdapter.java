package ui.adapter;

import java.util.List;

import ui.Index;
import ui.Me;
import ui.adapter.IndexCardAdapter.CellHolder;
import ui.adapter.IndexCardAdapter.SectionView;

import com.vikaa.mycontact.R;

import bean.CardIntroEntity;
import config.CommonValue;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class MeCardAdapter extends BaseExpandableListAdapter{

	private ExpandableListView iphoneTreeView;
	
	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	
	static class SectionView {
		TextView titleView;
	}
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	public MeCardAdapter(ExpandableListView iphoneTreeView, Context context, List<List<CardIntroEntity>> cards) {
		this.iphoneTreeView = iphoneTreeView;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return cards.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return cards.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return cards.get(groupPosition).get(0).cardSectionType;
	}

	@Override
	public int getGroupCount() {
		return cards.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
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
		sect.titleView.setText(getGroup(groupPosition).toString());
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
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
		final CardIntroEntity model = cards.get(groupPosition).get(childPosition);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		if (model.cardSectionType.equals(CommonValue.CardSectionType.OwnedSectionType)) {
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((Me)context).showCardViewWeb(model);
				}
			});
			convertView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					((Me)context).cardSharePre(false, null, model);
					return false;
				}
			});
		}
		else if (model.cardSectionType.equals(CommonValue.CardSectionType.BarcodeSectionType)) {
			if (childPosition == 0) {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Me)context).showMyBarcode();
					}
				});
			}
			else {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Me)context).showScan();
					}
				});
			}
			convertView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return false;
				}
			});
		}
		return convertView;
	}
}
