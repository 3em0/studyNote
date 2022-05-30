package org.richfaces.demo.datafilterslider;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Wesley
 * Date: Jan 26, 2007
 * Time: 8:21:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class DemoInventoryItem implements Serializable {

    String make;
    String model;
    String stock;
    String vin;

    BigDecimal mileage;
    BigDecimal mileageMarket;
    Integer price;
    BigDecimal priceMarket;

    int daysLive;
    BigDecimal changeSearches;
    BigDecimal changePrice;

    BigDecimal exposure;
    BigDecimal activity;
    BigDecimal printed;
    BigDecimal inquiries;


    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public BigDecimal getMileage() {
        return mileage;
    }

    public void setMileage(BigDecimal mileage) {
        this.mileage = mileage;
    }

    public BigDecimal getMileageMarket() {
        return mileageMarket;
    }

    public void setMileageMarket(BigDecimal mileageMarket) {
        this.mileageMarket = mileageMarket;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public BigDecimal getPriceMarket() {
        return priceMarket;
    }

    public void setPriceMarket(BigDecimal priceMarket) {
        this.priceMarket = priceMarket;
    }

    public int getDaysLive() {
        return daysLive;
    }

    public void setDaysLive(int daysLive) {
        this.daysLive = daysLive;
    }

    public BigDecimal getChangeSearches() {
        return changeSearches;
    }

    public void setChangeSearches(BigDecimal changeSearches) {
        this.changeSearches = changeSearches;
    }

    public BigDecimal getChangePrice() {
        return changePrice;
    }

    public void setChangePrice(BigDecimal changePrice) {
        this.changePrice = changePrice;
    }

    public BigDecimal getExposure() {
        return exposure;
    }

    public void setExposure(BigDecimal exposure) {
        this.exposure = exposure;
    }

    public BigDecimal getActivity() {
        return activity;
    }

    public void setActivity(BigDecimal activity) {
        this.activity = activity;
    }

    public BigDecimal getPrinted() {
        return printed;
    }

    public void setPrinted(BigDecimal printed) {
        this.printed = printed;
    }

    public BigDecimal getInquiries() {
        return inquiries;
    }

    public void setInquiries(BigDecimal inquiries) {
        this.inquiries = inquiries;
    }
}
