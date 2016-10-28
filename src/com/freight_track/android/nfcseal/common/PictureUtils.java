package com.freight_track.android.nfcseal.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PictureUtils {

	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaledDrawable(Activity a, String path,
			boolean trueSize) {
		Display display = a.getWindowManager().getDefaultDisplay();

		float destWidth;
		float destHeight;

		if (trueSize) {
			destWidth = display.getWidth();
			destHeight = display.getHeight();
		} else {
			destWidth = (float) 90.0;
			destHeight = (float) 90.0;
		}

		// read in the dimensions of the image on disk
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;

		int inSampleSize = 1;
		if (srcHeight > destHeight || srcWidth > destWidth) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round((float) srcHeight
						/ (float) destHeight);
			} else {
				inSampleSize = Math.round((float) srcWidth / (float) destWidth);
			}
		}

		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return new BitmapDrawable(a.getResources(), bitmap);
	}

	public static void cleanImageView(ImageView imageView) {
		if (!(imageView.getDrawable() instanceof BitmapDrawable))
			return;

		// Disable the codes below to omit errors caused by recycle.
		
		// clean up the view's image for the sake of memory
		// BitmapDrawable b = (BitmapDrawable)imageView.getDrawable();
		// b.getBitmap().recycle();
		// imageView.setImageDrawable(null);
	}

	public static void uploadPhoto(String filename, String filePath) {
		
//		Bitmap bitmap = resizeBitMapImage1(filePath, 800, 600);
		Bitmap bitmap = resizeBitMapImage1(filePath, 1024, 768);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("image_data", Base64.encodeToString(stream.toByteArray(), 0)));

		// image_str = null;
		try {
			stream.flush();
			stream.close();
			bitmap.recycle();
			nameValuePairs.add(new BasicNameValuePair("FileName", filename));

			String url = Utils.getUploadUrl();
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			
			Log.d("UnlockFragment", response.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Bitmap resizeBitMapImage1(String filePath, int targetWidth, int targetHeight) {
		Bitmap bitMapImage = null;
		try {
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			double sampleSize = 0;
			Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth
							- targetWidth);
			if (options.outHeight * options.outWidth * 2 >= 1638) {
				sampleSize = scaleByHeight ? options.outHeight / targetHeight : options.outWidth / targetWidth;
				sampleSize = (int) Math.pow(2d, Math.floor(Math.log(sampleSize) / Math.log(2d)));
			}
			options.inJustDecodeBounds = false;
			options.inTempStorage = new byte[128];
			while (true) {
				try {
					options.inSampleSize = (int) sampleSize;
					bitMapImage = BitmapFactory.decodeFile(filePath, options);
					break;
				} catch (Exception ex) {
					try {
						sampleSize = sampleSize * 2;
					} catch (Exception ex1) {

					}
				}
			}
		} catch (Exception ex) {

		}
		return bitMapImage;
	}

	public static void rotateImageWith90(ImageView iv) {
		Matrix matrix = new Matrix();
		float px = iv.getDrawable().getBounds().width()/2;
		float py = iv.getDrawable().getBounds().height()/2;
		matrix.postRotate(90f, px, py);
		iv.setScaleType(ScaleType.MATRIX);
		iv.setImageMatrix(matrix);
	}
	
	public static File createImageFile(String imageFileName) throws IOException {
		// Create an image file name
		
		if (imageFileName == null) {
			imageFileName = UUID.randomUUID().toString();
		}
		
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
		return imageF;
	}

	private static File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v("getAlbumDir", "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	/* Photo album for this application */
	private static String getAlbumName() {
		return "PicSample";
	}

	private static File getAlbumStorageDir(String albumName) {
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}

	public static void showPic(ImageView imageView, String photoPath) {

		int targetW = imageView.getWidth();
		int targetH = imageView.getHeight();

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(photoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

		imageView.setScaleType(ScaleType.CENTER);
		imageView.setImageBitmap(bitmap);

	}

	public static void showPic(ImageView imageView, String photoPath, int targetH, int targetW) {

		if (targetH == 0 && targetW == 0) {
			targetW = imageView.getWidth();
			targetH = imageView.getHeight();
		}

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(photoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.max(photoW / targetW, photoH / targetH);
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

		imageView.setScaleType(ScaleType.CENTER_CROP);
		imageView.setImageBitmap(bitmap);

	}

}
