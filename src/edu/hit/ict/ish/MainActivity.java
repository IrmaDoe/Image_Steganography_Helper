package edu.hit.ict.ish;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;

import edu.hit.ict.ish.fileexplorer.FileExplorerActivity;

public class MainActivity extends Activity {
	private static final int THUMB_SIZE = 150;
	
	private static final int CAMERA_WITH_DATA = 3023;
	private static final int PHOTO_PICKED_WITH_DATA = 3021;
	private static final int FILE_PICKED_WITH_DATA = 3020;
	
	private static final int MAX_SECRET_MESSAGE_LENGTH = 100*1024; /*100 KB */
	private static final int MAX_SECRET_FILE_SIZE = 2*1024*1024; /*2 MB */
	
	private static final String IMAGE_STEG_HELPER = "ImageStegHelper";
	private static final String STEG_LIBRARY = "StegLibrary";
	private static final String EXTRACTED_FILES = "ExtractedFiles";

	private IWXAPI api;

	private String StegLibraryDir;
	private String ExtractedFilesDir;
	
	private ImageView ivTargetImage;
	private TextView tvStegStat;
	private EditText etSecretMessage;
	private Button btnStegMessage, btnStegFile, btnExtract;
	
	private Button btnSecretFile;
	private String mSecretFile = null;
	
	private String mTargetJpegFile = null;

	private JpegStegInfo stegInfo = new JpegStegInfo();
	private JpegFileInfo fileInfo = new JpegFileInfo();
	

	


	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.main_iv_target_image:
				Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
				break;
			
			case R.id.main_btn_secret_file:
				if(mTargetJpegFile != null) {
					if(stegInfo.stegStat == 0) {
						Intent intent1 = new Intent(MainActivity.this, FileExplorerActivity.class);
						startActivityForResult(intent1, FILE_PICKED_WITH_DATA);
					}
				} else {
					(Toast.makeText(MainActivity.this, "请先选择一个JPEG图片文件", Toast.LENGTH_LONG)).show();
				}
				break;
				
			case R.id.main_btn_steg_message:
				if(mTargetJpegFile != null) {
					if(stegInfo.stegStat == 0) {
						String secretMessage = etSecretMessage.getText().toString();
						long messageLength = 0;
						try {
							messageLength = secretMessage.getBytes("UTF-8").length;
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						if(messageLength >= MAX_SECRET_MESSAGE_LENGTH) {
							(Toast.makeText(MainActivity.this, "秘密消息的长度不能超过100KB", Toast.LENGTH_LONG)).show();							
						} else {
							String fileName = fileInfo.fileName;
							String newName = fileName;
							String newPath = StegLibraryDir + File.separator + fileName;
							File newFile = new File(newPath);
							int order = 2;
							while(newFile.exists()) {
								newName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + order + fileName.substring(fileName.lastIndexOf("."));
								order++;
								newPath = StegLibraryDir + File.separator + newName;
								newFile = new File(newPath);
							}
								
								
							//steg message
							int resultCode = stegJpegMessage(mTargetJpegFile, 
									etSecretMessage.getText().toString(),
									newPath);
							
							if(resultCode == 0) {
								(Toast.makeText(MainActivity.this, "隐写成功，图片保存到" + newPath , Toast.LENGTH_LONG)).show();
								
								//refresh TargetJpegFile
								mTargetJpegFile = newPath;

								//get Jpeg StegInfo
								stegInfo = analyzeJpegStegInfo(newPath, stegInfo);
								
								String stegStatName = null;
						        switch(stegInfo.stegStat) {
						        case 0:
						        	stegStatName = "None";
						        	tvStegStat.setText(stegStatName);
						        	tvStegStat.setTextColor(Color.rgb(0, 255, 0));
						        	break;
						        case 1:
						        	stegStatName = "Message";
						        	tvStegStat.setText(stegStatName);
						        	tvStegStat.setTextColor(Color.rgb(255, 0, 0));
						        	break;
						        case 2:
						        	stegStatName = "File";
						        	tvStegStat.setText(stegStatName);
						        	tvStegStat.setTextColor(Color.rgb(255, 0, 0));
						        	break;
						        }
								
								
								//get Jpeg FileInfo
								fileInfo.fileName = newName;
								fileInfo.filePath = newPath;
								fileInfo.fileSize = String.valueOf(newFile.length());
								
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inJustDecodeBounds = true;
								BitmapFactory.decodeFile(newPath, options);
								
								fileInfo.width = options.outWidth;
								fileInfo.height = options.outHeight;
								
								//set ivTargetImage
								options.inJustDecodeBounds = false;
								options.inSampleSize = 2;
						        Bitmap bitmap = BitmapFactory.decodeFile(newPath, options);
									
								ivTargetImage.setImageBitmap(bitmap);
				
								
								//set etSecretMessage
								etSecretMessage.setText(null);
								 
								
								//set btnSecretFile
								mSecretFile = null;
								btnSecretFile.setText("这里是秘密文件...");
								
								
												
							} else if(resultCode == -1) {
								(Toast.makeText(MainActivity.this, "隐写失败，秘密消息的长度过长", Toast.LENGTH_LONG)).show();
							}
						}
						
					} else {
						(Toast.makeText(MainActivity.this, "此图片已经隐写了数据", Toast.LENGTH_LONG)).show();
					}
				} else {
					(Toast.makeText(MainActivity.this, "请先选择一个JPEG图片文件", Toast.LENGTH_LONG)).show();
				}
				break;
			case R.id.main_btn_steg_file:
				if(mTargetJpegFile != null) {
					if(stegInfo.stegStat == 0) {
						if(mSecretFile != null) {
							File secretFile = new File(mSecretFile);
							if(secretFile.length() >= MAX_SECRET_FILE_SIZE) {
								(Toast.makeText(MainActivity.this, "秘密文件的大小不能超过2MB", Toast.LENGTH_LONG)).show();
							} else {
								String fileName = fileInfo.fileName;
								String newName = fileName;
								String newPath = StegLibraryDir + File.separator + fileName;
								File newFile = new File(newPath);
								int order = 2;
								while(newFile.exists()) {
									newName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + order + fileName.substring(fileName.lastIndexOf("."));
									order++;
									newPath = StegLibraryDir + File.separator + newName;
									newFile = new File(newPath);
								}
								
								//steg File
								int resultCode = stegJpegFile(mTargetJpegFile, 
										mSecretFile,
										newPath);
								
								switch(resultCode){
								case 0:
									(Toast.makeText(MainActivity.this, "隐写成功，图片保存到" + newPath , Toast.LENGTH_LONG)).show();
									
									//refresh TargetJpegFile
									mTargetJpegFile = newPath;

									//get Jpeg StegInfo
									stegInfo = analyzeJpegStegInfo(newPath, stegInfo);
									
									String stegStatName = null;
							        switch(stegInfo.stegStat) {
							        case 0:
							        	stegStatName = "None";
							        	tvStegStat.setText(stegStatName);
							        	tvStegStat.setTextColor(Color.rgb(0, 255, 0));
							        	break;
							        case 1:
							        	stegStatName = "Message";
							        	tvStegStat.setText(stegStatName);
							        	tvStegStat.setTextColor(Color.rgb(255, 0, 0));
							        	break;
							        case 2:
							        	stegStatName = "File";
							        	tvStegStat.setText(stegStatName);
							        	tvStegStat.setTextColor(Color.rgb(255, 0, 0));
							        	break;
							        }
									
									
									//get Jpeg FileInfo
									fileInfo.fileName = newName;
									fileInfo.filePath = newPath;
									fileInfo.fileSize = String.valueOf(newFile.length());
									
									BitmapFactory.Options options = new BitmapFactory.Options();
									options.inJustDecodeBounds = true;
									BitmapFactory.decodeFile(newPath, options);
									
									fileInfo.width = options.outWidth;
									fileInfo.height = options.outHeight;
									
									//set ivTargetImage
									options.inJustDecodeBounds = false;
									options.inSampleSize = 2;
							        Bitmap bitmap = BitmapFactory.decodeFile(newPath, options);
										
									ivTargetImage.setImageBitmap(bitmap);
									
									//set etSecretMessage
									etSecretMessage.setText(null);

									//set btnSecretFile
									mSecretFile = null;
									btnSecretFile.setText("这里是秘密文件...");
									
									break;
								case -1:
									(Toast.makeText(MainActivity.this, "隐写失败，秘密文件过大", Toast.LENGTH_LONG)).show();
									break;
								case -2:
									(Toast.makeText(MainActivity.this, "此图片已经隐写了数据", Toast.LENGTH_LONG)).show();
									break;
								case -3:
									(Toast.makeText(MainActivity.this, "隐写失败，无法打开秘密文件", Toast.LENGTH_LONG)).show();
									break;
								}
								
								
							}
						} else {
							(Toast.makeText(MainActivity.this, "请先选择一个秘密文件", Toast.LENGTH_LONG)).show();
						}
					} else {
						(Toast.makeText(MainActivity.this, "此图片已经隐写了数据", Toast.LENGTH_LONG)).show();
					}
				} else {
					(Toast.makeText(MainActivity.this, "请先选择一个JPEG图片文件", Toast.LENGTH_LONG)).show();
				}
				break;
				
			case R.id.main_btn_extract:
				if(mTargetJpegFile != null) {
					if(stegInfo.stegStat == 0) {
						(Toast.makeText(MainActivity.this, "此图片未隐写数据", Toast.LENGTH_LONG)).show();
					} else if(stegInfo.stegStat == 1) {
						etSecretMessage.setText(extractJpegMessage(mTargetJpegFile));
						 
						(Toast.makeText(MainActivity.this, "提取成功", Toast.LENGTH_LONG)).show();
					} else if(stegInfo.stegStat == 2) {
						
						String secretFilename = extractJpegFile(mTargetJpegFile, ExtractedFilesDir);
						String secretFilePath = ExtractedFilesDir + File.separator + secretFilename;
						(Toast.makeText(MainActivity.this, "提取成功，秘密文件保存到" + secretFilePath, Toast.LENGTH_LONG)).show();

						File secretFile = new File(secretFilePath);
						
						//set btnSecretFile
						btnSecretFile.setText(secretFilename + ", " + secretFile.length() + " B");

						
					}
				} else {
					(Toast.makeText(MainActivity.this, "请先选择一个JPEG图片文件", Toast.LENGTH_LONG)).show();
				}
				break;
			}
			

		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_PICKED_WITH_DATA) {
			if (resultCode == RESULT_OK) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				
				Uri uri = data.getData();
				
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				String imgNo = cursor.getString(0); // 图片编号
				String imgPath = cursor.getString(1); // 图片文件路径
				String imgSize = cursor.getString(2); // 图片大小
				String imgName = cursor.getString(3); // 图片文件名
				cursor.close();
				
				//check the file extension
				String fileName = imgName.toLowerCase();
				if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
					
					//set mTargetJpegFile
					mTargetJpegFile = imgPath;
					
					//get Jpeg StegInfo
					stegInfo = analyzeJpegStegInfo(imgPath, stegInfo);
					
					String stegStatName = null;
			        switch(stegInfo.stegStat) {
			        case 0:
			        	stegStatName = "None";
			        	tvStegStat.setText(stegStatName);
			        	tvStegStat.setTextColor(Color.rgb(0, 255, 0));
			        	break;
			        case 1:
			        	stegStatName = "Message";
			        	tvStegStat.setText(stegStatName);
			        	tvStegStat.setTextColor(Color.rgb(255, 0, 0));
			        	break;
			        case 2:
			        	stegStatName = "File";
			        	tvStegStat.setText(stegStatName);
			        	tvStegStat.setTextColor(Color.rgb(255, 0, 0));
			        	break;
			        }
					
					
					//get Jpeg FileInfo
					fileInfo.fileName = imgName;
					fileInfo.filePath = imgPath;
					fileInfo.fileSize = imgSize;
					
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(imgPath, options);
					
					fileInfo.width = options.outWidth;
					fileInfo.height = options.outHeight;
					
					//set ivTargetImage
					options.inJustDecodeBounds = false;
					options.inSampleSize = 2;
			        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
						
					ivTargetImage.setImageBitmap(bitmap);
					
					//set etSecretMessage
					etSecretMessage.setText(null);

					//set btnSecretFile
					mSecretFile = null;
					btnSecretFile.setText("这里是秘密文件...");

					
				} else {
					(Toast.makeText(MainActivity.this, "请选择一个.jpg图片文件", Toast.LENGTH_LONG)).show();
				}
				
				

			}
		} else if(requestCode == FILE_PICKED_WITH_DATA) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				
				String filePath = bundle.getString("FilePath");
				String fileName = bundle.getString("FileName");
				String fileSize = bundle.getString("FileSize");
				
				//set mSecretFile
				mSecretFile = filePath;
				
				//set btnSecretFile
				btnSecretFile.setText(fileName + ", " + fileSize + " B");
				
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// create WXAPI and register!! App
		api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID, true);
		api.registerApp(Constants.WX_APP_ID);

		// set widgets
		ivTargetImage = (ImageView) findViewById(R.id.main_iv_target_image);
		ivTargetImage.setOnClickListener(listener);
		tvStegStat = (TextView) findViewById(R.id.main_tv_stegstat);
		
		etSecretMessage = (EditText) findViewById(R.id.main_et_secret_message);

		btnSecretFile = (Button) findViewById(R.id.main_btn_secret_file);
		btnSecretFile.setOnClickListener(listener);
		btnStegMessage = (Button) findViewById(R.id.main_btn_steg_message);
		btnStegMessage.setOnClickListener(listener);
		btnStegFile = (Button) findViewById(R.id.main_btn_steg_file);
		btnStegFile.setOnClickListener(listener);
		btnExtract = (Button) findViewById(R.id.main_btn_extract);
		btnExtract.setOnClickListener(listener);
		
		// mkdirs
		StegLibraryDir = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + IMAGE_STEG_HELPER
				+ File.separator + STEG_LIBRARY;//+ File.separator;
		File file1 = new File(StegLibraryDir);
		if(!file1.exists()) {
			file1.mkdirs();
		}
		ExtractedFilesDir = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + IMAGE_STEG_HELPER
				+ File.separator + EXTRACTED_FILES;// + File.separator;
		File file2 = new File(ExtractedFilesDir);
		if(!file2.exists()) {
			file2.mkdirs();
		}
		
		//
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

		switch (item.getItemId()) {
        	case R.id.action_sharetoWX:
        		if(mTargetJpegFile != null) {
	        		String path = mTargetJpegFile;
					File file = new File(path);
	
					WXImageObject imgObj = new WXImageObject();
					imgObj.setImagePath(path);
					
					WXMediaMessage msg = new WXMediaMessage();
					msg.mediaObject = imgObj;
					
					Bitmap bmp = BitmapFactory.decodeFile(path);
					Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
					bmp.recycle();
					msg.thumbData = bmpToByteArray(thumbBmp, true);
					
					SendMessageToWX.Req req = new SendMessageToWX.Req();
					req.transaction = "img" + System.currentTimeMillis();
					req.message = msg;
					req.scene = SendMessageToWX.Req.WXSceneSession; //微信好友
					api.sendReq(req);
        		} else {
					(Toast.makeText(MainActivity.this, "请先选择一个JPEG图片文件", Toast.LENGTH_LONG)).show();
				}
        		break;
        		
        	case R.id.action_showpicinfo:
        		PicInfoDialog picInfoDialog = new PicInfoDialog(MainActivity.this, fileInfo, stegInfo);
        		picInfoDialog.show();
        		break;
        		
        	case R.id.action_showFAQ:
        		break;
        		
        	case R.id.action_showabout:
        		Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        		startActivity(intent);
        		break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public native JpegStegInfo analyzeJpegStegInfo(String inputFile, JpegStegInfo info);
	public native int stegJpegMessage(String inputFile, String secretMessage, String outputFile);
	public native String extractJpegMessage(String inputFile);
	public native int stegJpegFile(String inputFile, String secretFile, String outputFile);
	public native String extractJpegFile(String inputFile, String savePath);

	static {
		// load libjsfc4ish.so
		// (JPEG Steganography Framework in C For Image Steganography Helper)
		System.loadLibrary("jsfc4ish");
	}

}

