package com.hrsh.doodledraw;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrawingActivity extends Activity
//        implements ColorPickerDialog.OnColorChangedListener
{
    MyView mv;
    LinearLayout linearLayout;
    AppCompatSeekBar seekBar;
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Path> paths = new ArrayList<Path>();
    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter mBlur;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseFirestore db;
    boolean eraseFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);
        mv= new MyView(this);
        mv.setDrawingCacheEnabled(true);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout2);
        linearLayout.addView(mv, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        v = findViewById(R.id.canvasView);
//        v = mv;
        db = FirebaseFirestore.getInstance();
        seekBar = findViewById(R.id.discreteSeekBar);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(seekBar.getProgress());
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaint.setStrokeWidth(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ((Button) findViewById(R.id.undoButton))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (paths.size() > 0) {
                            System.out.println("Undo button pressed...");
                            undonePaths.add(paths
                                    .remove(paths.size() - 1));
                            mv.invalidate();
                        }
                    }
                });
        ((Button) findViewById(R.id.redoButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("Redo button pressed...");
                        if (undonePaths.size()>0) {
                            paths.add(undonePaths.remove(undonePaths.size()-1));
                            mv.invalidate();
                        }
                    }
                });

        ((Button) findViewById(R.id.colorPicker))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorPickerDialogBuilder
                                .with(DrawingActivity.this)
                                .setTitle("Choose color")
                                .initialColor(Color.WHITE)
                                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                .density(12)
                                .setOnColorSelectedListener(new OnColorSelectedListener() {
                                    @Override
                                    public void onColorSelected(int selectedColor) {
                                        Toast.makeText(DrawingActivity.this, Integer.toHexString(selectedColor), Toast.LENGTH_SHORT);
                                    }
                                })
                                .setPositiveButton("ok", new ColorPickerClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                        mPaint.setColor(selectedColor);
                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .build()
                                .show();
//                        new ColorPickerDialog(MainActivity.this, MainActivity.this, mPaint.getColor()).show();
                    }
                });

        ((Button) findViewById(R.id.eraseButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("Erase clicked...");
//                        mPaint.setColor(Color.TRANSPARENT);
//                        eraseFlag=!eraseFlag;

//                        if(eraseFlag) {
                            mPaint.setStrokeWidth(50);
                            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                            mPaint.setAlpha(0x80);
//                        }else{

//                        }
                    }
                });

        ((Button) findViewById(R.id.saveButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
                        androidx.appcompat.app.AlertDialog.Builder editalert = new androidx.appcompat.app.AlertDialog.Builder(DrawingActivity.this);
                        editalert.setTitle("Please Enter the name with which you want to Save");
                        final EditText input = new EditText(DrawingActivity.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        editalert.setView(input);
                        editalert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String name= input.getText().toString();
                                Bitmap bmp = mv.save();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                                OutputStream imageOutStream = null;
                                ContentValues cv = new ContentValues();
                                cv.put(MediaStore.Images.Media.DISPLAY_NAME, name+".png");
                                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                                cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                                try {
                                    imageOutStream = getContentResolver().openOutputStream(uri);
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                    imageOutStream.close();
                                    uploadImage(baos);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//                                Bitmap bitmap = mv.getDrawingCache();
//
//                                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//                                File file = new File("/"+name+".png");
//                                try
//                                {
//                                    if(!file.exists())
//                                    {
//                                        file.createNewFile();
//                                    }
//                                    FileOutputStream ostream = new FileOutputStream(file);
//                                    bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
//                                    ostream.close();
//                                    mv.invalidate();
//                                }
//                                catch (Exception e)
//                                {
//                                    e.printStackTrace();
//                                }finally
//                                {
//                                    mv.setDrawingCacheEnabled(false);
//                                }
                            }
                        });

                        editalert.show();
                    }
                });
    }
    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadImage(ByteArrayOutputStream outputStream) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId="", userName="";

        if (user != null) {
            userId = user.getUid();
            userName = user.getDisplayName();
        }

        StorageReference drawingsRef = storageRef.child(userId+"_"+System.currentTimeMillis());

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = drawingsRef.putBytes(data);
        ProgressDialog finalProgressDialog = progressDialog;

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                finalProgressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                drawingsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();

                        Map<String , Object> drawing = new HashMap<>();
                        drawing.put("uid", user.getUid());
                        drawing.put("email", user.getEmail());
                        drawing.put("url", url);

                        db.collection("Drawings").add(drawing).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                finalProgressDialog.dismiss();
                            }
                        });
                    }
                });
//                taskSnapshot.getStorage().getDownloadUrl();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                finalProgressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        });
    }

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    void checkPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(DrawingActivity.this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(DrawingActivity.this, new String[] {permission}, requestCode);
        }else{
            Toast.makeText(DrawingActivity.this, "Permission already granted!!", Toast.LENGTH_SHORT).show();
        }
    }

    public class MyView extends View {

        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint   mBitmapPaint;
        Context context;

        public MyView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            paths.add(mPath);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        }

        Bitmap save(){
            return mBitmap;
        }
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            //showDialog();
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;

        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            paths.add(mPath);
            mPaint.setMaskFilter(null);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:

                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                ColorPickerDialogBuilder
                        .with(DrawingActivity.this)
                        .setTitle("Choose color")
                        .initialColor(Color.WHITE)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                Toast.makeText(DrawingActivity.this, Integer.toHexString(selectedColor), Toast.LENGTH_SHORT);
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mPaint.setColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .build()
                        .show();
//                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
//                eraseFlag=!eraseFlag;

//                if(eraseFlag) {
                    mPaint.setStrokeWidth(50);
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                            mPaint.setAlpha(0x80);
//                }else{
//                    mPaint.setStrokeWidth(seekBar.getProgress());
//                    mPaint.setXfermode(null);
//                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
