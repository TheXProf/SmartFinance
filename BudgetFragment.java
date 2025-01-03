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
import com.tobrosgame.smartfinance.databinding.FragmentBudgetBinding;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.viewmodels.BudgetViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

public class BudgetFragment extends Fragment {
    private FragmentBudgetBinding binding;
    private BudgetViewModel viewModel;
    private BudgetAdapter budgetAdapter;

    // Para birimi formatı için
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFab();
        observeBudgetData();
        setupPullToRefresh();
    }

    private void setupRecyclerView() {
        // RecyclerView ayarları
        budgetAdapter = new BudgetAdapter(new BudgetAdapter.BudgetClickListener() {
            @Override
            public void onBudgetClick(Budget budget) {
                // Bütçe detay sayfasına yönlendir
                Bundle args = new Bundle();
                args.putLong("budgetId", budget.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_budget_to_budgetDetail, args);
            }

            @Override
            public void onEditClick(Budget budget) {
                // Bütçe düzenleme sayfasına yönlendir
                Bundle args = new Bundle();
                args.putLong("budgetId", budget.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_budget_to_editBudget, args);
            }
        });

        binding.budgetRecyclerView.setAdapter(budgetAdapter);
        binding.budgetRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupFab() {
        // Yeni bütçe ekleme butonu
        binding.addBudgetFab.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_budget_to_addBudget)
        );
    }

    private void observeBudgetData() {
        // Aktif bütçeleri gözlemle
        viewModel.getActiveBudgets().observe(getViewLifecycleOwner(), budgets -> {
            budgetAdapter.submitList(budgets);
            // Boş durum kontrolü
            binding.emptyStateLayout.setVisibility(
                    budgets != null && budgets.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        // Bütçe özetini gözlemle
        viewModel.getBudgetSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                updateSummaryCard(summary);
            }
        });

        // Aşılan bütçeleri gözlemle ve uyarı göster
        viewModel.getOverspentBudgets().observe(getViewLifecycleOwner(), overspentBudgets -> {
            if (overspentBudgets != null && !overspentBudgets.isEmpty()) {
                showOverspentWarning(overspentBudgets.size());
            }
        });

        // Yükleme durumunu gözlemle
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Hata durumlarını gözlemle
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateSummaryCard(BudgetViewModel.BudgetSummary summary) {
        // Toplam bütçe
        binding.totalBudgetText.setText(currencyFormatter.format(summary.getTotalPlanned()));

        // Harcanan miktar
        binding.spentAmountText.setText(currencyFormatter.format(summary.getTotalSpent()));

        // Kalan miktar
        double remaining = summary.getTotalPlanned() - summary.getTotalSpent();
        binding.remainingText.setText(currencyFormatter.format(remaining));

        // İlerleme çubuğunu güncelle
        int progress = (int) ((summary.getTotalSpent() / summary.getTotalPlanned()) * 100);
        binding.budgetProgressBar.setProgress(progress);

        // İlerleme çubuğu rengini duruma göre ayarla
        int color = getProgressColor(progress);
        binding.budgetProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private int getProgressColor(int progress) {
        if (progress >= 90) {
            return getResources().getColor(R.color.expense_red);
        } else if (progress >= 75) {
            return getResources().getColor(R.color.warning_yellow);
        } else {
            return getResources().getColor(R.color.income_green);
        }
    }

    private void showOverspentWarning(int count) {
        Snackbar.make(
                binding.getRoot(),
                getString(R.string.warning_overspent_budgets, count),
                Snackbar.LENGTH_LONG
        ).setAction("Göster", v -> {
            // Aşılan bütçeleri gösteren listeye yönlendir
            Navigation.findNavController(v).navigate(R.id.action_budget_to_overspentBudgets);
        }).show();
    }

    private void setupPullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshBudgets();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}