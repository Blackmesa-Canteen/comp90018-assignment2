package com.comp90018.assignment2.db.repository;

@Deprecated
public class ProductRepository {
    /*单例的一堆代码*/
    private static ProductRepository instance;

    private ProductRepository() {
        // protect the constructor
    }

    @Deprecated
    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }

        return instance;
    }
}
