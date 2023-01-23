package com.pradeep.videoplayercollection;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class GalleryImageData extends AppCompatActivity {
    private String TAG = "GalleryImageData";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private File fileUri;
    private LinearLayout mGallaryLayout;
    private LinearLayout mImageLayout;
    private ImageView mFullImage;
    private Context mContext;
    private RelativeLayout imageListView;
    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float oldDist = 1f;
    private float oldRot = 0f;
    private float oldScaleX = 0f;
    private float oldScaleY = 0f;

    private float xCoOrdinate, yCoOrdinate;
    private ArrayList<Object> listImage;
    private ImageAdapter imageAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galary_image);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGallaryLayout = findViewById(R.id.galary_view);
        mImageLayout = findViewById(R.id.image_full_view);
        mFullImage = findViewById(R.id.image_full_view_display);
        imageListView = (RelativeLayout) findViewById(R.id.image_view_display);
        mContext = this;
        final CardView image_view = (CardView) findViewById(R.id.image_view);
        final CardView camera = (CardView) findViewById(R.id.camera);
        final CardView image_editor = (CardView) findViewById(R.id.image_editor);
        image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout allview = (LinearLayout) findViewById(R.id.all_view);
                allview.setVisibility(View.GONE);
                DisplayImage();
                Log.v(TAG, "Profile....image_view");
            }
        });
        image_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout allview = (LinearLayout) findViewById(R.id.all_view);
                allview.setVisibility(View.GONE);
                Log.v(TAG, "Profile....image_editor");
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout allview = (LinearLayout) findViewById(R.id.all_view);
                allview.setVisibility(View.GONE);
                Log.v(TAG, "Profile....camera");
                openCamara();

            }
        });
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        display();
    }

    private void display() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                listImage = getFilePaths();
            }
        };
        thread.start();


    }

    private void openCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.i(TAG, "IOException");
            }
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        fileUri = image;
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode != RESULT_CANCELED) {
                    if (resultCode == RESULT_OK) {
                        saveImageToExternalStorage(fileUri);
                    }
                }
        }
    }

    public void saveImageToExternalStorage(File finalBitmap) {
        Intent galleryIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri picUri = Uri.fromFile(finalBitmap);
        galleryIntent.setData(picUri);
        GalleryImageData.this.sendBroadcast(galleryIntent);
        LinearLayout allview = (LinearLayout) findViewById(R.id.all_view);
        allview.setVisibility(View.VISIBLE);
        return;
    }

    private void DisplayImage() {
        //listImage = getFilePaths();
        Log.e(TAG, "DisplayImage"+listImage.size());
        imageListView.setVisibility(View.VISIBLE);
        RecyclerView imageRecyclerView = (RecyclerView) findViewById(R.id.image_recycler_view);
        GridLayoutManager manager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        imageRecyclerView.setLayoutManager(manager);
        imageAdapter = new ImageAdapter(GalleryImageData.this, listImage, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, final int position, final List<Object> recordedEvent) {
                try {
                    mGallaryLayout.setVisibility(View.GONE);
                    mImageLayout.setVisibility(View.VISIBLE);
                    ImageObject imageObject = (ImageObject) listImage.get(position);
                    String filePath = imageObject.getmPath();
                    File imgFile = new  File(filePath);
                    if(imgFile.exists()){
                        Glide.with(mContext).load(filePath).placeholder(R.drawable.maxresdefault).error(R.drawable.no_thumbnail).into(mFullImage);
                    }
                    oldRot = mFullImage.getRotation();
                    oldScaleX= mFullImage.getScaleX();
                    oldScaleY= mFullImage.getScaleY();
                    mFullImage.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            ImageView view = (ImageView) v;
                            view.bringToFront();
                            viewTransformation(view, event);
                            return true;
                        }
                    });
                } catch (IndexOutOfBoundsException ex) {
                    Log.w(TAG, "No such Item exists");
                    return;
                }
            }

            @Override
            public boolean onItemLongClick(View v, final int position, final List<Object> recordedEvent) {
                final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                LinearLayout delete = (LinearLayout) findViewById(R.id.image_delete_layout);
                delete.setVisibility(View.VISIBLE);
                ImageButton deleteImage = (ImageButton)findViewById(R.id.btn_delete);
                deleteImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteImage();
                    }
                });
                ImageButton shareImage = (ImageButton)findViewById(R.id.btn_share);
                shareImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareData();
                    }
                });
                return true;
            }
        });
        imageRecyclerView.setAdapter(imageAdapter);
        imageRecyclerView.hasFixedSize();
        Log.e(TAG, "end"+listImage.size());
    }

    private void shareData() {
        LinearLayout delete = (LinearLayout) findViewById(R.id.image_delete_layout);
        delete.setVisibility(View.GONE);
        deselectAll();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "sending some file");
        intent.setType("image/jpeg");
        ArrayList<Uri> files = new ArrayList<Uri>();
        for(int i = 0; i< listImage.size();i++) {
            ImageObject data = (ImageObject)listImage.get(i);
            if(data.isSelect()){
                File image = new File(data.getmPath());
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                bitmap = Bitmap.createScaledBitmap(bitmap,720,576,true);
                File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file"+i+".jpg");
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(imageFile);
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    files.add(Uri.fromFile(new File(String.valueOf(imageFile))));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Image"));
    }

    private void deleteImage() {
        LinearLayout delete = (LinearLayout) findViewById(R.id.image_delete_layout);
        delete.setVisibility(View.GONE);
        for(int i = 0; i< listImage.size();i++) {
            ImageObject data = (ImageObject)listImage.get(i);
            if(data.isSelect()){
                String path = data.getmPath();
                File filedelete = new File(path);
                if (filedelete.exists()) {
                    if (filedelete.delete()) {
                        System.out.println("file Deleted :" + path);
                    } else {
                        System.out.println("file not Deleted :" + path);
                    }
                }
                listImage.remove(i);
            }
        }
        imageAdapter.swap();
    }

    private void deselectAll() {
        LinearLayout delete = (LinearLayout) findViewById(R.id.image_delete_layout);
        delete.setVisibility(View.GONE);
        for(int i = 0; i< listImage.size();i++) {
            ImageObject data = (ImageObject)listImage.get(i);
            data.setSelect(false);
        }
        imageAdapter.swap();
    }

    public ArrayList<Object> getFilePaths() {
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri t = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<String>();
        ArrayList<Object> resultIAV = new ArrayList<Object>();
        String[] directories = null;
        if (u != null) {
            c = managedQuery(u, projection, null, null, null);
        }

        if ((c != null) && (c.moveToFirst())) {
            do {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch (Exception e) {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if (imagePath.isDirectory()) {
                        imageList = imagePath.listFiles();
                    }
                    if (imagePath.getName().contains(".jpg") || imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg") || imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                    ) {
                        String path = imagePath.getAbsolutePath();
                        ImageObject temp = new ImageObject(path,false);
                        resultIAV.add(temp);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (t != null) {
            c = managedQuery(t, projection, null, null, null);
        }
        if ((c != null) && (c.moveToFirst())) {
            do {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch (Exception e) {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if (imagePath.isDirectory()) {
                        imageList = imagePath.listFiles();
                    }
                    if (imagePath.getName().contains(".jpg") || imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg") || imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                    ) {


                        String path = imagePath.getAbsolutePath();
                        ImageObject temp = new ImageObject(path,false);
                        resultIAV.add(temp);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Log.e(TAG, "Profile....list  size "+resultIAV.size());

        return resultIAV;
    }

    public ArrayList<HashMap<String, String>> getAudioList() {
        ArrayList<HashMap<String, String>> mSongsList = new ArrayList<HashMap<String, String>>();
        Cursor mCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA
        }, null, null, null);
        int count = mCursor.getCount();
        System.out.println("total no of songs are=" + count);
        HashMap<String, String> songMap;
        while (mCursor.moveToNext()) {
            songMap = new HashMap<String, String>();
            songMap.put("songTitle", mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
            songMap.put("songPath", mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            mSongsList.add(songMap);
        }
        mCursor.close();
        return mSongsList;
    }

    @Override
    public void onBackPressed() {
        LinearLayout delete = (LinearLayout) findViewById(R.id.image_delete_layout);
        if(delete.getVisibility() == View.VISIBLE) {
            deselectAll();
            return;
        } else if(mImageLayout.getVisibility() ==View.VISIBLE) {
            lastEvent = null;
            d = 0f;
            newRot = 0f;
            isZoomAndRotate = false;
            isOutSide = false;
            mode = NONE;
            start = null;
            start = new PointF();
            mid = null;
            mid = new PointF();
            oldDist = 1f;
            xCoOrdinate = 0.0f;
            yCoOrdinate = 0.0f;
            if((mFullImage.getRotation() != oldRot) &&(mFullImage.getScaleX() != oldScaleX) &&(mFullImage.getScaleY() != oldScaleY)){
                mFullImage.setScaleX(oldScaleX);
                mFullImage.setScaleY(oldScaleY);
                mFullImage.setRotation(oldRot);
                return;
            }
            oldScaleX = 0f;
            oldScaleY = 0f;
            oldRot = 0f;
            mGallaryLayout.setVisibility(View.VISIBLE);
            mImageLayout.setVisibility(View.GONE);
            return;
        } else if(imageListView.getVisibility() == View.VISIBLE) {
            imageListView.setVisibility(View.GONE);
            LinearLayout allview = (LinearLayout) findViewById(R.id.all_view);
            allview.setVisibility(View.VISIBLE);
        } else {
            Intent i = new Intent(GalleryImageData.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(i);
            finish();
        }
    }

    private void viewTransformation(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();
                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE: {
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            float scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL : {
                lastEvent = null;
                d = 0f;
                newRot = 0f;
                isZoomAndRotate = false;
                isOutSide = false;
                mode = NONE;
                start = null;
                start = new PointF();
                mid = null;
                mid = new PointF();
                oldDist = 1f;
                xCoOrdinate = 0.0f;
                yCoOrdinate = 0.0f;
            }
        }
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


}