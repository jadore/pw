package ui.adapter;

import java.util.List;
import com.vikaa.mycontact.R;

import bean.KeyValue;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CardViewAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<KeyValue> summarys;
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	public CardViewAdapter(Context context, List<KeyValue> summarys) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.summarys = summarys;
	}
	
	@Override
	public int getCount() {
		return summarys.size();
	}

	@Override
	public Object getItem(int arg0) {
		return summarys.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return summarys.get(arg0).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.card_view_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		KeyValue model = summarys.get(position);
		cell.titleView.setText(model.key);
		cell.desView.setText(Html.fromHtml(model.value));
		return convertView;
	}

	
}
