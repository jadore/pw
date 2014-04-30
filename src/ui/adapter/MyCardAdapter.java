package ui.adapter;

import java.util.List;

import ui.MyCard;
import com.vikaa.mycontact.R;

import bean.CardIntroEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyCardAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	public MyCardAdapter(Context context, List<CardIntroEntity> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
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
		return cards.get(arg0).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
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
		final CardIntroEntity model = cards.get(position);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((MyCard)context).showCardViewWeb(model);
			}
		});
		return convertView;
	}

}
