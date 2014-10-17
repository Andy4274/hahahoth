package com.jassoftware.picviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
	int picsize;
	
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
		Log.v("FileAdapter.getView", "Called for file " + mList.get(pos).getName());
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
			holder.thumb.setImageResource(R.drawable.ic_dir);
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
				holder.thumb.setImageResource(R.drawable.ic_launcher);
				//picsize = holder.thumb.getWidth();
				DisplayMetrics metrics = new DisplayMetrics();
				WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
				wm.getDefaultDisplay().getMetrics(metrics);
				switch (metrics.densityDpi){
					case DisplayMetrics.DENSITY_LOW:
						picsize = 36;
						break;
					case DisplayMetrics.DENSITY_MEDIUM:
						picsize = 48;
						break;
					case DisplayMetrics.DENSITY_HIGH:
						picsize = 72;
						break;
					case DisplayMetrics.DENSITY_XHIGH:
						picsize = 96;
						break;
					case DisplayMetrics.DENSITY_XXHIGH:
						picsize = 144;
						break;
					case DisplayMetrics.DENSITY_XXXHIGH:
						picsize = 192;
						break;
					default:
						picsize = 512;
						break;
				}
				loadBitmap(f, holder.thumb);
				holder.thumb.setAdjustViewBounds(true);
				holder.thumb.setMaxHeight(picsize);
				holder.thumb.setMaxWidth(picsize);
			} else {
				holder.type.setText("file");
				holder.thumb.setImageResource(R.drawable.ic_file);
			}
		}		
		return cview;
	}

	class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private File data = null;
	    private Bitmap bm = null;
	    private String TAG = "BitmapWorkerTask";
	    
	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(File... params) {
	        data = params[0];
	        try{
	        		BitmapFactory.Options op = new BitmapFactory.Options();
	        		//op.inPurgeable = true;
	        		//op.inInputShareable = true;
	        		op.inJustDecodeBounds=true;
	        		//fis.mark(fis.available());
    				bm = BitmapFactory.decodeFile(data.getAbsolutePath(), op);
    				int width = op.outWidth;
    				int height = op.outHeight;
    				int inSampleSize = 1;
    			    if (height > picsize || width > picsize) {
    			        final int halfHeight = height / 2;
    			        final int halfWidth = width / 2;
    			        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
    			        // height and width larger than the requested height and width.
    			        while ((halfHeight / inSampleSize) > picsize
    			                && (halfWidth / inSampleSize) > picsize) {
    			            inSampleSize *= 2;
    			        }
    			    }
    			    op = new BitmapFactory.Options();
    			    op.inSampleSize = inSampleSize;
    			    //op.inJustDecodeBounds = false;
    			    Log.v(TAG, "Decoding "+data.getAbsolutePath());
    			    bm = BitmapFactory.decodeFile(data.getAbsolutePath(), op);	        
	        } catch (RuntimeException e){
	        	Log.e(TAG, e.getMessage());
	        }
	        return bm;
	    }

	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (isCancelled()) {
	            bitmap = null;
	        }

	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            final BitmapWorkerTask bitmapWorkerTask =
	                    getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	                imageView.setImageBitmap(bitmap);
	                Log.v("onPostExecute", "size: "+bitmap.getWidth()+" x "+bitmap.getHeight());
	                Log.v("onPostExecute", "size: "+imageView.getWidth()+" x "+imageView.getHeight());
	            }
	        }
	    }
	}
		
	public void loadBitmap(File f, ImageView imageView) {
		    if (cancelPotentialWork(f, imageView)) {
		        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		        Bitmap bm = BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher);
		        final AsyncDrawable asyncDrawable =
		                new AsyncDrawable(f, bm, task);
		        imageView.setImageDrawable(asyncDrawable);
		        task.execute(f);
		    }
		}

	public static boolean cancelPotentialWork(File data, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final File bitmapData = bitmapWorkerTask.data;
	        // If bitmapData is not yet set or it differs from the new data
	        if (bitmapData == null ||!(bitmapData.getName().equalsIgnoreCase(data.getName()))) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		   if (imageView != null) {
		       final Drawable drawable = imageView.getDrawable();
		       if (drawable instanceof AsyncDrawable) {
		           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
		           return asyncDrawable.getBitmapWorkerTask();
		       }
		    }
		    return null;
		}
	
	static class AsyncDrawable extends BitmapDrawable {
	    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(File f, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
	        super(bitmap);
	        bitmapWorkerTaskReference =
	            new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
}


