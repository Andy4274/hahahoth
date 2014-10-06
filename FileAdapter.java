package com.jassoftware.picviewer;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {

	static class ViewHolder{
		ImageView thumb;
		TextView name;
		TextView type;
	}
	LayoutInflater inflater;
	ArrayList<File> mList; 
	Context c;
	
	public FileAdapter(Context context, ArrayList<File> list){
		c = context;
		mList = list;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int pos) {
		return mList.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View cview, ViewGroup parent) {
		ViewHolder holder;
		if (cview==null){
			holder = new ViewHolder();
			inflater = ((Activity) c).getLayoutInflater();
			cview = inflater.inflate(R.layout.filelistitem, parent, false);
			holder.thumb = (ImageView)cview.findViewById(R.id.thumb);
			holder.name = (TextView)cview.findViewById(R.id.name);
			holder.type = (TextView)cview.findViewById(R.id.type);
			cview.setTag(holder);
		} else {
			holder = (ViewHolder)cview.getTag();
		}
		File f = mList.get(pos);
		holder.name.setText(f.getName());
		if (f.isDirectory()){
			holder.type.setText("dir");
		} else {
			
			String sepchar = ".";
			int sep = f.getName().indexOf(sepchar);
			String suffix = f.getName().substring(sep+1);
			String[] picTypes = {"gif","jpeg","jpg","png","bmp"};
			boolean isPic = false;
			for (int i = 0;i<picTypes.length;i++){
				if (suffix.equalsIgnoreCase(picTypes[i])){
					isPic = true;
				}
			}
			if (isPic){
				holder.type.setText("pic: "+suffix);
			} else {
				holder.type.setText("file");
			}
		}		
		return cview;
	}

}

