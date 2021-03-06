package com.comp90018.assignment2.dto;


import android.os.Parcel;
import android.os.Parcelable;

import com.comp90018.assignment2.network.MyGeoPoint;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Parcelable DTO that mapped from the database object
 * make it parcelable to let it be able to bundled between activities
 *
 * @author jing
 * @author Xiaotian Li
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO implements Parcelable {
    private String id;
    private String brand;
    private DocumentReference category_ref;
    private Integer currency;
    private String description;
    private Integer favorite_number;
    private ArrayList<String> image_address;
    @Deprecated
    private GeoPoint location_coordinate;
    private String location_text;
    private DocumentReference owner_ref;
    private Double price;
    private Timestamp publish_time;
    private Integer quality;
    private Integer status;
    @Deprecated
    private Double star_number;
    private DocumentReference sub_category_ref;
    private Integer view_number;
    private String geo_hash;

    private Double lat;
    private Double lng;

    protected ProductDTO(Parcel in) {
        id = in.readString();

        category_ref = FirebaseFirestore.getInstance().document(in.readString());
        owner_ref = FirebaseFirestore.getInstance().document(in.readString());
        sub_category_ref = FirebaseFirestore.getInstance().document(in.readString());
        location_coordinate = new GeoPoint(in.readDouble(), in.readDouble());
        if (in.readByte() == 0) {
            lat = null;
        } else {
            lat = in.readDouble();
        }

        if (in.readByte() == 0) {
            lng = null;
        } else {
            lng = in.readDouble();
        }

        brand = in.readString();
        if (in.readByte() == 0) {
            currency = null;
        } else {
            currency = in.readInt();
        }
        description = in.readString();
        if (in.readByte() == 0) {
            favorite_number = null;
        } else {
            favorite_number = in.readInt();
        }
        image_address = in.createStringArrayList();
        location_text = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        publish_time = in.readParcelable(Timestamp.class.getClassLoader());
        if (in.readByte() == 0) {
            quality = null;
        } else {
            quality = in.readInt();
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        if (in.readByte() == 0) {
            star_number = null;
        } else {
            star_number = in.readDouble();
        }

        if (in.readByte() == 0) {
            view_number = null;
        } else {
            view_number = in.readInt();
        }

        geo_hash = in.readString();
    }

    public static final Creator<ProductDTO> CREATOR = new Creator<ProductDTO>() {
        @Override
        public ProductDTO createFromParcel(Parcel in) {
            return new ProductDTO(in);
        }

        @Override
        public ProductDTO[] newArray(int size) {
            return new ProductDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);

        String category_ref_text = category_ref.getPath();
        dest.writeString(category_ref_text);

        String owner_ref_text = owner_ref.getPath();
        dest.writeString(owner_ref_text);

        String sub_category_ref_text = sub_category_ref.getPath();
        dest.writeString(sub_category_ref_text);

        // geo location
        dest.writeDouble(location_coordinate.getLatitude());
        dest.writeDouble(location_coordinate.getLongitude());
        if (lat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lat);
        }

        if (lng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lng);
        }

        dest.writeString(brand);
        if (currency == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(currency);
        }
        dest.writeString(description);
        if (favorite_number == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(favorite_number);
        }
        dest.writeStringList(image_address);
        dest.writeString(location_text);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        dest.writeParcelable(publish_time, flags);
        if (quality == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(quality);
        }
        if (status == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(status);
        }
        if (star_number == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(star_number);
        }

        if (view_number == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(view_number);
        }

        dest.writeString(geo_hash);
    }
}
