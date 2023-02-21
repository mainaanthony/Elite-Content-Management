package com.paulbrian.elitecontentmanagement.user;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paulbrian.elitecontentmanagement.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ProfileFragment extends Fragment {
    private EditText editTextName, editTextMobile;
    private Button buttonAdd;
    private ImageView imageView;

    private FirebaseAuth firebaseAuth;

    private Uri uri;
    private Uri uriContent;
    private Bitmap bitmap = null;
    public static final int MY_PERMISSIONS_REQUEST_FILES = 100;
    private final int FILES_REQUEST = 12;
    private final int CONTENT_FILES_REQUEST = 13;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        editTextName = view.findViewById(R.id.editTextName);
        editTextMobile = view.findViewById(R.id.editTextMobile);


        imageView = view.findViewById(R.id.imageView);
        buttonAdd = view.findViewById(R.id.buttonUpload);

        buttonAdd.setOnClickListener(v -> add());
        imageView.setOnClickListener(v -> chooseImage());
        firebaseAuth = FirebaseAuth.getInstance();

        final String uid = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editTextName.setText(snapshot.child("name").getValue().toString());
                editTextMobile.setText(snapshot.child("mobile").getValue().toString());

                if (snapshot.hasChild("photoURL")){
                    Glide.with(getContext()).load(snapshot.child("photoURL").getValue().toString()).into(imageView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void add() {

        if (bitmap != null){
            uploadImage();
        }

        saveToFirebase("");

    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());

        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Saving Image to Firebase");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String fileName = System.currentTimeMillis()+".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Images").child(fileName);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        storageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> saveToFirebase(uri.toString()));
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        });

        if(uri!= null) {
            StorageReference imageRef= FirebaseStorage.getInstance().getReference().child("Images").child(System.currentTimeMillis()+"."+getFileExtension(uri));
        }
    }


    private void saveToFirebase(String imageUrl){
        final String uid = firebaseAuth.getCurrentUser().getUid();

        String name = editTextName.getText().toString();
        String mobile = editTextMobile.getText().toString();

        ProgressDialog progressDialog = new ProgressDialog(getContext());

        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Updating Details");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid);
        databaseReference.child("name").setValue(name);
        databaseReference.child("mobile").setValue(mobile);
        if (!imageUrl.equals("")) databaseReference.child("photoURL").setValue(imageUrl);

        progressDialog.dismiss();
        Toast.makeText(getContext(),"Profile Updated successfully", Toast.LENGTH_LONG).show();

        buttonAdd.setClickable(false);
        buttonAdd.setText("Updated");
        buttonAdd.setBackgroundColor(Color.GRAY);
    }

    private void chooseImage() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Allow Files Access Permissions First", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(),  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_FILES);

        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Choose a file"),
                    FILES_REQUEST
            );
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== FILES_REQUEST && resultCode== Activity.RESULT_OK){
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

}