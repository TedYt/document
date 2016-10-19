package com.sugar.note;

import android.os.Parcel;
import android.os.Parcelable;


public class NoteItem implements Parcelable {
       public int id;
       public String note;
       public String create_time;
       public boolean isselect;
       public String notegroup;
       public long modify_time;
       public int itembgcol;//add by yangkui  20140403 add the noteitemcol
       public long alertdate;//add by yangkui 20140416 add for alerticon
       public String notetitle;//add by yangkui 20140417 for itemtitle
       public int is_top;
       public int sort_index;

       public NoteItem()
       {

       }

	   public NoteItem(Parcel in)
	   {
                 id = in.readInt();
		 note = in.readString();
		 create_time = in.readString();
		 isselect = in.readByte() == 1;
		 notegroup = in.readString();
		 modify_time = in.readLong();
		 itembgcol = in.readInt();
		 alertdate = in.readLong();
		 notetitle = in.readString();
		 is_top = in.readInt();
		 sort_index = in.readInt();
	   }

	   public int describeContents() {
                 return 0;
          }

          public void writeToParcel(Parcel dest, int flags) {
               dest.writeInt(id);
               dest.writeString(note);
	       dest.writeString(create_time);
               dest.writeByte(isselect ? (byte)1 : (byte)0);
	       dest.writeString(notegroup);
	       dest.writeLong(modify_time);
	       dest.writeInt(itembgcol);
	       dest.writeLong(alertdate);
	       dest.writeString(notetitle);
	       dest.writeInt(is_top);
	       dest.writeInt(sort_index);
          }

          public static final Parcelable.Creator<NoteItem> CREATOR = new Parcelable.Creator<NoteItem>() {
                public NoteItem createFromParcel(Parcel in) {
                      return new NoteItem(in);
               }

               public NoteItem[] newArray(int size) {
                     return new NoteItem[size];
              }
         };
}
