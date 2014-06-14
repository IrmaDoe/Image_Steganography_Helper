package edu.hit.ict.ish.fileexplorer;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.hit.ict.ish.R;

public class FileExplorerAdapter extends BaseAdapter {
	private File[] mFiles;
	private LayoutInflater mInflater;
	private Context mContext;
	private boolean mIsRoot;
	
	public FileExplorerAdapter(Context context, File[] files, boolean isRoot) {
		mFiles = files;
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mIsRoot = isRoot;
	}

	@Override
	public int getCount() {
		return mFiles.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
		ViewHolder holder = null;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_explorer_item, null);

            // Creates a ViewHolder and store references to the children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.file_exp_item_iv_icon);
            holder.tv1 = (TextView) convertView.findViewById(R.id.file_exp_item_tv_1);
            holder.tv2 = (TextView) convertView.findViewById(R.id.file_exp_item_tv_2);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

		// Bind the data efficiently with the holder :
        File file = mFiles[position];
        
        // the parent folder!
		if(!mIsRoot && (position == 0) ) {
			holder.tv1.setText("..");
			holder.ivIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_up_level));
			
		} else { // the normal file or folder
			
	        //show file's name
			holder.tv1.setText(file.getName());
			
			//set file icon by it's file type, or it is a folder
			if (file.isDirectory()) {
				holder.ivIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_folder));
			} 
			else {
				Drawable d = null;
				if(Utils.checkExtension(file)) {
					d = mContext.getResources().getDrawable(R.drawable.icon_file_jpeg);
				}
				else {
					d = mContext.getResources().getDrawable(R.drawable.icon_file_default);
				}
				holder.ivIcon.setImageDrawable(d);
				holder.tv2.setText(file.length() + " B");
			}
			
		}
		
        return convertView;
	}

}
