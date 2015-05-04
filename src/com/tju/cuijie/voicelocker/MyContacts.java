package com.tju.cuijie.voicelocker;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

public class MyContacts {
	private static Context CONTACT_CONTEXT;
	public static Map<String,String> CONTACT_LIST = new HashMap<String, String>();
	private static final String[] PHONES_PROJECTION = new String[] { Phone.DISPLAY_NAME, Phone.NUMBER, Phone.CONTACT_ID };  
	private int nameIndex;
	private int numberIndex;
	private String contactName;
	private String phoneNumber;
	
	public MyContacts (Context context) {
		CONTACT_CONTEXT = context;
	}
	public void getPhoneContacts() {
		ContentResolver resolver = CONTACT_CONTEXT.getContentResolver();  
		
		// 获取手机联系人  
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);  
		
		if (phoneCursor != null) {  
			
			while (phoneCursor.moveToNext()) {
				
				nameIndex = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

		        phoneNumber = phoneCursor.getString(numberIndex);
				contactName = phoneCursor.getString(nameIndex);  
				
				//当手机号码为空的或者为空字段 跳过当前循环  
				if ( TextUtils.isEmpty(phoneNumber) ) {
					continue;
				}
				CONTACT_LIST.put(contactName, phoneNumber);
			}  
			phoneCursor.close();
		}
	}
}