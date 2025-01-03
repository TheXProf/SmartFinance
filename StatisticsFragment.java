package com.tobrosgame.smartfinance.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.FragmentStatisticsBinding;
import com.tobrosgame.smartfinance.viewmodels.TransactionViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.Calendar;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private TransactionViewModel viewModel;

    // Renk paleti tanımlıyoruz - Material Design renklerini kullanıyoruz
    private final int[] chartColors = new int[]{
            Color.parseColor("#4CAF50"), // Yeşil
            Color.parseColor("#2196F3"), // Mavi
            Color.parseColor("#FFC107"), // Sarı
            Color.parseColor("#FF5722"), // Turuncu
            Color.parseColor("#9C27B0"), // Mor
            Color.parseColor("#F44336")  // Kırmızı
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCharts();
        observeData();
        setupPeriodSelector();
    }

    private void setupCharts() {
        // Pasta grafik ayarları
        setupPieChart(binding.expensePieChart);
        setupPieChart(binding.incomePieChart);

        // Çubuk grafik ayarları (gelecekte eklenecek)
        setupBarChart();
    }

    private void setupPieChart(PieChart chart) {
        // Temel grafik ayarları
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);

        // Ortadaki boşluk
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        // Animasyon
        chart.animateY(1400, Easing.EaseInOutQuad);

        // Açıklama (Legend) ayarları
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(getResources().getColor(R.color.text_primary));
        legend.setTextSize(12f);
    }

    private void updatePieChartData(PieChart chart, List<PieEntry> entries, String label) {
        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setColors(chartColors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();
    }

    private void observeData() {
        // Harcama kategorilerini gözlemle
        viewModel.getCategoryTotals().observe(getViewLifecycleOwner(), categoryTotals -> {
            if (categoryTotals != null) {
                updateExpenseChart(categoryTotals);
            }
        });

        // Gelir kategorilerini gözlemle
        viewModel.getIncomeCategoryTotals().observe(getViewLifecycleOwner(), categoryTotals -> {
            if (categoryTotals != null) {
                updateIncomeChart(categoryTotals);
            }
        });

        // Mali özeti gözlemle
        viewModel.getFinancialSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                updateSummaryCard(summary);
            }
        });
    }

    private void setupPeriodSelector() {
        // Dönem seçici için chip group ayarları
        binding.periodChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Calendar calendar = Calendar.getInstance();
            if (checkedId == R.id.chipWeek) {
                calendar.add(Calendar.DAY_OF_YEAR, -7);
            } else if (checkedId == R.id.chipMonth) {
                calendar.add(Calendar.MONTH, -1);
            } else if (checkedId == R.id.chipYear) {
                calendar.add(Calendar.YEAR, -1);
            }
            // Seçilen döneme göre verileri güncelle
            viewModel.setSelectedPeriod(calendar.getTime());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}