package edu.hit.ict.ish;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PicInfoDialog extends AlertDialog {
    private Context mContext;
	private JpegFileInfo mFileInfo;
	private JpegStegInfo mStegInfo;
    private View mView;

    public PicInfoDialog(Context context, JpegFileInfo fileInfo, JpegStegInfo stegInfo) {
        super(context);
        mContext = context;
        mFileInfo = fileInfo;
        mStegInfo = stegInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.picinfo_dialog, null);

        setTitle(mFileInfo.fileName);

        ((TextView) mView.findViewById(R.id.picinfo_tv_filename)).setText(mFileInfo.fileName);
        ((TextView) mView.findViewById(R.id.picinfo_tv_filepath)).setText(mFileInfo.filePath);
        ((TextView) mView.findViewById(R.id.picinfo_tv_filesize)).setText(mFileInfo.fileSize + " byte");
        ((TextView) mView.findViewById(R.id.picinfo_tv_width)).setText(String.valueOf(mFileInfo.width) + " px");
        ((TextView) mView.findViewById(R.id.picinfo_tv_height)).setText(String.valueOf(mFileInfo.height) + " px");
        
        String stegStatName = null;
        switch(mStegInfo.stegStat) {
        case 0:
        	stegStatName = "None";
        	break;
        case 1:
        	stegStatName = "Message";
        	break;
        case 2:
        	stegStatName = "File";
        	break;
        }
        ((TextView) mView.findViewById(R.id.picinfo_tv_stegstat)).setText(stegStatName);
        ((TextView) mView.findViewById(R.id.picinfo_tv_capacity)).setText(String.valueOf(mStegInfo.capacity) + " byte");
        ((TextView) mView.findViewById(R.id.picinfo_tv_secretsize)).setText(String.valueOf(mStegInfo.secretSize) + " byte");

        int usageRateInt = (int) (mStegInfo.usageRate * 100);
        ((TextView) mView.findViewById(R.id.picinfo_tv_usagerate)).setText(String.valueOf(usageRateInt) + " %");

        setView(mView);
        setButton(BUTTON_NEGATIVE, "OK", (DialogInterface.OnClickListener) null);

        super.onCreate(savedInstanceState);
    }
    
}
