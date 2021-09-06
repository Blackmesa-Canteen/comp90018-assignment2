package com.comp90018.assignment2.dto;


import com.firebase.geofire.GeoLocation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * Data Model of Product
 *
 * @author jing
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO implements Serializable {
    private String brand;
    private DocumentReference category_ref;
    private Integer currency;
    private String description;
    private Integer favorite_number;
    private List<String> image_address;
    private GeoPoint location_coordinate;
    private String location_text;
    private DocumentReference owner_ref;
    private Double price;
    private Timestamp publish_time;
    private Integer quality;
    private Integer status;
    private Double star_number;
    private DocumentReference sub_category_ref;
    private String view_number;

}
