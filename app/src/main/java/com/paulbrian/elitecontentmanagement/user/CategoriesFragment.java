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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paulbrian.elitecontentmanagement.Constants;
import com.paulbrian.elitecontentmanagement.R;
import com.paulbrian.elitecontentmanagement.ViewContentActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesFragment extends Fragment {
    private Spinner spinnerCategories;
    private CategoriesFragmentAdapter categoriesFragmentAdapter;
    private ProgressDialog progressDialog;
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

    public CategoriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoriesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoriesFragment newInstance(String param1, String param2) {
        CategoriesFragment fragment = new CategoriesFragment();
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
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        textViewError = view.findViewById(R.id.textViewError);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoriesFragmentAdapter = new CategoriesFragmentAdapter(this, getContext(), contents);
        recyclerView.setAdapter(categoriesFragmentAdapter);

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        spinnerCategories = (Spinner) view.findViewById(R.id.spinnerCategories);
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadContents(spinnerCategories.getSelectedItem().toString());
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

        return view;
    }

    private void loadContents(String category) {
        contents.clear();
        categoriesFragmentAdapter.notifyDataSetChanged();
        textViewError.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Fetching Contents");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String uid = firebaseAuth.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean anContentExists = false;

                for (DataSnapshot dataSnapshotTicket: snapshot.getChildren()){
                    if (dataSnapshotTicket.hasChild("notApproved")){
                        continue;
                    }

                    if (dataSnapshotTicket.child("category").getValue().toString().equals(category)){

                            HashMap<String ,String > hashMap = new HashMap<>();
                            hashMap.put("key", dataSnapshotTicket.getKey());
                            if (dataSnapshotTicket.hasChild("unlocked/"+uid)){
                                hashMap.put("isUnlocked", "true");
                            }else {
                                hashMap.put("isUnlocked", "false");
                            }
                            anContentExists = true;
                            for (DataSnapshot dataSnapshotValue: dataSnapshotTicket.getChildren()){
                                hashMap.put(dataSnapshotValue.getKey(), dataSnapshotValue.getValue().toString());
                            }
                            contents.add(hashMap);
                            categoriesFragmentAdapter.notifyDataSetChanged();

                    }


                }

                if (!anContentExists){
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("No contents here yet");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

    }
}

class CategoriesFragmentAdapter extends RecyclerView.Adapter<CategoriesFragmentAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private CategoriesFragmentAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;
    private Fragment fragment;


    // data is passed into the constructor
    public CategoriesFragmentAdapter(Fragment fragment, Context context, ArrayList<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.fragment = fragment;
    }

    // inflates the row layout from xml when needed
    @Override
    public CategoriesFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_view_content, parent, false);
        return new CategoriesFragmentAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(CategoriesFragmentAdapter.ViewHolder holder, int position) {
        message = mData.get(position);

        String amount = message.get("amount");
        String key = message.get("key");
        String contentUrl = message.get("content");
        String uploader = message.get("uploader");
        String isUnlocked = message.get("isUnlocked");
        String uid = FirebaseAuth.getInstance().getUid();

        Glide.with(context).load(message.get("image")).into(holder.imageView);

        holder.textViewTitle.setText(message.get("title"));
        holder.textViewDescription.setText(message.get("description"));


        if(uploader.equals(uid)){
            holder.buttonUnlock.setText("My Content");

            holder.buttonUnlock.setOnClickListener(v -> {
                Intent intent = new Intent(context, ViewContentActivity.class);
                intent.putExtra("contentUrl", contentUrl);
                intent.putExtra("ownContent", true);
                intent.putExtra("key", key);

                context.startActivity(intent);


            });
        }else if (isUnlocked.equals("true")){
            holder.buttonUnlock.setText("Content Unlocked");
            holder.buttonUnlock.setOnClickListener(v -> {
                Intent intent = new Intent(context, ViewContentActivity.class);
                intent.putExtra("contentUrl", contentUrl);
                intent.putExtra("key", key);

                context.startActivity(intent);


            });
        }else {
            holder.buttonUnlock.setText("Unlock Content (Ksh. " + message.get("amount") + ")");

            holder.buttonUnlock.setOnClickListener(v -> {
                if (holder.linearLayoutPay.getVisibility() == View.GONE) {
                    holder.linearLayoutPay.setVisibility(View.VISIBLE);
                } else {
                    String mobile = holder.editTextMobile.getText().toString();

                    if (mobile.isEmpty()) {
                        Toast.makeText(context, "Enter Mpesa Number First", Toast.LENGTH_LONG).show();
                        holder.editTextMobile.requestFocus();
                        return;
                    }
                    if (mobile.startsWith("07")) mobile = mobile.replaceFirst("07", "2547");
                    else if (mobile.startsWith("01")) mobile = mobile.replaceFirst("01", "2541");

                    makeMpesaPayment(mobile, amount, "Elite Content App", contentUrl, key, holder.buttonUnlock, holder.linearLayoutPay);

                }

            });
        }

    }

    private void makeMpesaPayment(String mobile, String amount, String app, String contentUrl, String content, Button button, LinearLayout linearLayoutPay){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Loading m-pesa payment");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = Constants.SERVER_BASE_URL_GENERAL_APIS+"?action=mpesa&mobile="+mobile+"&amount="+amount+"&app="+app;
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    if (response != null) {
                        try {

                            if (response.getString("ResponseCode").equals("0")) {

                                Toast.makeText(context, "Enter Mpesa Pin to Pay", Toast.LENGTH_LONG).show();
                                String uid = firebaseAuth.getCurrentUser().getUid();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contents")
                                        .child(content).child("unlocked");
                                databaseReference.child(uid).setValue(true);

                                button.setClickable(false);
                                button.setText("Unlocked");
                                linearLayoutPay.setVisibility(View.GONE);

                                Intent intent = new Intent(context, ViewContentActivity.class);
                                intent.putExtra("contentUrl", contentUrl);

                                context.startActivity(intent);



                            }else {
                                Toast.makeText(context, "We could not process the request\n"+response.getString("ResponseCode"), Toast.LENGTH_LONG).show();

                            }
                        }catch (JSONException e){
                            Toast.makeText(context, "Mpesa error", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }
                    progressDialog.dismiss();

                }, error -> progressDialog.dismiss()
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                5*60*1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }


    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewTitle, textViewDescription;
        Button buttonUnlock;
        ImageView imageView;
        LinearLayout linearLayoutPay;
        EditText editTextMobile;

        ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            editTextMobile = itemView.findViewById(R.id.editTextMobile);

            buttonUnlock = itemView.findViewById(R.id.buttonUnlock);
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
    public void setClickListener(CategoriesFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
