package contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MobileSynBean implements Serializable{
	public String prefix;
	public String suffix;
	public String firstname;
	public String middlename;
	public String lastname;
	
	public String organization ;
	public String department ;
	public String jobtitle ;
	public String birthday ;
	public List<AddressBean> address = new ArrayList<AddressBean>();
	public List<DateBean> dates = new ArrayList<DateBean>();
	public List<PhoneBean> phone = new ArrayList<PhoneBean>();
	public List<UrlBean> url = new ArrayList<UrlBean>();
	public List<IMBean> im = new ArrayList<IMBean>();
	public List<EmailBean> email = new ArrayList<EmailBean>();
	public String note ;
	
}
