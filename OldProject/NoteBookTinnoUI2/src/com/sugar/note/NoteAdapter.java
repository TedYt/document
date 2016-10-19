/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.sugar.note;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;

import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sugar.note.NotePad.Notes;

import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import android.widget.ImageView;
import android.util.Log;

import android.content.ContentResolver;
import android.content.ContentValues;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class NoteAdapter extends BaseAdapter {

     private Context mContext;
     public Cursor cur;
     private int mWhich;
     public ArrayList<NoteItem> list = new ArrayList<NoteItem>();
     public List<NoteItem> mylist = new ArrayList<NoteItem>(); 
     private Resources resource; 
     private ColorStateList colorWork; 
     private ColorStateList colorPersonal; 
     private ColorStateList colorFamily; 
     private ColorStateList colorStudy; 
     private String groupWork; 
     private String groupPersonal;
     private String groupFamily;
     private String groupStudy;

    private int mEditeMode;

     public NoteAdapter(Context context, Cursor cursor, int token) {
            mContext = context;
            cur = cursor;
            mWhich = token;
            setDataOfCurrentActivity();
     }

     private void setDataOfCurrentActivity(){
    	 resource = (Resources) mContext.getResources(); 
         colorWork = (ColorStateList) resource.getColorStateList(R.color.work); 
         colorPersonal = (ColorStateList) resource.getColorStateList(R.color.personal); 
         colorFamily = (ColorStateList) resource.getColorStateList(R.color.family); 
         colorStudy = (ColorStateList) resource.getColorStateList(R.color.study); 
         groupWork = (String) resource.getString(R.string.menu_work); 
         groupPersonal = (String) resource.getString(R.string.menu_personal);
         groupFamily = (String) resource.getString(R.string.menu_family);
         groupStudy = (String) resource.getString(R.string.menu_study);
     }

     public int getCount() {
         return cur.getCount();
     }

     public Object getItem(int position) {
         return list.get(position);
     }

     public long getItemId(int position) {
         return position;
     }

    /**
     * yutao
     * @param mode
     */
     public void setModeState(int mode){
         mEditeMode = mode;
     }

     public static class ViewClass{
    	 TextView title;
    	 TextView createTime;
    	// TextView groupColor;
    	// TextView notegroup;
    	 ImageView picture_icon;
         ImageView alert_icon;
         ImageView settop_icon;
         CheckBox mCheckbox;
     }
     public View getView(final int position, View convertView, ViewGroup parent) {
         ViewClass view;
         NoteItem item = list.get(position);
         if (convertView == null) {
             convertView = LayoutInflater.from(mContext).inflate(R.layout.noteslist_item_context, null);
             view = new ViewClass();
             view.title = (TextView) convertView.findViewById(R.id.title);
             view.createTime = (TextView) convertView.findViewById(R.id.create_time);
             view.picture_icon = (ImageView) convertView.findViewById(R.id.iv_picture_icon);
             view.alert_icon = (ImageView) convertView.findViewById(R.id.iv_alert_icon);
             view.settop_icon = (ImageView) convertView.findViewById(R.id.iv_settop);
             view.mCheckbox = (CheckBox) convertView.findViewById(R.id.checkbox);
             convertView.setTag(view);
         } else {
             view = (ViewClass) convertView.getTag();
         }

         view.title.setText(item.notetitle);

         view.createTime.setText(item.create_time);
         if (item.note.contains("__END_OF_PART__PHOTO__")) {
             view.picture_icon.setVisibility(View.VISIBLE);
         } else {
             view.picture_icon.setVisibility(View.GONE);
         }

         if (item.alertdate > 0) {
             view.alert_icon.setVisibility(View.VISIBLE);
         } else {
             view.alert_icon.setVisibility(View.GONE);
         }


         if (item.is_top == 1) {
             view.settop_icon.setVisibility(View.VISIBLE);
         } else {
             view.settop_icon.setVisibility(View.GONE);
         }

         if (item.itembgcol == 0) {
             convertView.setBackgroundResource(R.drawable.noteitem_blue_selector);
         } else if (item.itembgcol == 1) {
             convertView.setBackgroundResource(R.drawable.noteitem_green_selector);
         } else if (item.itembgcol == 2) {
             convertView.setBackgroundResource(R.drawable.noteitem_red_selector);
         } else if (item.itembgcol == 3) {
             convertView.setBackgroundResource(R.drawable.noteitem_yellow_selector);
         }

         if (mEditeMode == NotesList.NOTE_SETTOP_DELETE) {
             view.mCheckbox.setVisibility(View.VISIBLE);
             if (item.isselect) {
                 view.mCheckbox.setChecked(true);
             } else {
                 view.mCheckbox.setChecked(false);
             }

             view.mCheckbox.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     checkboxClickAction(position);
                 }
             });
         } else {
             view.mCheckbox.setVisibility(View.GONE);
         }


         return convertView;
     }


     public void checkboxClickAction(int position) {
         Resources resource = (Resources) mContext.getResources(); 
         String selected = (String) resource.getString(R.string.title_bar_selected); 
         NoteItem item = list.get(position);
         if (item.isselect) {
             item.isselect = false;
         } else {
             item.isselect = true;
         }
         int count = selectedNumber();
         NotePad.Notes.sDeleteNum = count;

         if (mEditeMode == NotesList.NOTE_SETTOP_DELETE){
             ((NotesList) mContext).setBottomButtonStatus();
         }
     }

     public void addList(NoteItem item) {
         list.add(item);   
     }

     public void deleteSelectedNote() {
         Log.e("tui", "deleteSelectedNote  begin");
         for (int i = 0; i < list.size(); i ++) {
             if (list.get(i).isselect) {
                 int noteId = list.get(i).id;
                 Uri muri = Uri.parse(Notes.CONTENT_URI + "/" + noteId);
                 String where =
                         NotePad.Notes._ID +                              
                         " = " +                                          
                         muri.getPathSegments().                           
                             get(NotePad.Notes.NOTE_ID_PATH_POSITION);
                 deleteSelectedNote_ImgFile(mContext.getContentResolver(), muri);
                 mContext.getContentResolver().delete(muri, where, null);
                 PublicUtils.cancelNoteAlarm(mContext, muri);
             }
         }
     }


     private boolean deleteSelectedNote_ImgFile(ContentResolver resolver, Uri noteUri) {
         String note_content = null;

         if (resolver == null) {
             return false;
         }

         if (noteUri == null) {
             return false;
         }

         Cursor cursor = resolver.query(noteUri,
                 new String[]{NotePad.Notes.COLUMN_NAME_NOTE},
                 null,
                 null,
                 null);

         try {
             if (cursor != null && cursor.moveToFirst()) {
                 note_content = cursor.getString(0);
             } else {
                 Log.e("tui", "deleteSelectedNote_ImgFile query no data!");
             }
         } catch (IndexOutOfBoundsException e) {
             Log.e("tui", "deleteSelectedNote_ImgFile query data failed:" + e.toString());
         } finally {
             if (cursor != null) {
                 cursor.close();
             }
         }

         if (note_content != null) {
             String[] items = note_content.split("__END_OF_PART__");
             int num = items.length;
             for (int i = 0; i < num; i++) {
                 Log.e("tui", "items[" + i + "]: " + items[i]);
             }

             for (int i = 1; i < num - 1; i = i + 2) {
                 String name = items[i];
                 if (name != null && name.startsWith("PHOTO__") && !name.equals("PHOTO__") && items[i + 1].equals("PHOTO__")) {
                     String photoName = name.substring(7);
                     File file = new File(NoteView.pathForNewCameraPhotoEx(photoName + ".jpg"));
                     if (file.exists()) {
                         file.delete();
                     }

                     file = new File(NoteView.pathForNewCameraPhotoEx(photoName + "_thum.jpg"));
                     if (file.exists()) {
                         file.delete();
                     }
                 }
             }
         }
         return true;
     }

     public void selectAllOrNoCheckbox(boolean userSelect) {
         Resources resource = (Resources) mContext.getResources();
         String selected = (String) resource.getString(R.string.title_bar_selected);
         for (int i = 0; i < cur.getCount(); i++) {
             list.get(i).isselect = userSelect;
         }

         if (userSelect) {
             NotePad.Notes.sDeleteNum = cur.getCount();
         } else {
             NotePad.Notes.sDeleteNum = 0;
         }
     }


     public int selectedNumber() {
         int count = 0;
         for (int i = 0; i < cur.getCount(); i ++) {
             if (list.get(i).isselect) {
                 count ++;
             }
         }
         NotePad.Notes.sDeleteNum = count;
         return count;
     }

     public void cancelSelected() {
         for (int i = 0; i < cur.getCount(); i ++) {
             if (list.get(i).isselect) {
                 list.get(i).isselect = false;
             }
         }
         NotePad.Notes.sDeleteNum = 0;
     }

     public boolean getSelectedIsAllTop() {
         boolean IsAllTop = true;
         boolean SelectFlag = false;

         if (list.size() == 0) {
             return false;
         }
         for (int i = 0; i < list.size(); i++) {
             if (list.get(i).isselect) {
                 if (list.get(i).is_top == 0) {
                     IsAllTop = false;
                     break;
                 }
                 SelectFlag = true;
             }
         }


         if (SelectFlag == false) {
             return false;
         }

         return IsAllTop;
     }

     public void NoteSetTop(ContentResolver resolver) {
         ArrayList<Integer> selectedlist = new ArrayList<Integer>();
         ArrayList<Integer> unselectedlist = new ArrayList<Integer>();

         for (int i = 0; i < list.size(); i++) {
             if (list.get(i).isselect) {
                 selectedlist.add(list.get(i).id);
             } else {
                 unselectedlist.add(list.get(i).id);
             }
         }

         for (int j = 0; j < selectedlist.size(); j++) {
             ContentValues values = new ContentValues();
             values.put(NotePad.Notes.SORT_INDEX, j + 1);
             values.put(NotePad.Notes.IS_TOP, 1);

             resolver.update(
                     NotePad.Notes.CONTENT_URI,
                     values,
                     NotePad.Notes._ID + "=?",
                     new String[]{String.valueOf(selectedlist.get(j))}
             );
         }

         int selected_count = selectedlist.size();
         for (int k = 0; k < unselectedlist.size(); k++) {
             ContentValues values = new ContentValues();
             int sort_index = selected_count + k + 1;

             values.put(NotePad.Notes.SORT_INDEX, sort_index);
             resolver.update(
                     NotePad.Notes.CONTENT_URI,
                     values,
                     NotePad.Notes._ID + "=?",
                     new String[]{String.valueOf(unselectedlist.get(k))}
             );
         }
     }


    public int getLastTopIndex() {
        int lasttopindex = 0;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).is_top == 0) {
                lasttopindex = list.get(i).sort_index - 1;
                break;
            }
        }

        return lasttopindex;
    }


   private void AddListToNotTop(ArrayList<NoteItem> nlist, ArrayList<NoteItem> clist) {
       for (int m = 0; m < clist.size(); m++) {
           long cancel_mdtime = clist.get(m).modify_time;
           boolean addFlag = false;
           int n = 0;

           for (n = 0; n < nlist.size(); n++) {
               long nottop_mdtime = nlist.get(n).modify_time;
               if (cancel_mdtime > nottop_mdtime) {
                   nlist.add(n, clist.get(m));
                   addFlag = true;
                   break;
               }
           }

           if (addFlag == false && n == nlist.size()) {
               //nlist.add(n, clist.get(m));
               nlist.add(clist.get(m));
           }
       }
   }

    public void NoteCancelTop(ContentResolver resolver) {
        ArrayList<Integer> istoplist = new ArrayList<Integer>();
        ArrayList<NoteItem> nottoplist = new ArrayList<NoteItem>();
        ArrayList<NoteItem> cancellist = new ArrayList<NoteItem>();


        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isselect && list.get(i).is_top == 1) {
                istoplist.add(list.get(i).id);
            } else if (list.get(i).isselect && list.get(i).is_top == 1) {
                cancellist.add(list.get(i));
                //CacelListID.add(list.get(i).id);
            } else {
                nottoplist.add(list.get(i));
            }
        }

        for (int m = 0; m < cancellist.size(); m++) {
            ContentValues values = new ContentValues();
            values.put(NotePad.Notes.IS_TOP, 0);

            resolver.update(
                    NotePad.Notes.CONTENT_URI,
                    values,
                    NotePad.Notes._ID + "=?",
                    new String[]{String.valueOf(cancellist.get(m).id)}
            );
        }

        if (nottoplist.size() == 0) {
            nottoplist.add(cancellist.get(0));
            cancellist.remove(0);
        }
        if (cancellist.size() > 0) {
            AddListToNotTop(nottoplist, cancellist);
        }

        for (int j = 0; j < istoplist.size(); j++) {
            ContentValues values = new ContentValues();
            values.put(NotePad.Notes.SORT_INDEX, j + 1);
            values.put(NotePad.Notes.IS_TOP, 1);

            resolver.update(
                    NotePad.Notes.CONTENT_URI,
                    values,
                    NotePad.Notes._ID + "=?",
                    new String[]{String.valueOf(istoplist.get(j))}
            );
        }

        int istop_count = istoplist.size();
        for (int k = 0; k < nottoplist.size(); k++) {
            ContentValues values = new ContentValues();
            int sort_index = istop_count + k + 1;

            values.put(NotePad.Notes.SORT_INDEX, sort_index);
            resolver.update(
                    NotePad.Notes.CONTENT_URI,
                    values,
                    NotePad.Notes._ID + "=?",
                    new String[]{String.valueOf(nottoplist.get(k).id)}
            );
        }
    }
}
