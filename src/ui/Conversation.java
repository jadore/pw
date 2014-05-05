package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.Logger;
import ui.adapter.ConversationAdapter;
import bean.CardIntroEntity;
import bean.FriendCardListEntity;

import com.vikaa.mycontact.R;

import config.CommonValue;
import db.manager.MessageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Conversation extends AppActivity implements OnEditorActionListener{
	private ListView mListView;
	private List<im.bean.Conversation> conversations;
	private ConversationAdapter conversationAdapter;
	
	private View searchHeaderView;
	private InputMethodManager imm;
	private EditText editText;
	private Button searchDeleteButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		initUI();
	}
	
	private void initUI() {
		searchHeaderView = getLayoutInflater().inflate(R.layout.search_headview, null);
		editText = (EditText) searchHeaderView.findViewById(R.id.searchEditView);
		editText.setHint("搜索");
		editText.setOnEditorActionListener(this);
		editText.addTextChangedListener(TWPN);
		searchDeleteButton = (Button) searchHeaderView.findViewById(R.id.searchDeleteButton);
		
		mListView = (ListView) findViewById(R.id.xlistview);
		mListView.addHeaderView(searchHeaderView);
		conversations = new ArrayList<im.bean.Conversation>();
		conversationAdapter = new ConversationAdapter(this, conversations);
		mListView.setAdapter(conversationAdapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		findMessage();
	}
	
	private void findMessage() {
		conversations = MessageManager.getInstance(this).getRecentContactsWithLastMsg();
//		conversationAdapter.notifyDataSetChanged();
		Logger.i(conversations.size()+"");
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			break;

		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onEditorAction(TextView arg0, int actionID, KeyEvent event) {
		switch(actionID){  
        case EditorInfo.IME_ACTION_SEARCH:  
            break;  
        }  
		return true;
	}
	
	TextWatcher TWPN = new TextWatcher() {
        private CharSequence temp;
        private int editStart ;
        private int editEnd ;
        public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                int arg3) {
            temp = s;
        }
       
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        	if (s.length() > 0) {
//            	searchDeleteButton.setVisibility(View.VISIBLE);
//            	String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
//        		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
//        		if(entity != null){
//        			if (entity.u.size() > 0) {
//        				List<CardIntroEntity> tempList = new ArrayList<CardIntroEntity>();
//                		for (CardIntroEntity friend : entity.u) {
//    						if (friend.realname.contains(s.toString()) ) {
//    							tempList.add(friend);
//    						}
//    					}
//                		if (tempList.size() > 0) {
////                			bilaterals.clear();
////                			bilaterals.addAll(tempList);
////    						lvDataState = UIHelper.LISTVIEW_DATA_FULL;
////    						mBilateralAdapter.notifyDataSetChanged();
//    					}
//        			}
//        		}
//        	}
//            else {
////            	searchDeleteButton.setVisibility(View.INVISIBLE);
////            	String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
////        		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
////        		if(entity != null){
////        			if (entity.u.size() > 0) {
////        				currentPage = 1;
////        				handleFriends(entity, UIHelper.LISTVIEW_ACTION_INIT);
////        			}
////        		}
            }
        }
       
		public void afterTextChanged(Editable s) {
            
		}
    };
}
