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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.paulbrian.elitecontentmanagement.Constants;
import com.paulbrian.elitecontentmanagement.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddContentFragment extends Fragment {
    private EditText editTextTitle, editTextDescription, editTextAmount, editTextArticle;
    private Button buttonAdd;
    private ImageView imageView, imageViewContent;
    private RadioGroup  radioGroupContentType;
    private RadioButton radioButtonArticle, radioButtonVideo, radioButtonPodcast;
    private LinearLayout linearLayoutArticle, linearLayoutVideo, linearLayoutPodcast;
    private Spinner spinnerCategories;

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

    public AddContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddContentFragment newInstance(String param1, String param2) {
        AddContentFragment fragment = new AddContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_add_content, container, false);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextArticle = view.findViewById(R.id.editTextArticle);
        radioGroupContentType = view.findViewById(R.id.radioGroupContentType);

        radioButtonArticle = view.findViewById(R.id.radioButtonArticle);
        radioButtonVideo = view.findViewById(R.id.radioButtonVideo);
        radioButtonPodcast = view.findViewById(R.id.radioButtonPodcast);
        linearLayoutArticle = view.findViewById(R.id.linearLayoutArticle);
        linearLayoutVideo = view.findViewById(R.id.linearLayoutVideo);
        linearLayoutPodcast = view.findViewById(R.id.linearLayoutPodcast);

        firebaseAuth = FirebaseAuth.getInstance();

        imageView = view.findViewById(R.id.imageView);
        imageViewContent = view.findViewById(R.id.imageViewContent);
        buttonAdd = view.findViewById(R.id.buttonAdd);

        buttonAdd.setOnClickListener(v -> add());
        imageView.setOnClickListener(v -> chooseImage());
        imageViewContent.setOnClickListener(v -> chooseContent());

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        spinnerCategories = (Spinner) view.findViewById(R.id.spinnerCategories);
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, Constants.categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinnerCategories.setAdapter(arrayAdapter);


//        radioButtonArticle.setOnClickListener(v -> {
//            linearLayoutArticle.setVisibility(View.VISIBLE);
//            linearLayoutVideo.setVisibility(View.GONE);
//            linearLayoutPodcast.setVisibility(View.GONE);
//        });
//        radioButtonVideo.setOnClickListener(v -> {
//            linearLayoutArticle.setVisibility(View.GONE);
//            linearLayoutVideo.setVisibility(View.VISIBLE);
//            linearLayoutPodcast.setVisibility(View.GONE);
//        });
//        radioButtonPodcast.setOnClickListener(v -> {
//            linearLayoutArticle.setVisibility(View.GONE);
//            linearLayoutVideo.setVisibility(View.GONE);
//            linearLayoutPodcast.setVisibility(View.VISIBLE);
//        });


        return view;
    }


    private void add() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String amount = editTextAmount.getText().toString();
        String contentType = getSelectedRadioButton(radioGroupContentType);

        if (title.isEmpty()||description.isEmpty()||amount.isEmpty()){
            Toast.makeText(getContext(), "Fill all details first", Toast.LENGTH_LONG).show();
        }else if (bitmap == null){
            Toast.makeText(getContext(), "Choose Image first first", Toast.LENGTH_LONG).show();
            imageView.requestFocus();
        }else if (contentType.equals("-1")){
            Toast.makeText(getContext(), "Choose Type Of Content first", Toast.LENGTH_LONG).show();
            radioGroupContentType.requestFocus();
        }else if (uriContent == null){
            Toast.makeText(getContext(), "Choose content file first", Toast.LENGTH_LONG).show();
            imageViewContent.requestFocus();
        }else {
            uploadImage();
        }

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
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> uploadContent(uri.toString()));
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        });

        if(uri!= null) {
            StorageReference imageRef= FirebaseStorage.getInstance().getReference().child("Images").child(System.currentTimeMillis()+"."+getFileExtension(uri));
        }
    }

    private void uploadContent(String imageUrl) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());

        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Uploading Content File to Firebase");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        String fileName = System.currentTimeMillis()+"."+getFileExtension(uriContent);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Images").child(fileName);

        storageRef.putFile(uriContent).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> saveToFirebase(imageUrl, uri.toString()));
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }).addOnProgressListener(snapshot -> progressDialog.setProgress((int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount())));

    }

    private void saveToFirebase(String imageUrl, String contentUrl){
        final String uid = firebaseAuth.getCurrentUser().getUid();

        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String amount = editTextAmount.getText().toString();
        String category = spinnerCategories.getSelectedItem().toString();
        String contentType = getSelectedRadioButton(radioGroupContentType);

        ProgressDialog progressDialog = new ProgressDialog(getContext());

        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Posting Content to others");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("amount", amount);
        hashMap.put("category", category);
        hashMap.put("contentType", contentType);
        hashMap.put("content", contentUrl);
        hashMap.put("notApproved", "true");
        hashMap.put("uploader", uid);
        hashMap.put("image", imageUrl);

        databaseReference.push().setValue(hashMap).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(),"Content added successfully", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> progressDialog.dismiss());

        buttonAdd.setClickable(false);
        buttonAdd.setText("Uploaded");
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

    private void chooseContent() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Allow Files Access Permissions First", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(),  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_FILES);

        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Choose a file"),
                    CONTENT_FILES_REQUEST
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


        }else if(requestCode== CONTENT_FILES_REQUEST && resultCode== Activity.RESULT_OK){
            uriContent = data.getData();
            imageViewContent.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_cloud_done_24));
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private String getSelectedRadioButton(RadioGroup radioGroup){
        int radioButtonID = radioGroup.getCheckedRadioButtonId();

        if (radioButtonID == -1) return "-1";

        View radioButton = radioGroup.findViewById(radioButtonID);
        int idx = radioGroup.indexOfChild(radioButton);

        RadioButton r = (RadioButton) radioGroup.getChildAt(idx);

        return r.getText().toString();
    }

}