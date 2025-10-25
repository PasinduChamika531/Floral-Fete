package com.example.floralfete.drawer2.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.floralfete.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.Legend;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FirebaseFirestore firestore;
    private TextView totalProductCount, totalUserCount, totalEarnings, totalFlowerTypeCount;
    private PieChart pieChart;
    private BarChart barChart;

    public DashboardFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);


        firestore = FirebaseFirestore.getInstance();


        totalProductCount = rootView.findViewById(R.id.totalProductCount);
        totalUserCount = rootView.findViewById(R.id.totalUserCount);
        totalEarnings = rootView.findViewById(R.id.totalEarnings);
        totalFlowerTypeCount = rootView.findViewById(R.id.totalFlowerTypeCount);


        pieChart = rootView.findViewById(R.id.pieChart);
        barChart = rootView.findViewById(R.id.barChart);


        loadDashboardData();

        return rootView;
    }

    private void loadDashboardData() {

        CollectionReference productRef = firestore.collection("products");
        productRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            totalProductCount.setText("Total Products: " + queryDocumentSnapshots.size());
        });


        CollectionReference userRef = firestore.collection("user");
        userRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            totalUserCount.setText("Total Users: " + queryDocumentSnapshots.size());
        });

        CollectionReference orderRef = firestore.collection("orders");
        orderRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            double earnings = 0;
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Double totalAmount = document.getDouble("totalAmount");
                if (totalAmount != null) {
                    earnings += totalAmount;
                }
            }
            totalEarnings.setText("Total Earnings: Rs." + earnings);
        });


        CollectionReference flowerTypeRef = firestore.collection("flowerTypes");
        flowerTypeRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            totalFlowerTypeCount.setText("Total Flower Types: " + queryDocumentSnapshots.size());
        });


        orderRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            int delivered = 0, packing = 0, shipped = 0, processing = 0;
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                String status = document.getString("orderStatus");
                if (status != null) {
                    if (status.equals("Delivered")) {
                        delivered++;
                    } else if (status.equals("Packing")) {
                        packing++;
                    } else if (status.equals("Shipped")) {
                        shipped++;
                    } else if (status.equals("Processing")) {
                        processing++;
                    }
                }
            }


            ArrayList<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(delivered, "Delivered"));
            pieEntries.add(new PieEntry(packing, "Packing"));
            pieEntries.add(new PieEntry(shipped, "Shipped"));
            pieEntries.add(new PieEntry(processing, "Processing"));

            ArrayList<Integer> colors = new ArrayList<>();
            colors.add(Color.parseColor("#4CAF50"));  // Green for Delivered
            colors.add(Color.parseColor("#FFEB3B"));  // Yellow for Packing
            colors.add(Color.parseColor("#2196F3"));  // Blue for Shipped
            colors.add(Color.parseColor("#FF5722"));  // Red for Processing


            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setColors(colors);
            PieData pieData = new PieData(pieDataSet);
            pieData.setValueTextSize(16);
            pieChart.setData(pieData);
            pieChart.setCenterText("Order Status");
            pieChart.setCenterTextSize(18);
            pieChart.setDescription(null);
            pieChart.invalidate(); // refresh chart
        });




        orderRef.get().addOnSuccessListener(orderSnapshots -> {
            if (orderSnapshots.isEmpty()) {
                Log.d("BarChartDebug", "No orders found in Firestore.");
                return;
            }

            //Count total sold quantities per product
            Map<String, Integer> productSales = new HashMap<>();
            for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                List<Map<String, Object>> products = (List<Map<String, Object>>) orderDoc.get("products");
                if (products != null) {
                    for (Map<String, Object> product : products) {
                        String productId = (String) product.get("productId");
                        Long quantity = (Long) product.get("quantity");
                        if (productId != null && quantity != null) {
                            productSales.put(productId, productSales.getOrDefault(productId, 0) + quantity.intValue());
                        }
                    }
                }
            }

            if (productSales.isEmpty()) {
                Log.d("BarChartDebug", "No product sales data.");
                return;
            }

            //Sort and take top 4 products
            List<Map.Entry<String, Integer>> sortedProductSales = new ArrayList<>(productSales.entrySet());
            sortedProductSales.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

            // Limit to 4 most sold products
            int topN = Math.min(sortedProductSales.size(), 4);
            final List<Map.Entry<String, Integer>> topProducts = new ArrayList<>(sortedProductSales.subList(0, topN));
            if (topN > 0) {
                sortedProductSales = sortedProductSales.subList(0, topN);
            } else {
                Log.d("BarChartDebug", "Not enough data to display.");
                return;
            }

            // Extract product IDs
            List<String> productIds = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : sortedProductSales) {
                productIds.add(entry.getKey());
            }

            //Fetch product names from Firestore
            FirebaseFirestore.getInstance().collection("products")
                    .whereIn(FieldPath.documentId(), productIds)
                    .get().addOnSuccessListener(productSnapshots -> {
                        if (productSnapshots.isEmpty()) {
                            Log.d("BarChartDebug", "No product names found.");
                            return;
                        }

                        //Map product names to sales count
                        Map<String, String> productNameMap = new HashMap<>();
                        for (QueryDocumentSnapshot productDoc : productSnapshots) {
                            productNameMap.put(productDoc.getId(), productDoc.getString("name"));
                        }

                        //Prepare Bar Chart Data
                        ArrayList<BarEntry> barEntries = new ArrayList<>();
                        ArrayList<String> productNames = new ArrayList<>();
                        int index = 0;

                        for (Map.Entry<String, Integer> entry : topProducts) {
                            String productId = entry.getKey();
                            int soldQuantity = entry.getValue();
                            String productName = productNameMap.getOrDefault(productId, "Unknown");

                            Log.d("BarChartDebug", "Product: " + productName + ", Sales: " + soldQuantity);
                            barEntries.add(new BarEntry(index, soldQuantity));
                            productNames.add(productName);
                            index++;
                        }

                        //Update the Bar Chart
                        BarDataSet barDataSet = new BarDataSet(barEntries, "Most Sold Flowers");

                        List<Integer> colors = Arrays.asList(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW);
                        barDataSet.setColors(colors);
                        barDataSet.setValueTextSize(12f);
                        barDataSet.setValueTextColor(Color.BLACK);

                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setGranularity(1f);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setLabelRotationAngle(-35);
                        xAxis.setDrawGridLines(false);
                        xAxis.setLabelCount(productNames.size());
                        xAxis.setAvoidFirstLastClipping(true);

                        Legend legend = barChart.getLegend();
                        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                        legend.setWordWrapEnabled(true);
                        legend.setTextSize(14f);
                        legend.setXEntrySpace(5f);
                        legend.setYEntrySpace(5f);
                        legend.setDrawInside(false);

                        LegendEntry[] legendEntries = new LegendEntry[productNames.size()];
                        for (int i = 0; i < productNames.size(); i++) {
                            LegendEntry entry = new LegendEntry();
                            entry.label = productNames.get(i);
                            entry.formColor = colors.get(i % colors.size());
                            legendEntries[i] = entry;
                        }
                        legend.setCustom(legendEntries);

                        BarData barData = new BarData(barDataSet);
                        barChart.setData(barData);
                        barChart.invalidate(); // Refresh the chart
                    });
        });



    }
}
