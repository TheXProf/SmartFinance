package com.tobrosgame.smartfinance.ui.home;

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
import androidx.recyclerview.widget.RecyclerView;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.FragmentHomeBinding;
import com.tobrosgame.smartfinance.viewmodels.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ViewModel'i başlat
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // View Binding kullanarak layout'u bağla
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        // RecyclerView'ı yapılandır
        transactionAdapter = new TransactionAdapter();
        binding.transactionsRecyclerView.setAdapter(transactionAdapter);
        binding.transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupFab() {
        // Yeni işlem ekleme butonunu yapılandır
        binding.addTransactionFab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                    R.id.action_home_to_addTransaction
            );
        });
    }

    private void observeData() {
        // Mali durum özetini gözlemle
        transactionViewModel.getFinancialSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                updateBalanceInfo(summary);
            }
        });

        // Son işlemleri gözlemle
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                transactionAdapter.submitList(transactions);
                // Eğer liste boşsa "işlem yok" mesajını göster
                binding.emptyStateLayout.setVisibility(
                        transactions.isEmpty() ? View.VISIBLE : View.GONE
                );
            }
        });

        // Yükleme durumunu gözlemle
        transactionViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingProgressBar.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );
        });
    }

    private void updateBalanceInfo(TransactionViewModel.FinancialSummary summary) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

        // Toplam bakiye
        binding.balanceAmountText.setText(formatter.format(summary.getBalance()));

        // Gelir
        binding.incomeAmountText.setText(formatter.format(summary.getTotalIncome()));

        // Gider
        binding.expenseAmountText.setText(formatter.format(summary.getTotalExpense()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Memory leak'i önlemek için binding'i temizle
    }
}
