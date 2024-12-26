package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "category_seq")
    @SequenceGenerator(name = "category_seq")
    Long id;
    String cate;

    public Category() {
    }

    public Category(Long id, String cate) {
        this.id = id;
        this.cate = cate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", cate='" + cate + '\'' +
                '}';
    }
}
