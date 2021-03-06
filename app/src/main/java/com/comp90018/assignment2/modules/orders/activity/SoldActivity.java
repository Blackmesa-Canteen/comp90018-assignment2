package com.comp90018.assignment2.modules.orders.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.comp90018.assignment2.utils.Constants.USERS_COLLECTION;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.comp90018.assignment2.R;
import com.comp90018.assignment2.databinding.ActivitySoldPdtListBinding;
import com.comp90018.assignment2.databinding.ItemSoldPdtListBinding;
import com.comp90018.assignment2.dto.OrderDTO;
import com.comp90018.assignment2.dto.ProductDTO;
import com.comp90018.assignment2.dto.UserDTO;

import com.comp90018.assignment2.modules.orders.adapter.SoldAdapter;
import com.comp90018.assignment2.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class SoldActivity extends AppCompatActivity {
    private static final String TAG = "Sold[dev]";
    private ActivitySoldPdtListBinding list_binding;
    private ItemSoldPdtListBinding item_binding;
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private List<OrderDTO> orderDTOList;
    private SoldAdapter adapter;
    private Button DetailButton;
    private WaveSwipeRefreshLayout mWaveSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list_binding = ActivitySoldPdtListBinding.inflate(getLayoutInflater());
        item_binding = ItemSoldPdtListBinding.inflate(getLayoutInflater());

        setContentView(list_binding.getRoot());

        mWaveSwipeRefreshLayout = (WaveSwipeRefreshLayout) list_binding.soldSwipe;
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(SoldActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        recyclerView = list_binding.soldList;
        recyclerView.setVisibility(View.VISIBLE);

        list_binding.soldBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //
        // attach no result alert view
        Intent intent = getIntent();
        orderDTOList = intent.getParcelableArrayListExtra("orderDTOList");
        if (orderDTOList != null && orderDTOList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            processData(orderDTOList);
            progressDialog.dismiss();
        } else if (orderDTOList != null && orderDTOList.size() == 0) {
            // show empty result alert
            recyclerView.setVisibility(View.GONE);
            progressDialog.dismiss();
        } else {
            // if the incoming list is null, for debug use
            recyclerView.setVisibility(View.VISIBLE);
            db.collection(Constants.ORDERS_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<OrderDTO> orderDTOList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            orderDTOList.add(document.toObject(OrderDTO.class));
                        }
                        processData(orderDTOList);
                        progressDialog.dismiss();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }
        mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                new RefreshTask().execute();
            }
        });
    }
    private class RefreshTask extends AsyncTask<Void, Void, String[]> {
        @SuppressLint("MissingPermission")
        @Override
        protected String[] doInBackground(Void... voids) {
            finish();
            //Intent intent = new Intent(getApplicationContext(), PurchasedActivity.class);
            startActivity(getIntent());
            return new String[0]; //can't convert the type to void, so have to return String[]
        }
        @Override
        protected void onPostExecute(String[] result) {
            // Call setRefreshing(false) when the list has been refreshed.
            mWaveSwipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(result);
        }
    }

    private void processData(List<OrderDTO> orderDTOList) {
        List<OrderDTO> publishedOrderDTOList = new ArrayList<>();
        for (OrderDTO orderDTO : orderDTOList) {
            DocumentReference sellerDocReference = orderDTO.getSeller_ref();
            DocumentReference currentUserReference = db.collection(USERS_COLLECTION).document(firebaseAuth.getCurrentUser().getUid());
            String id1 = sellerDocReference.getId();
            String id2 = currentUserReference.getId();
            if (id1.equals(id2)) {
                publishedOrderDTOList.add(orderDTO);
            }
        }

        adapter = new SoldAdapter(this, publishedOrderDTOList);
        recyclerView.setAdapter(adapter);

        // 1 columns grid
        GridLayoutManager gvManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gvManager);

        for (int index = 0; index < publishedOrderDTOList.size(); index++) {
            OrderDTO orderDTO = publishedOrderDTOList.get(index);
            int finalIndex = index;
            orderDTO.getBuyer_ref().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        UserDTO userDTO = document.toObject(UserDTO.class);
                        DocumentReference reference = document.getReference();
                        Log.d(TAG, "get user info: " + userDTO.getEmail());
                        // add to adapter and refresh it

                        adapter.addNewUserDtoInMap(reference, userDTO);
                        adapter.notifyItemChanged(finalIndex);
                    } else {
                        Log.w(TAG, "user info db connection failed");
                    }
                }
            });
            orderDTO.getSeller_ref().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        UserDTO userDTO = document.toObject(UserDTO.class);
                        DocumentReference reference = document.getReference();
                        Log.d(TAG, "get user info: " + userDTO.getEmail());
                        // add to adapter and refresh it

                        adapter.addNewUserDtoInMap(reference, userDTO);
                        adapter.notifyItemChanged(finalIndex);
                    } else {
                        Log.w(TAG, "user info db connection failed");
                    }
                }
            });
            orderDTO.getProduct_ref().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        ProductDTO productDTO = document.toObject(ProductDTO.class);
                        DocumentReference reference = document.getReference();
                        Log.d(TAG, "get user info: " + productDTO.getId());
                        // add to adapter and refresh it
                        adapter.addNewProductDtoInMap(reference, productDTO);
                        adapter.notifyItemChanged(finalIndex);
                    } else {
                        Log.w(TAG, "user info db connection failed");
                    }
                }
            });
        }
    }
}
