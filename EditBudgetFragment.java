package com.tobrosgame.smartfinance.ui.budget;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.FragmentEditBudgetBinding;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.viewmodels.BudgetViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditBudgetFragment extends Fragment {
    private FragmentEditBudgetBinding binding;
    private BudgetViewModel viewModel;
    private Calendar startDate;
    private Calendar endDate;
    private Budget currentBudget;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy", new Locale("tr"));

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Fragment'e gönderilen bütçe ID'sini al
        long budgetId = getArguments() != null ? getArguments().getLong("budgetId", -1) : -1;
        if (budgetId == -1) {
            // Geçerli bir ID yoksa geri dön
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        // Bütçe verilerini yükle
        viewModel.loadBudget(budgetId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        observeViewModel();
    }

    private void setupViews() {
        // Kategori spinner'ını ayarla
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                viewModel.getAvailableCategories()
        );
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.categorySpinner.setAdapter(categoryAdapter);

        // Tarih seçicileri ayarla
        binding.startDateLayout.setEndIconOnClickListener(v -> showStartDatePicker());
        binding.startDateInput.setOnClickListener(v -> showStartDatePicker());

        binding.endDateLayout.setEndIconOnClickListener(v -> showEndDatePicker());
        binding.endDateInput.setOnClickListener(v -> showEndDatePicker());

        // Kaydet butonu
        binding.updateButton.setOnClickListener(v -> {
            if (validateInputs()) {
                showConfirmationDialog();
            }
        });

        // Sil butonu
        binding.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void observeViewModel() {
        // Bütçe verilerini gözlemle
        viewModel.getCurrentBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null) {
                currentBudget = budget;
                populateFields(budget);
            }
        });

        // Yükleme durumunu gözlemle
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.updateButton.setEnabled(!isLoading);
            binding.deleteButton.setEnabled(!isLoading);
        });

        // Hata durumunu gözlemle
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Başarı durumunu gözlemle
        viewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Snackbar.make(binding.getRoot(), success, Snackbar.LENGTH_SHORT).show();
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }
        });
    }

    private void populateFields(Budget budget) {
        // Kategori seçimini ayarla
        int categoryPosition = ((ArrayAdapter) binding.categorySpinner.getAdapter())
                .getPosition(budget.getCategory());
        binding.categorySpinner.setSelection(categoryPosition);

        // Tutar bilgisini ayarla
        binding.amountInput.setText(String.valueOf(budget.getPlannedAmount()));

        // Tarihleri ayarla
        startDate = Calendar.getInstance();
        startDate.setTime(budget.getStartDate());
        binding.startDateInput.setText(dateFormatter.format(startDate.getTime()));

        endDate = Calendar.getInstance();
        endDate.setTime(budget.getEndDate());
        binding.endDateInput.setText(dateFormatter.format(endDate.getTime()));

        // Not bilgisini ayarla
        binding.notesInput.setText(budget.getNotes());

        // Harcama istatistiklerini göster
        updateSpendingStats(budget);
    }

    private void updateSpendingStats(Budget budget) {
        double spentPercentage = (budget.getSpentAmount() / budget.getPlannedAmount()) * 100;
        binding.spendingProgress.setProgress((int) spentPercentage);

        // İlerleme çubuğu rengini duruma göre ayarla
        int color;
        if (spentPercentage >= 90) {
            color = requireContext().getColor(R.color.expense_red);
        } else if (spentPercentage >= 75) {
            color = requireContext().getColor(R.color.warning_yellow);
        } else {
            color = requireContext().getColor(R.color.income_green);
        }
        binding.spendingProgress.setIndicatorColor(color);

        // Harcama bilgilerini göster
        String spentText = String.format(Locale.getDefault(),
                "Harcanan: ₺%.2f / ₺%.2f (%%%.1f)",
                budget.getSpentAmount(),
                budget.getPlannedAmount(),
                spentPercentage
        );
        binding.spendingText.setText(spentText);
    }

    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Bütçeyi Güncelle")
                .setMessage("Bütçe bilgilerini güncellemek istediğinizden emin misiniz?")
                .setPositiveButton("Güncelle", (dialog, which) -> updateBudget())
                .setNegativeButton("İptal", null)
                .show();
    }

    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Bütçeyi Sil")
                .setMessage("Bu bütçeyi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
                .setPositiveButton("Sil", (dialog, which) -> deleteBudget())
                .setNegativeButton("İptal", null)
                .show();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Tutar kontrolü
        String amountStr = binding.amountInput.getText().toString();
        if (amountStr.isEmpty()) {
            binding.amountLayout.setError("Tutar girilmesi zorunludur");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    binding.amountLayout.setError("Tutar sıfırdan büyük olmalıdır");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.amountLayout.setError("Geçerli bir tutar giriniz");
                isValid = false;
            }
        }

        return isValid;
    }

    private void updateBudget() {
        if (currentBudget != null) {
            // Mevcut bütçeyi güncelle
            currentBudget.setPlannedAmount(
                    Double.parseDouble(binding.amountInput.getText().toString())
            );
            currentBudget.setCategory(binding.categorySpinner.getSelectedItem().toString());
            currentBudget.setStartDate(startDate.getTime());
            currentBudget.setEndDate(endDate.getTime());
            currentBudget.setNotes(binding.notesInput.getText().toString());

            viewModel.updateBudget(currentBudget);
        }
    }

    private void deleteBudget() {
        if (currentBudget != null) {
            viewModel.deleteBudget(currentBudget);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
