package com.fatih.sanatkitabm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;


public class SecondFragment extends Fragment {

    ActivityResultLauncher<String> permissionLauncher;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Bitmap selectedImage;
    ImageView imageView;
    Button saveButton;
    EditText artText,artistText,yearText;
    SQLiteDatabase db;
    ByteArrayOutputStream outputStream;
    String info;
    int id;
    byte[] image;

    public SecondFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=requireContext().openOrCreateDatabase("Art", Context.MODE_PRIVATE,null);
        registerLauncher();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       
        return inflater.inflate(R.layout.fragment_second, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artText=view.findViewById(R.id.artText);
        artistText=view.findViewById(R.id.artistText);
        yearText=view.findViewById(R.id.yearText);
        saveButton=view.findViewById(R.id.saveButton);
        imageView=view.findViewById(R.id.imageView);
        if(getArguments()!=null){
            info=SecondFragmentArgs.fromBundle(getArguments()).getInfo();
            System.out.println("info"+info);
            if(info.equals("new")){
                saveButton.setVisibility(View.VISIBLE);
            }else if(info.equals("old")){
                id= SecondFragmentArgs.fromBundle(getArguments()).getId();
                System.out.println("id"+id);
                saveButton.setVisibility(View.GONE);
                try {
                    Cursor cursor=db.rawQuery("SELECT * FROM Art WHERE id=?", new String[]{String.valueOf(id)});
                    int artX=cursor.getColumnIndex("art");
                    int artistX=cursor.getColumnIndex("artist");
                    int yearX=cursor.getColumnIndex("year");
                    int imageX=cursor.getColumnIndex("image");
                    while (cursor.moveToNext()){
                        artText.setText(cursor.getString(artX));
                        artistText.setText(cursor.getString(artistX));
                        yearText.setText(cursor.getString(yearX));
                        byte[] image=cursor.getBlob(imageX);
                        Bitmap selectedBitmap= BitmapFactory.decodeByteArray(image, 0, image.length);
                        imageView.setImageBitmap(selectedBitmap);
                    }
                    cursor.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });
    }
    public void save(View view) {

        String art = artText.getText().toString();
        String artist = artistText.getText().toString();
        String year = yearText.getText().toString();
        if(selectedImage!=null){
            Bitmap smallImage = smallImage(selectedImage, 250);
            outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
            image = outputStream.toByteArray();
        }
        if(image!=null&& !art.equals("") && !artist.equals("") && !year.equals("")){
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS Art(id INTEGER PRIMARY KEY,art String,artist String,year String,image BLOB)");
                String sql = "INSERT INTO Art(art,artist,year,image) VALUES(?,?,?,?)";
                SQLiteStatement sqLiteStatement = db.compileStatement(sql);
                sqLiteStatement.bindString(1, art);
                sqLiteStatement.bindString(2, artist);
                sqLiteStatement.bindString(3, year);
                sqLiteStatement.bindBlob(4, image);
                sqLiteStatement.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        NavDirections action = SecondFragmentDirections.actionSecondFragment2ToFirstFragment2();
        Navigation.findNavController(view).navigate(action);
    }

    private Bitmap smallImage(Bitmap bitmap,int maxSize){
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        float scale=(float)width/(float) height;
        if(scale>1){
            width=maxSize;
            height= (int) (width/scale);
        }else{
            height=maxSize;
            width= (int) (height*scale);
        }
        return Bitmap.createScaledBitmap(bitmap,width, height, true);
    }
    @SuppressLint("ShowToast")
    public void selectImage(View view){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Need Permission", BaseTransientBottomBar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                //permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            //gallery
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        }
    }
    private void registerLauncher() {
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intent);
                }
            }
        });
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intentFromGallery=result.getData();
                    if(intentFromGallery!=null){
                        Uri uri=intentFromGallery.getData();
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                ImageDecoder.Source source=ImageDecoder.createSource(requireActivity().getContentResolver(), uri);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                imageView.setImageBitmap(selectedImage);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}