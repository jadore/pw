package ui.adapter;

import java.util.List;
import com.squareup.picasso.Picasso;
import com.vikaa.mycontact.R;

import bean.CardIntroEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendCardSearchAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	public FriendCardSearchAdapter(Context context, List<CardIntroEntity> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int position) {
		return cards.get(position);
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
		Picasso.with(context)
        .load(model.avatar)
        .placeholder(R.drawable.avatar_placeholder)
        .error(R.drawable.avatar_placeholder)
        .resize(50, 50)
        .centerCrop()
        .into(cell.avatarImageView);
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
