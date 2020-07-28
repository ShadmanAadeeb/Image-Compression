package com.example.imagecompression;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button getImageButton,uploadImageButton,rotateImageButton;

    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Linking the elements*/
        linkTheXMLComponents();
        /*Setting action listener to the button*/
        setActionListenerToTheButton();

    }


    private void linkTheXMLComponents() {
        imageView=findViewById(R.id.showImageImageView);
        getImageButton =findViewById(R.id.getImageButton);
        uploadImageButton=findViewById(R.id.uploadImageButton);
        rotateImageButton=findViewById(R.id.rotateImageButton);
        uploadImageButton.setEnabled(false);
        rotateImageButton.setEnabled(false);
    }

    private  void setActionListenerToTheButton(){
        getImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*THE CODE TO GET IMAGE FROM THE GALLERY */

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);


                /*REST OF THE CODES FOR PLACING GALLERY  IMAGE ON IMAGEVIEW  IS IN onActivityResult() FUNCTION*/
            }
        });
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*THE CODE HERE UPLODAS THE IMAGE TO THE FIREBASE STORAGE*/

                /*Converting the Image from image view into a byte array*/
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 65, baos);
                byte[] data = baos.toByteArray();

                /*CODE FOR PUTTING THE IMAGE INTO THE STORAGE Under Images Folder with name myImage.jpg(Extension is importatnt)*/
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("/Images");
                storageRef.child("myImage.jpg").putBytes(data).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("TAG1","FAILED");
                        exception.printStackTrace();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d("TAG1","SUCCESS");
                    }
                });
            }
        });
        rotateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*THIS IS THE CODE FOR ROTATING THE IMAGE ON THE IMAGE VIEW*/
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmapOrg = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg, 200, 200, true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                imageView.setImageBitmap(rotatedBitmap);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG1","CAME HERE");
        /*THE CODE HERE SIMPLY PLACES THE CHOSEN IMAGE ON THE IMAGEVIEW*/
        if (requestCode == 1 && resultCode==RESULT_OK) {
            try {
                /*Get the Uniform Resource Identifier of the image*/
                final Uri imageUri = data.getData();
                /*Create an input stream from the image uri*/
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                /*Get the data from the input stream into the butmap*/
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                /*Resize the image and place it on another bitmap*/
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        selectedImage, 200, 200, false);//RESIZED TO 200 by 200
                /*Place the image on imageview*/
                imageView.setImageBitmap(resizedBitmap);

                uploadImageButton.setEnabled(true);
                rotateImageButton.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else{
            Log.d("TAG1","CHOSE NOTHING");
        }
    }
}