package comp5216.sydney.edu.au.mediaaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.core.content.FileProvider;
public class MainActivity extends Activity {
    public final String APP_TAG = "MobileComputingTutorial";
    public String photoFileName = "photo.jpg";
    public String videoFileName = "video.mp4";

    //request codes
    private static final int MY_PERMISSIONS_REQUEST_OPEN_CAMERA = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_VIDEO = 103;
    private static final int MY_PERMISSIONS_REQUEST_READ_VIDEOS = 104;

    private ImageView picture;
    private VideoView video;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
    private File file;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTakePhotoClick(View v) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()){
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            photoFileName = "IMG_" + timeStamp + ".jpg";
            // Create a photo file reference
            Uri file_uri = getFileUri(photoFileName,0);
            // Add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OPEN_CAMERA);
            }
        }
    }
    public void onRecordVideoClick(View v) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to capture a video and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            videoFileName = "VIDEO_" + timeStamp + ".mp4";
            // Create a video file reference
            Uri file_uri = getFileUri(videoFileName,1);
            // add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
            // Start the video record intent to capture video
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_RECORD_VIDEO);
        }
    }

    // Returns the Uri for a photo/media stored on disk given the fileName and type
    public Uri getFileUri(String fileName, int type) {
        Uri fileUri = null;
        try {
            String typestr = "images"; //default to images type
            if (type == 1) {
                typestr = "videos";
            } else if (type != 0) {
                typestr = "audios";
            }
            File mediaStorageDir = new
                    File(getExternalFilesDir(Environment.getExternalStorageDirectory().toString()), APP_TAG);
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "failed to create directory");
            }
            // Create the file target for the media based on filename
            file = new File(mediaStorageDir, fileName);
            // Wrap File object into a content provider, required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-withIntents#sharing-files-with-api-24-or-higher
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(
                        this.getApplicationContext(),
                        "comp5216.sydney.edu.au.mediaaccess.fileProvider", file);
            } else {
                fileUri = Uri.fromFile(mediaStorageDir);}
        } catch (Exception ex) {
            Log.e("getFileUri", ex.getStackTrace().toString());
        }
        Log.i("File Directory:", String.valueOf(fileUri));
        return fileUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final VideoView mVideoView = (VideoView) findViewById(R.id.videoview);
        ImageView ivPreview = (ImageView) findViewById(R.id.photopreview);
        mVideoView.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        if (requestCode == MY_PERMISSIONS_REQUEST_OPEN_CAMERA) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(file.getAbsolutePath());
                // Load the taken image into a preview
                ivPreview.setImageBitmap(takenImage);
                ivPreview.setVisibility(View.VISIBLE);
            } else {
                // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHOTOS) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(), photoUri);
                    // Load the selected image into a preview
                    ivPreview.setImageBitmap(selectedImage);
                    ivPreview.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_VIDEOS) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(videoUri);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri takenVideoUri = getFileUri(videoFileName,1);mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(takenVideoUri);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            }
        }
    }
}