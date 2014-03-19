package im.bean;


import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class IMMessage implements Comparable<IMMessage>, Serializable{
	
	public interface JSBubbleMessageStatus {
	    int JSBubbleMessageStatusReaded = 0;
	    int JSBubbleMessageStatusReceiving = 1;
	    int JSBubbleMessageStatusDelivering = 2;
	    int JSBubbleMessageStatusNormal = 3;
	} 

	public interface JSBubbleMessageType {
		int JSBubbleMessageTypeIncoming = 0;
		int JSBubbleMessageTypeOutgoing = 1;
	} 

	public interface JSBubbleMediaType {
		int JSBubbleMediaTypeText = 0;
		int JSBubbleMediaTypeImage = 1;
		int JSBubbleMediaTypeVoice = 2;
		int JSBubbleMediaTypeVideo = 3;
	}
	
	public static final String IMMESSAGE_KEY = "immessage.key";
	public static final String KEY_TIME = "immessage.time";
	
	public String content;
	public String msgTime;
	public String openId;
	public String roomId;
	public int msgType = 0;//0:接受 1：发送
	public int msgStatus;
	public int mediaType;
	
	//cursor
	public String chatId;
	public String postAt;
	
	@Override
	public int compareTo(IMMessage oth) {
		if (null == this.msgTime || null == oth.msgTime) {
			return 0;
		}
		String time1 = "";
		String time2 = "";
		time1 = this.msgTime;
		time2 = oth.msgTime;
		return time1.compareTo(time2);
	}
}
