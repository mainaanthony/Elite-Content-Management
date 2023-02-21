package com.paulbrian.elitecontentmanagement.admin.fradments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paulbrian.elitecontentmanagement.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ApproveContentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApproveContentsFragment extends Fragment {
    private Spinner spinnerCategories;
    private ProgressDialog progressDialog;
    private ApproveContentsFragmentAdapter viewContentCreatorsFragmentAdapter;
    private TextView textViewError;
    private FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;

    ArrayList<Map<String , String >> contents = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ApproveContentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ApproveContentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ApproveContentsFragment newInstance(String param1, String param2) {
        ApproveContentsFragment fragment = new ApproveContentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_approve_contents, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        textViewError = view.findViewById(R.id.textViewError);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        viewContentCreatorsFragmentAdapter = new ApproveContentsFragmentAdapter(this, getContext(), contents);
        recyclerView.setAdapter(viewContentCreatorsFragmentAdapter);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Fetching Unapproved Contents");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean anContentExists = false;

                for (DataSnapshot dataSnapshotTicket: snapshot.getChildren()){
                    if (dataSnapshotTicket.hasChild("notApproved")){
                        anContentExists = true;
                        HashMap<String ,String > hashMap = new HashMap<>();
                        hashMap.put("key", dataSnapshotTicket.getKey());

                        for (DataSnapshot dataSnapshotValue: dataSnapshotTicket.getChildren()){
                            hashMap.put(dataSnapshotValue.getKey(), dataSnapshotValue.getValue().toString());
                        }
                        contents.add(hashMap);
                        viewContentCreatorsFragmentAdapter.notifyDataSetChanged();
                    }

                }

                if (!anContentExists){
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("No pending contents to approve");
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        return view;
    }
}

class ApproveContentsFragmentAdapter extends RecyclerView.Adapter<ApproveContentsFragmentAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private ApproveContentsFragmentAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;
    private Fragment fragment;


    // data is passed into the constructor
    public ApproveContentsFragmentAdapter(Fragment fragment, Context context, ArrayList<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.fragment = fragment;
    }

    // inflates the row layout from xml when needed
    @Override
    public ApproveContentsFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_approve_content, parent, false);
        return new ApproveContentsFragmentAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ApproveContentsFragmentAdapter.ViewHolder holder, int position) {
        message = mData.get(position);

        String key = message.get("key");


        holder.textViewTitle.setText(message.get("title"));
        holder.textViewDescription.setText(message.get("description"));
        Glide.with(context).load(message.get("image")).into(holder.imageView);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents");


        holder.buttonApprove.setOnClickListener(v -> {
            databaseReference.child(key).child("notApproved").removeValue();

            holder.buttonApprove.setText("Content Approved");
            holder.buttonDeny.setVisibility(View.GONE);
            holder.buttonApprove.setClickable(false);
        });

        holder.buttonDeny.setOnClickListener(v -> {
            databaseReference.child(key).removeValue();

            holder.buttonDeny.setText("Content Disapproved");
            holder.buttonApprove.setVisibility(View.GONE);
            holder.buttonDeny.setClickable(false);
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
        Button buttonApprove, buttonDeny;
        ImageView imageView;
        LinearLayout linearLayoutPay;
        EditText editTextMobile;

        ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            editTextMobile = itemView.findViewById(R.id.editTextMobile);

            buttonApprove = itemView.findViewById(R.id.buttonApprove);
            buttonDeny = itemView.findViewById(R.id.buttonDeny);
            imageView  = itemView.findViewById(R.id.imageView);
            linearLayoutPay  = itemView.findViewById(R.id.linearLayoutPay);

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
    public void setClickListener(ApproveContentsFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
