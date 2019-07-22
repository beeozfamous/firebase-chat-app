package com.project.helloworst.firebasechatapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AvatarBottomSheetFragment extends BottomSheetDialogFragment {

    private static final int CAMERA_PICK = 2 ;
    private Button camera_btn;
    private Button library_btn;
    private static final int GALLERY_PICK=1;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUserData;
    private StorageReference mProfileImage;

    private ProgressDialog mProgressDialog;
    private Uri imageUri;


    public AvatarBottomSheetFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_avatar_bottom_sheet, container, false);

        mCurrentUserData=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUID= mCurrentUserData.getUid();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUID);
        mProfileImage=FirebaseStorage.getInstance().getReference();


        library_btn=v.findViewById(R.id.avatar_library);
        library_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent= new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        camera_btn=v.findViewById(R.id.avatar_camera);
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionsGranted())
                {
                    captureImage();
                }else{
                requestPermission();
                }
            }
        });
        return v;
    }

    public void captureImage(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your camera");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent= new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, CAMERA_PICK);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CAMERA_PICK && resultCode==RESULT_OK){
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1).setCropShape(CropImageView.CropShape.OVAL)
                    .start(getContext(),this);
        }
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){

            imageUri=data.getData();
             CropImage.activity(imageUri)
                    .setAspectRatio(1,1).setCropShape(CropImageView.CropShape.OVAL)
                    .start(getContext(),this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog= new ProgressDialog(getContext());
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                String userID=mCurrentUserData.getUid();

                StorageReference filepath=mProfileImage.child("profile_images").child(userID+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            String download_url=task.getResult().getDownloadUrl().toString();

                            mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });

                        }else{
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @SuppressLint("CheckResult")
    public void requestPermission(){
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        rxPermissions
                .request( Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        captureImage();
                    } else {
                        Toast.makeText(getContext(),"Action need permisson!",Toast.LENGTH_LONG).show();
                    }
                });
    }
    private boolean checkPermissionsGranted() {
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

}
