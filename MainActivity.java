package com.jassoftware.picviewer;

import java.io.File;
import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;



public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }
   

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	File top;
    	File here;
    	ListView fList;
    	TextView loc;
    	Button up;
    	ArrayList<File> FListArray;
    	FileAdapter fAd;
    	
        public PlaceholderFragment() {
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
            File here = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            top = here;
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
					if (getHere()==getTop()){  //here is top
						//do nothing
					} else {  //not at top
						File h = getHere();
						setHere(h.getParentFile());
						File[] l = getHere().listFiles();
						FListArray.clear();
						for(int i=0;i<l.length;i++){
							FListArray.add(l[i]);
						}
						fAd.notifyDataSetChanged();
					}		
				}
            });
            fList.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int pos, long id) {
					setHere(FListArray.get(pos));
					File herev = getHere();
					if (herev.isDirectory()){  //open sub directory
						loc.setText(herev.getAbsolutePath());
						FListArray.clear();
						File[] list = herev.listFiles();
						for (int i=0;i<list.length;i++){
							FListArray.add(list[i]);
						}
						fAd.notifyDataSetChanged();
					} else {  //open pic view
						TextView vtype = (TextView)view.findViewById(R.id.type);
						String type = vtype.getText().toString();
						if (type.startsWith("pic")){
							//do open pic view stuff here
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
}
