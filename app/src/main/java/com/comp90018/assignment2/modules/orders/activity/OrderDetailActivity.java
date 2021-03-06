package com.comp90018.assignment2.modules.orders.activity;

import static com.comp90018.assignment2.utils.Constants.ORDERS_COLLECTION;
import static com.comp90018.assignment2.utils.Constants.PRODUCT_COLLECTION;
import static com.comp90018.assignment2.utils.Constants.USERS_COLLECTION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.comp90018.assignment2.modules.orders.adapter.PurchasedAdapter;
import com.google.firebase.Timestamp;
import com.bumptech.glide.Glide;
import com.comp90018.assignment2.R;
import com.comp90018.assignment2.databinding.ActivityOrderDetailBinding;
import com.comp90018.assignment2.databinding.ActivityProductDetailBinding;
import com.comp90018.assignment2.dto.ProductDTO;
import com.comp90018.assignment2.dto.UserDTO;
import com.comp90018.assignment2.dto.OrderDTO;
import com.comp90018.assignment2.modules.product.activity.ProductDetailActivity;
import com.comp90018.assignment2.modules.users.me.activity.EditProfileActivity;
import com.comp90018.assignment2.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.luck.picture.lib.tools.ToastUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import cn.jpush.im.android.api.JMessageClient;
import me.leefeng.promptlibrary.PromptDialog;

public class OrderDetailActivity extends AppCompatActivity {
    private static final String TAG = "[dev]OrderDetail";
    private ActivityOrderDetailBinding binding;
    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private UserDTO currentUserDTO;
    private OrderDTO currentOrderDTO;
    private Context context;
    private PromptDialog dialog;
    private PurchasedAdapter purchasedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new PromptDialog(this);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        ProductDTO productDTO = (ProductDTO) intent.getParcelableExtra("productDTO");
        UserDTO sellerDTO = (UserDTO) intent.getParcelableExtra("sellerDTO");
        OrderDTO orderDTO = (OrderDTO) intent.getParcelableExtra("orderDTO");
        UserDTO buyerDTO = (UserDTO) intent.getParcelableExtra("buyerDTO");
        UserDTO userDTO = (UserDTO) intent.getParcelableExtra("userDTO");
        DocumentReference sellerDocReference = orderDTO.getSeller_ref(); //get buyer info
        DocumentReference currentUserReference = db.collection(USERS_COLLECTION).document(firebaseAuth.getCurrentUser().getUid());//get current user info
        DocumentReference buyerDocReference = orderDTO.getBuyer_ref();
        String seller_id = sellerDocReference.getId();//get buyer id
        String current_user_id = currentUserReference.getId(); //get current user id
        String buyer_id  = buyerDocReference.getId();

        if (seller_id.equals(current_user_id)) { //if buyer = current user
            binding.orderRefundBtn.setVisibility(View.GONE); //button disappear
            binding.refundAddress.setVisibility(View.GONE);
            binding.receivingConfirmationBtn.setVisibility(View.GONE);
            if(orderDTO.getStatus()==Constants.ON_REFUND || orderDTO.getStatus()==Constants.ON_REFUND_DELIVERING){
                Toast.makeText(binding.getRoot().getContext(), "The buyer wants to refund this item", Toast.LENGTH_SHORT).show();
            }
            else{
                binding.orderAgreeBtn.setVisibility(View.GONE);
                binding.orderDisagreeBtn.setVisibility(View.GONE);
            }
            if(orderDTO.getStatus()!= Constants.WAITING_DELIVERY){
                binding.trackingIdBtn.setVisibility(View.GONE);
                binding.inputTrackingId.setVisibility(View.GONE);
            }
        }else if(buyer_id.equals(current_user_id)){
            binding.orderAgreeBtn.setVisibility(View.GONE);
            binding.orderDisagreeBtn.setVisibility(View.GONE);
            binding.trackingIdBtn.setVisibility(View.GONE);
            binding.inputTrackingId.setVisibility(View.GONE);
        }

        // Refund - purchased
        if (orderDTO.getStatus()!=Constants.WAITING_DELIVERY && orderDTO.getStatus()!=Constants.SUCCESSFUL_NOT_COMMENT ){
            buttonColor(binding.orderRefundBtn);
            binding.refundAddress.setVisibility(View.GONE);
        }
        if(orderDTO.getStatus()!= Constants.DELIVERING){
            binding.receivingConfirmationBtn.setVisibility(View.GONE);
        }
        /*
        if (orderDTO.getStatus()!=Constants.SUCCESSFUL_NOT_COMMENT){
            binding.refundAddress.setVisibility(View.GONE);
        }
        */
        binding.orderRefundBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(orderDTO.getStatus()==Constants.WAITING_DELIVERY) {
                    db.collection(ORDERS_COLLECTION)
                            .document(orderDTO.getId())
                            .update("status", Constants.ON_REFUND);
                    orderDTO.setStatus(Constants.ON_REFUND);
                    buttonColor(binding.orderRefundBtn);
                    Toast.makeText(binding.getRoot().getContext(), "We will notify your seller of the refund", Toast.LENGTH_SHORT).show();

                    finish();
                    Intent intent = new Intent(binding.getRoot().getContext(), PurchasedActivity.class);
                    startActivity(intent);
                }else if(orderDTO.getStatus()==Constants.SUCCESSFUL_NOT_COMMENT){
                    String address = binding.refundAddress.getText().toString();
                    String addressRegex = "^[a-zA-Z0-9 !?,.;@\"'()-]{0,40}$";
                    // No password regex, because it is login
                    if (!address.matches(addressRegex)) {
                        new AlertDialog.Builder(OrderDetailActivity.this).setMessage("address should be letters, numbers, common symbols and space").setPositiveButton("ok", null).show();
                        return;
                    }else{
                        db.collection(ORDERS_COLLECTION)
                                .document(orderDTO.getId())
                                .update("status", Constants.ON_REFUND_DELIVERING);
                        orderDTO.setStatus(Constants.ON_REFUND_DELIVERING);
                        buttonColor(binding.orderRefundBtn);
                        Toast.makeText(binding.getRoot().getContext(), "We will notify your seller of the refund", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(binding.getRoot().getContext(), PurchasedActivity.class);
                        startActivity(intent);
                    }

                }else if((orderDTO.getStatus()==Constants.ON_REFUND) || (orderDTO.getStatus()==Constants.ON_REFUND_DELIVERING)) {
                    Toast.makeText(binding.getRoot().getContext(), "The item is refunding, please wait", Toast.LENGTH_SHORT).show();
                }else if(orderDTO.getStatus()==Constants.REFUNDED){
                    Toast.makeText(binding.getRoot().getContext(), "The item has been refunded", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(binding.getRoot().getContext(), "The item cannot refund now", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //
        // Confirm refund - sold
        if(orderDTO.getStatus()!=Constants.ON_REFUND){
            buttonColor(binding.orderAgreeBtn);
            buttonColor(binding.orderDisagreeBtn);
        }
        binding.orderAgreeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(orderDTO.getStatus()==Constants.ON_REFUND || orderDTO.getStatus()==Constants.ON_REFUND_DELIVERING) {
                    db.collection(ORDERS_COLLECTION)
                            .document(orderDTO.getId())
                            .update("status", Constants.REFUNDED);
                    orderDTO.setStatus(Constants.REFUNDED);
                    db.collection(PRODUCT_COLLECTION)
                            .document(productDTO.getId())
                            .update("status", Constants.PUBLISHED);
                    productDTO.setStatus(Constants.PUBLISHED);
                    buttonColor(binding.orderAgreeBtn);
                    buttonColor(binding.orderDisagreeBtn);
                    Toast.makeText(binding.getRoot().getContext(), "You agreed to refund", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(binding.getRoot().getContext(), SoldActivity.class);
                    startActivity(intent);

                }else{
                    Toast.makeText(binding.getRoot().getContext(), "The buyer did not request a refund", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.orderDisagreeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(orderDTO.getStatus()==Constants.ON_REFUND) {
                    db.collection(ORDERS_COLLECTION)
                            .document(orderDTO.getId())
                            .update("status", Constants.WAITING_DELIVERY);
                    orderDTO.setStatus(Constants.WAITING_DELIVERY);
                    buttonColor(binding.orderAgreeBtn);
                    buttonColor(binding.orderDisagreeBtn);
                    Toast.makeText(binding.getRoot().getContext(), "You disagreed to refund", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(binding.getRoot().getContext(), SoldActivity.class);
                    startActivity(intent);
                }else if(orderDTO.getStatus()==Constants.ON_REFUND_DELIVERING){
                    db.collection(ORDERS_COLLECTION)
                            .document(orderDTO.getId())
                            .update("status", Constants.SUCCESSFUL_NOT_COMMENT);
                    orderDTO.setStatus(Constants.SUCCESSFUL_NOT_COMMENT);
                    buttonColor(binding.orderAgreeBtn);
                    buttonColor(binding.orderDisagreeBtn);
                    Toast.makeText(binding.getRoot().getContext(), "You disagreed to refund", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(binding.getRoot().getContext(), SoldActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(binding.getRoot().getContext(), "The buyer did not request a refund", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.trackingIdBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                binding.inputTrackingId.setHint("input tracking id");
                String trackingid = binding.inputTrackingId.getText().toString();
                String trackingidbox = "^[a-zA-Z0-9]{8,20}$";
                if (!trackingid.matches(trackingidbox)) {
                    new AlertDialog.Builder(OrderDetailActivity.this).setMessage("address should be letters, numbers").setPositiveButton("ok", null).show();
                    return;
                }
                db.collection(ORDERS_COLLECTION)
                        .document(orderDTO.getId())
                        .update("status", Constants.DELIVERING);
                db.collection(ORDERS_COLLECTION)
                        .document(orderDTO.getId())
                        .update("tracking_info", "delivering");
                db.collection(ORDERS_COLLECTION)
                        .document(orderDTO.getId())
                        .update("tracking_id",trackingid);
                Toast.makeText(binding.getRoot().getContext(), "Loading", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(OrderDetailActivity.this,SoldActivity.class);
                startActivity(intent);
            }
        });

        binding.receivingConfirmationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                db.collection(ORDERS_COLLECTION)
                        .document(orderDTO.getId())
                        .update("status", Constants.SUCCESSFUL_NOT_COMMENT);
                db.collection(ORDERS_COLLECTION)
                        .document(orderDTO.getId())
                        .update("tracking_info", "sent");
                Toast.makeText(binding.getRoot().getContext(), "Loading", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(OrderDetailActivity.this,PurchasedActivity.class);
                startActivity(intent);
            }
        }
        );



        switch (orderDTO.getStatus()) {
            case Constants.WAITING_DELIVERY:
                binding.orderDetailOrderStatus.setText("Waiting delivery");
                break;
            case Constants.DELIVERING:
                binding.orderDetailOrderStatus.setText("Delivering");
                break;
            case Constants.SUCCESSFUL_NOT_COMMENT:
                binding.orderDetailOrderStatus.setText("Successful not comment");
                break;
            case Constants.SUCCESSFUL_COMMENT:
                binding.orderDetailOrderStatus.setText("Successful comment");
                break;
            case Constants.CANCELED:
                binding.orderDetailOrderStatus.setText("Canceled");
                break;
            case Constants.ON_REFUND:
                binding.orderDetailOrderStatus.setText("On refund");
                break;
            case Constants.ON_REFUND_DELIVERING:
                binding.orderDetailOrderStatus.setText("On refund delivering");
                break;
            case Constants.REFUNDED:
                binding.orderDetailOrderStatus.setText("Refunded");
                break;
        }
        binding.orderDetailDeliveryStatus.setText("Delivery Status: "+orderDTO.getTracking_info().toString());
        binding.orderDetailDeliveryTrackingId.setText(orderDTO.getTracking_id().toString());


        if (productDTO.getImage_address() == null
                || productDTO.getImage_address().get(0) == null
                || productDTO.getImage_address().get(0).equals("")
                || productDTO.getImage_address().get(0).equals("default")
                || productDTO.getImage_address().get(0).equals("gs://comp90018-mobile-caa7c.appspot.com/public/default.png")) {
            binding.orderDetailPdtImg.setImageResource(R.drawable.default_image);
        } else {
            // only show the first img
            // storage Reference of firebase
            StorageReference imgReference = storage.getReferenceFromUrl(productDTO.getImage_address().get(0));

            // query image with the reference
            Glide.with(OrderDetailActivity.this)
                    .load(imgReference)
                    .into(binding.orderDetailPdtImg);
        }
        String descriptionCut;
        if (productDTO.getDescription().length() > 33) {
            descriptionCut = productDTO.getDescription().substring(0, 33) + "...";
        } else {
            descriptionCut = productDTO.getDescription();
        }
        binding.orderDetailPdtName.setText(descriptionCut);


        double price = productDTO.getPrice();
        String formattedPriceText;

        // Use BigDecimal to round, reserving one decimal place
        price = new BigDecimal(price).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (price < 1000) {
            formattedPriceText = String.valueOf(price);
        } else {
            // e.g 8750 to 8.8k
            int numThousands = new BigDecimal((int) (price / 1000)).setScale(1, BigDecimal.ROUND_HALF_UP).intValue();
            formattedPriceText = String.valueOf(numThousands) + "k";
        }
        // currency info
        if (productDTO.getCurrency() == Constants.AUS_DOLLAR) {
            formattedPriceText = "$" + formattedPriceText;
        } else {
            formattedPriceText = "$" + formattedPriceText;
        }
        binding.orderDetailTotalPrice.setText(formattedPriceText);
        binding.orderDetailReceiverName.setText(buyerDTO.getNickname());
        binding.orderDetailReceiverEmail.setText(buyerDTO.getEmail());
        binding.orderDetailReceiverAddr.setText(buyerDTO.getLocation_text().toString());
        binding.orderDetailSellerUsername.setText(sellerDTO.getNickname());



        binding.orderDetailId.setText(orderDTO.getId());
        Button copy_btn = binding.orderDetailCopyOrderIdBtn;
        copy_btn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("EditText", binding.orderDetailId.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(binding.getRoot().getContext(), "Copied", Toast.LENGTH_SHORT).show();
            }
        });


        String format =  "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Timestamp time = orderDTO.getCreated_time();
        binding.orderDetailTransactionDate.setText(time.toDate().toString());

        binding.orderDetailBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // binding.orderDetailOrderStatus.setText("Retrieve Order Status"); // already binded
        //binding.orderDetailDeliveryStatus.setText("Retrieve delivery Status"); // already binded
        //binding.orderDetailDeliveryTrackingId.setText("Retrieve Tracking id, CAN be Null"); // already binded

        binding.orderDetailPdtName.setText(productDTO.getDescription());
        //binding.orderDetailPdtPrice.setText("$ + retrieve pdt price");
        //binding.orderDetailPdtQuantity.setText("retrieve quantity");

        //binding.orderDetailTotalPrice.setText("calculate product of price and quantity");
        //binding.orderDetailReceiverName.setText("retrieve receiver's name"); // already binded
        //binding.orderDetailReceiverPhone.setText("retrieve receiver's phone number");

        binding.orderDetailReceiverAddr.setText(orderDTO.getAddress());



        /*
        binding.orderDetailConnectSellerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = "go to connect seller page";
            }
        });*/

    }
    @Override
    public void onResume() {
        super.onResume();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }

    private void buttonColor(Button btn){
        btn.setBackgroundColor(Color.parseColor("#D3D3D3"));
        btn.setTextColor(Color.parseColor("#778899"));
    }
}