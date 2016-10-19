package com.android.contacts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.contacts.common.model.account.AccountType;
import com.android.internal.util.XmlUtils;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.util.Xml;

public class InitContactsReceiver extends BroadcastReceiver{
	static final String PARTNER_CONTACTS_PATH = "etc/contactsconf.xml";
	private HashMap<String, String> ContactInitMap;
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
		//store data flag to SharedPreferences named contactsflag
                SharedPreferences sharedPreferences = arg0.getSharedPreferences("contactsflag", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
                if(sharedPreferences.getBoolean("flag",false)){return;}
		editor.putBoolean("flag", true);
		editor.commit();
	        boolean flag=sharedPreferences.getBoolean("flag",true);
                if(arg1.getAction().equals("android.intent.action.BOOT_COMPLETED")&&flag){
		FileReader contactsReader = null;
        Log.d("mm","=======start====:");
		final File contactsFile = new File(Environment.getRootDirectory(),
		PARTNER_CONTACTS_PATH);
		try {
		        contactsReader = new FileReader(contactsFile);
		} catch (FileNotFoundException e) {
			Log.d("mm", "Can't open " +
			Environment.getRootDirectory() + "/" + PARTNER_CONTACTS_PATH);
			return ;
		}
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(contactsReader);
			XmlUtils.beginDocument(parser, "contacts");
			  while (true) {
			     XmlUtils.nextElement(parser);
			     String name = parser.getName();
				if (!"contact".equals(name)) {
					break;
				}
			      String contactname = parser.getAttributeValue(null, "name");
			      String phone = parser.getAttributeValue(null, "phone");
				Log.d("mm","====:");
                Log.d("mm","==contactname==:"+contactname);
				//ContactInitMap.put(contactname, phone);
				contactSt(contactname,phone,arg0);
			}
			 //return ContactInitMap;

		} catch (XmlPullParserException e) {
			Log.w("TAG", "Exception in contactsconf parser " + e);

		} catch (IOException e) {
			Log.d("TAG", "Exception in contactsconf parser " + e);

		}
		  // editor.putBoolean("flag", false);
       		//editor.commit();
	}
		
}		
       void contactSt(String name,String number,Context context){
			ContentValues values = new ContentValues();    
			values.put(RawContacts.ACCOUNT_NAME, "Local");
			values.put(RawContacts.ACCOUNT_TYPE, "LOCAL");
	        //inset RawContacts.CONTENT_URI (raw_contacts ), to get contacts'ID    
	        Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);    
	        Log.d("mm","rawContactUri====:"+rawContactUri);
	        //then return rawContactId , new contacts ID    
	        long rawContactId = ContentUris.parseId(rawContactUri);    
	            
	         /* Andorid  name phonenumber 
	          *   restore data table  insert twice
	          *  */           
	        //insert name
	        values.clear();    
	            
	        // raw_contacts_id    
	        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);               
	        // mimitype_id,describle the type,phoneNumber or Email....    
	        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);              
	        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);    
	        context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);    
	            
	        //insert data phoneNumber    
	        values.clear();   
	        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);              
	        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);     
	        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,number);    
	        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);    
	        context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values); 
	}					
	}

