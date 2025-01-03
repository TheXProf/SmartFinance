package com.tobrosgame.smartfinance.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.FragmentBudgetDetailBinding;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.models.Transaction;
import com.tobrosgame.smartfinance.viewmodels.BudgetViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetDetailFragment extends Fragment {
    private FragmentBudgetDetailBinding binding;
    private BudgetViewModel viewModel;
    private BudgetTransactionAdapter transactionAdapter;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMM", new Locale("tr"));
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Bütçe ID'sini al ve yükle
        long budgetId = getArguments() != null ? getArguments().getLong("budgetId", -1) : -1;
        if (budgetId != -1) {
            viewModel.loadBudget(budgetId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupChart();
        observeViewModel();
        setupEditButton();
    }

    private void setupRecyclerView() {
        transactionAdapter = new BudgetTransactionAdapter();
        binding.transactionsRecyclerView.setAdapter(transactionAdapter);
        binding.transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupChart() {
        LineChart chart = binding.spendingChart;

        // Grafik genel ayarları
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        // X ekseni ayarları
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary));

        // Y ekseni ayarları
        chart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_secondary));
        chart.getAxisRight().setEnabled(false);

        // Açıklama ayarları
        chart.getLegend().setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void updateChart(Budget budget, List<Transaction> transactions) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        float totalSpent = 0;
        int index = 0;

        // İşlemleri tarihe göre sırala ve grafiğe ekle
        for (Transaction transaction : transactions) {
            totalSpent += transaction.getAmount();
            entries.add(new Entry(index, totalSpent));
            labels.add(dateFormatter.format(transaction.getDate()));
            index++;
        }

        // Veri setini oluştur
        LineDataSet dataSet = new LineDataSet(entries, "Toplam Harcama");
        dataSet.setColor(getResources().getColor(R.color.primary_variant));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.primary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        // Grafiği güncelle
        LineData lineData = new LineData(dataSet);
        binding.spendingChart.setData(lineData);
        binding.spendingChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.spendingChart.animateX(1000, Easing.EaseInOutQuad);
        binding.spendingChart.invalidate();
    }

    private void observeViewModel() {
        // Bütçe verilerini gözlemle
        viewModel.getCurrentBudget().observe(getViewLifecycleOwner(), this::updateBudgetDetails);

        // İşlemleri gözlemle
        viewModel.getBudgetTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                transactionAdapter.submitList(transactions);
                // Boş durum kontrolü
                binding.emptyStateLayout.setVisibility(
                        transactions.isEmpty() ? View.VISIBLE : View.GONE
                );
                // Bütçe varsa grafiği güncelle
                Budget currentBudget = viewModel.getCurrentBudget().getValue();
                if (currentBudget != null) {
                    updateChart(currentBudget, transactions);
                }
            }
        });
    }

    private void updateBudgetDetails(Budget budget) {
        if (budget != null) {
            // Başlık ve kategori bilgisi
            binding.titleText.setText(budget.getCategory());

            // Bütçe tutarı ve kullanım durumu
            binding.budgetAmountText.setText(currencyFormatter.format(budget.getPlannedAmount()));
            binding.spentAmountText.setText(currencyFormatter.format(budget.getSpentAmount()));

            double remainingAmount = budget.getPlannedAmount() - budget.getSpentAmount();
            binding.remainingAmountText.setText(currencyFormatter.format(remainingAmount));

            // İlerleme çubuğu
            double percentage = (budget.getSpentAmount() / budget.getPlannedAmount()) * 100;
            binding.budgetProgress.setProgress((int) percentage);

            // İlerleme çubuğu rengini duruma göre ayarla
            int color = getProgressColor(percentage);
            binding.budgetProgress.setIndicatorColor(color);

            // Tarih bilgileri
            String dateRange = String.format("%s - %s",
                    dateFormatter.format(budget.getStartDate()),
                    dateFormatter.format(budget.getEndDate()));
            binding.dateRangeText.setText(dateRange);

            // Not bilgisi
            binding.notesText.setText(budget.getNotes());
            binding.notesCard.setVisibility(
                    budget.getNotes() != null && !budget.getNotes().isEmpty()
                            ? View.VISIBLE : View.GONE
            );
        }
    }

    private int getProgressColor(double percentage) {
        if (percentage >= 90) {
            return getResources().getColor(R.color.expense_red);
        } else if (percentage >= 75) {
            return getResources().getColor(R.color.warning_yellow);
        } else {
            return getResources().getColor(R.color.income_green);
        }
    }

    private void setupEditButton() {
        binding.editButton.setOnClickListener(v -> {
            Budget currentBudget = viewModel.getCurrentBudget().getValue();
            if (currentBudget != null) {
                Bundle args = new Bundle();
                args.putLong("budgetId", currentBudget.getId());
                Navigation.findNavController(v)
                        .navigate(R.id.action_budgetDetail_to_editBudget, args);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}