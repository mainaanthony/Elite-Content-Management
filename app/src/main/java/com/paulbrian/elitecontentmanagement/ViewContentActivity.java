package com.paulbrian.elitecontentmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ViewContentActivity extends AppCompatActivity {
    private WebView webView;
    private EditText editTextComment;
    private Button buttonComment, buttonLike, buttonDislike, buttonShare, buttonDownload;
    private String contentUrl, key;
    private LinearLayout linearLayoutComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content);

        webView = findViewById(R.id.webView);
        editTextComment = findViewById(R.id.editTextComment);
        buttonComment = findViewById(R.id.buttonComment);
        buttonLike = findViewById(R.id.buttonLike);
        buttonDislike = findViewById(R.id.buttonDislike);
        buttonShare = findViewById(R.id.buttonShare);
        buttonDownload = findViewById(R.id.buttonDownload);
        linearLayoutComment = findViewById(R.id.linearLayoutComment);

        contentUrl = getIntent().getStringExtra("contentUrl");
        key = getIntent().getStringExtra("key");

        if (contentUrl.contains(".pdf?alt=media")||contentUrl.contains(".docx?alt=media")||contentUrl.contains(".doc?alt=media")) {
            try {
                webView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + URLEncoder.encode(contentUrl, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                webView.loadUrl(contentUrl);

            }
            buttonDownload.setVisibility(View.VISIBLE);
            buttonDownload.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(contentUrl));
                startActivity(i);
            });

        }else {
            webView.loadUrl(contentUrl);
        }

        loadLikes(key);

        if (getIntent().hasExtra("ownContent")){
            linearLayoutComment.setVisibility(View.GONE);
        }else {
            String uid = FirebaseAuth.getInstance().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents")
                    .child(key);

            buttonComment.setOnClickListener(v -> {
                String comment = editTextComment.getText().toString();

                if (comment.isEmpty()) {
                    Toast.makeText(getBaseContext(), "Enter comment first", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "Comment posted successfully", Toast.LENGTH_LONG).show();
                    buttonComment.setText("Comment Posted");
                    editTextComment.setVisibility(View.GONE);
                    databaseReference.child("comments").child(uid).setValue(comment);

                }
            });

            buttonLike.setOnClickListener(v -> {
                Toast.makeText(getBaseContext(), "Content liked", Toast.LENGTH_LONG).show();
                databaseReference.child("likes").child(uid).setValue(true);
                databaseReference.child("disLikes").child(uid).removeValue();
            });
            buttonDislike.setOnClickListener(v -> {
                Toast.makeText(getBaseContext(), "Content disliked", Toast.LENGTH_LONG).show();
                databaseReference.child("disLikes").child(uid).setValue(true);
                databaseReference.child("likes").child(uid).removeValue();
            });
            buttonShare.setOnClickListener(v -> {
                Toast.makeText(getBaseContext(), "Share content to others", Toast.LENGTH_LONG).show();
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    String shareMessage = "\nElite Content" +
                            "Check out this elite post: " + contentUrl;
                    shareMessage = shareMessage + "\n\n~eliteApp";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose app"));
                } catch (Exception e) {
                    //e.toString();
                }
            });
        }

    }

    private void loadLikes(String key){
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents")
                .child(key);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                buttonLike.setText(String.valueOf(snapshot.child("likes").getChildrenCount()));
                buttonDislike.setText(String.valueOf(snapshot.child("disLikes").getChildrenCount()));

                if (snapshot.child("comments").hasChild(uid)){
                    editTextComment.setText(snapshot.child("comments").child(uid).getValue().toString());
                    buttonComment.setText("Edit Comment");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}