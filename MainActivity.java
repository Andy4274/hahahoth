package com.jassoftware.picviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;



public class MainActivity extends ActionBarActivity {

	static ViewFragment v;
	static BrowserFragment b;
	android.support.v4.app.FragmentManager fm;
	File PicToOpen;
	File Location;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
        	v = new ViewFragment();
        	b = new BrowserFragment();
        	fm = getSupportFragmentManager();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, b)
                    .commit();
        }
    }
   
    public void SwitchToBrowser(){
    	b = new BrowserFragment();
    	fm.beginTransaction()
    		.detach(v)
    		.add(R.id.container, b)
    		.commit();
    }
    
    public void SwitchToView(){
    	v = new ViewFragment();
    	fm.beginTransaction()
		.detach(b)
		.add(R.id.container, v)
		.commit();
    }
    
    public class BrowserFragment extends Fragment {
    
    	File top;
    	File here;
    	ListView fList;
    	TextView loc;
    	Button up;
    	ArrayList<File> FListArray;
    	FileAdapter fAd;
    	
        public BrowserFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //find things
            up = (Button)rootView.findViewById(R.id.up);
            loc = (TextView)rootView.findViewById(R.id.location);
            fList = (ListView)rootView.findViewById(R.id.filelist);
            
            //set up array and adapter and set location
            FListArray = new ArrayList<File>();
            if (Location == null){
            	here = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            	top = here;
            } else {
            	here = Location;
            }
            File[] dir = here.listFiles();
            loc.setText(here.getAbsolutePath());
            for (int i = 0; i<dir.length;i++){
            	FListArray.add(dir[i]);
            }
            fAd = new FileAdapter(getActivity(), FListArray);
            fList.setAdapter(fAd);
            //set up Buttons
            up.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
						File h = getHere().getParentFile();
						if (h!=null){
							setHere(h);
							loc.setText(h.getAbsolutePath());
							File[] l = h.listFiles();
							FListArray.clear();
							if (l != null){
								for(int i=0;i<l.length;i++){
									FListArray.add(l[i]);
								}
							}
							fAd.notifyDataSetChanged();
						}		
				}
            });
            fList.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int pos, long id) {
					File herev = FListArray.get(pos);
					if (herev.isDirectory()){  //open sub directory
						setHere(herev);
						Location = herev;
						loc.setText(herev.getAbsolutePath());
						FListArray.clear();
						File[] list = herev.listFiles(); 
						if (list != null){
							for (int i=0;i<list.length;i++){
								FListArray.add(list[i]);
							}
						}
						fAd.notifyDataSetChanged();
						Log.v("OnItemClick", "entering dir:"+herev.getName());
					} else {  //open pic view
						TextView vtype = (TextView)view.findViewById(R.id.type);
						String type = vtype.getText().toString();
						if (type.startsWith("pic")){
							PicToOpen = herev;
							SwitchToView();
						}
					}  //is a file but not a pic:  do nothing					
				}
            });
            return rootView;
        }
        public void setHere(File f){
        	here = f;
        }
        public File getHere(){
        	return here;
        }
        public File getTop(){
        	return top;
        }
    }
    
    public  class ViewFragment extends Fragment{
    	
    	TextView name;
    	ImageView pic;
    	Button back;
    	File src;
    	int width;
    	int height;
    	
    	public ViewFragment(){
    		src =PicToOpen;
    	}
    	
    	public int getWidth(){
        	return width;
        }
        public int getHeight(){
        	return height;
        }
        
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.viewpicfragment, container, false);
            //find things
            back = (Button)rootView.findViewById(R.id.back);
            name = (TextView)rootView.findViewById(R.id.name);
            pic = (ImageView)rootView.findViewById(R.id.pic);
            //set data
            src = PicToOpen;
            name.setText(src.getName());
            pic.setImageResource(R.drawable.ic_launcher);
            new LoadDrawable().execute(src.getAbsolutePath()+"/"+src.getName());
            //set buttons
            back.setOnClickListener(new OnClickListener(){
            	@Override
            	public void onClick(View v){
            		SwitchToBrowser();
            	}
            });
            width = rootView.getWidth();
            height = rootView.getHeight();
            return rootView;
    	}
    	
    	public class LoadDrawable extends AsyncTask<String, Void, Bitmap> {

    		@Override
            protected void onPostExecute(Bitmap result) {
    			if (result!=null){
    				pic.setImageBitmap(result);
    				//Toast t;
    				//t = Toast.makeText(getBaseContext(), "Done Loading", Toast.LENGTH_SHORT);
    				//t.show();
    			}
            }

			@Override
    		protected Bitmap doInBackground(String... arg0) {
    			Bitmap pic = null;
    			FileInputStream is = null;
    			try {
    				is = new FileInputStream(src);
				} catch (FileNotFoundException e) {
					Log.e("LoadDrawable Open FileInputStream", e.getMessage());
				}
    			if (is!=null){
    				pic = BitmapFactory.decodeStream(is);
    			}
    			
    			try {
					if (is!=null){
						is.close();
					}
				} catch (IOException e) {
					Log.e("LoadDrawable Close FileInputStream", e.getMessage());
				} 
    			return pic;
    		}

			
		}//LoadDrawable
    }//ViewFragment
}
