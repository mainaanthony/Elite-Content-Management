package com.paulbrian.elitecontentmanagement.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paulbrian.elitecontentmanagement.R;
import com.paulbrian.elitecontentmanagement.ViewContentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyContentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyContentsFragment extends Fragment {
    private MyContentsFragmentAdapter myContentsFragmentAdapter;
    private ProgressDialog progressDialog;
    private TextView textViewError;
    private FirebaseAuth firebaseAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyContentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyContentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyContentsFragment newInstance(String param1, String param2) {
        MyContentsFragment fragment = new MyContentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_my_contents, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        textViewError = view.findViewById(R.id.textViewError);

        String uid = firebaseAuth.getUid();

        ArrayList<Map<String , String >> canteens = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myContentsFragmentAdapter = new MyContentsFragmentAdapter(this,getContext(), canteens);
        recyclerView.setAdapter(myContentsFragmentAdapter);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Fetching Contents");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean anContentExists = false;

                for (DataSnapshot dataSnapshotTicket: snapshot.getChildren()){

                    if (dataSnapshotTicket.child("uploader").getValue().toString().equals(uid)){
                        HashMap<String ,String > hashMap = new HashMap<>();
                        hashMap.put("key", dataSnapshotTicket.getKey());
                        anContentExists = true;
                        for (DataSnapshot dataSnapshotValue: dataSnapshotTicket.getChildren()){
                            hashMap.put(dataSnapshotValue.getKey(), dataSnapshotValue.getValue().toString());
                        }
                        canteens.add(hashMap);
                        myContentsFragmentAdapter.notifyDataSetChanged();
                    }


                }

                if (!anContentExists){
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("You have no contents yet");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        return  view;
    }
}
class MyContentsFragmentAdapter extends RecyclerView.Adapter<MyContentsFragmentAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private MyContentsFragmentAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;
    private Fragment fragment;


    // data is passed into the constructor
    public MyContentsFragmentAdapter(Fragment fragment, Context context, ArrayList<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.fragment = fragment;
    }

    // inflates the row layout from xml when needed
    @Override
    public MyContentsFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_content, parent, false);
        return new MyContentsFragmentAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(MyContentsFragmentAdapter.ViewHolder holder, int position) {
        message = mData.get(position);

        String key = message.get("key");
        String contentUrl = message.get("content");
        String uid = FirebaseAuth.getInstance().getUid();

        Glide.with(context).load(message.get("image")).into(holder.imageView);

        holder.textViewTitle.setText(message.get("title"));
        holder.textViewDescription.setText(message.get("description"));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents").child(key);

        holder.buttonDelete.setOnClickListener(v -> {
            databaseReference.removeValue();

            holder.buttonDelete.setVisibility(View.GONE);
            holder.buttonMore.setVisibility(View.GONE);

            Toast.makeText(context, "Content Deleted",Toast.LENGTH_LONG).show();
        });

        holder.buttonMore.setOnClickListener(v -> {
//            Toast.makeText(context, "To implement this...",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, ViewContentActivity.class);
            intent.putExtra("contentUrl", contentUrl);
            intent.putExtra("ownContent", true);
            intent.putExtra("key", key);

            context.startActivity(intent);

        });
    }



    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewTitle, textViewDescription;
        Button buttonDelete, buttonMore;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);

            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonMore = itemView.findViewById(R.id.buttonMore);
            imageView  = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Map<String, String> getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(MyContentsFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
