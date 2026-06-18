package com.prateek.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// this is not a table in DB
// get api data -> update fields of this class and create a chart on website for eg for past 7 days variations in price of bitcoin.

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketChartData {
    private long timestamp;
    private double price;
}
