package edu.hit.ict.ish.fileexplorer;

import java.io.File;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import edu.hit.ict.ish.Constants;
import edu.hit.ict.ish.R;
import edu.hit.ict.ish.MainActivity;


public class FileExplorerActivity extends ListActivity {
	private String mRoot = Environment.getExternalStorageDirectory().getPath();
	private File[] mFiles;

	private void refreshDirectory(String dirPath) {
		File f = new File(dirPath);

		// get all files in the dirPath
		File[] tmpFiles = f.listFiles();
		
		// first, sort all files! 
		Utils.sortFilesByName(tmpFiles);
		
		File[] files = null;
		// add the parent folder as a file at the top(index 0)
		boolean isRoot = dirPath.equals(mRoot);
		if (!isRoot) {
			files = new File[tmpFiles.length + 1];
			System.arraycopy(tmpFiles, 0, files, 1, tmpFiles.length);
			files[0] = new File(f.getParent());
		} else {
			files = tmpFiles;
		}

		mFiles = files;
		setListAdapter(new FileExplorerAdapter(this, files, isRoot));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_explorer_activity);
		refreshDirectory(mRoot);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		File file = mFiles[position];

		// if the file is a directory
		if (file.isDirectory()) {
			if (file.canRead())
				refreshDirectory(file.getAbsolutePath());
			else {
				Toast.makeText(FileExplorerActivity.this, "此文件夹不可读 ",
						Toast.LENGTH_LONG).show();
			}
		} else {
			// if the file is not a directory
			Intent intent = new Intent(FileExplorerActivity.this, MainActivity.class);
			intent.putExtra("FileName", file.getName());
			intent.putExtra("FilePath", file.getAbsolutePath());
			intent.putExtra("FileSize", String.valueOf(file.length()));
			setResult(RESULT_OK, intent);
			finish();

		}
	}
	


}
