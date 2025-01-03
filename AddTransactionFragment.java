package com.tobrosgame.smartfinance.ui.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.tobrosgame.smartfinance.databinding.FragmentAddTransactionBinding;
import com.tobrosgame.smartfinance.models.Transaction;
import com.tobrosgame.smartfinance.viewmodels.TransactionViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private Calendar selectedDate;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy", new Locale("tr"));

    // İşlem kategorileri
    private final String[] incomeCategories = {"Maaş", "Ek Gelir", "Yatırım", "Diğer"};
    private final String[] expenseCategories = {"Market", "Fatura", "Ulaşım", "Yemek", "Sağlık", "Eğlence", "Diğer"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        selectedDate = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTypeToggle();
        setupDatePicker();
        setupAmountInput();
        setupCategorySpinner();
        setupObservers();
        setupSaveButton();
    }

    private void setupTypeToggle() {
        // İşlem tipi seçimi (Gelir/Gider) değiştiğinde kategorileri güncelle
        binding.typeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean isIncome = (checkedId == R.id.incomeButton);
                updateCategories(isIncome);
                // UI renklerini güncelle
                updateUIColors(isIncome);
            }
        });
    }

    private void setupDatePicker() {
        // Tarih seçici gösterimi
        binding.dateLayout.setEndIconOnClickListener(v -> showDatePicker());
        binding.dateInput.setOnClickListener(v -> showDatePicker());

        // Başlangıçta bugünün tarihini göster
        binding.dateInput.setText(dateFormatter.format(selectedDate.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    binding.dateInput.setText(dateFormatter.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupAmountInput() {
        // Para birimi formatlaması için TextWatcher
        binding.amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        // Sayısal değer kontrolü
                        double amount = Double.parseDouble(s.toString());
                        binding.amountLayout.setError(null);
                    } catch (NumberFormatException e) {
                        binding.amountLayout.setError("Geçerli bir tutar giriniz");
                    }
                }
            }
        });
    }

    private void setupCategorySpinner() {
        // Varsayılan olarak gider kategorilerini göster
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                expenseCategories
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.categorySpinner.setAdapter(adapter);
    }

    private void updateCategories(boolean isIncome) {
        // İşlem tipine göre kategori listesini güncelle
        String[] categories = isIncome ? incomeCategories : expenseCategories;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                categories
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.categorySpinner.setAdapter(adapter);
    }

    private void updateUIColors(boolean isIncome) {
        // İşlem tipine göre UI renklerini güncelle
        int color = requireContext().getColor(
                isIncome ? R.color.income_green : R.color.expense_red
        );
        binding.amountLayout.setStartIconTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void setupObservers() {
        // ViewModel'den gelen durumları gözlemle
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.saveButton.setEnabled(!isLoading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Snackbar.make(binding.getRoot(), success, Snackbar.LENGTH_SHORT).show();
                // İşlem başarılı olduğunda ana ekrana dön
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }
        });
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveTransaction();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Tutar kontrolü
        String amountStr = binding.amountInput.getText().toString();
        if (amountStr.isEmpty()) {
            binding.amountLayout.setError("Tutar girilmesi zorunludur");
            isValid = false;
        }

        // Başlık kontrolü
        String title = binding.titleInput.getText().toString();
        if (title.isEmpty()) {
            binding.titleLayout.setError("Başlık girilmesi zorunludur");
            isValid = false;
        }

        return isValid;
    }

    private void saveTransaction() {
        // Yeni işlem oluştur
        try {
            double amount = Double.parseDouble(binding.amountInput.getText().toString());
            String title = binding.titleInput.getText().toString();
            String category = binding.categorySpinner.getSelectedItem().toString();
            String description = binding.descriptionInput.getText().toString();
            String type = binding.typeToggleGroup.getCheckedButtonId() == R.id.incomeButton ?
                    "INCOME" : "EXPENSE";

            Transaction transaction = new Transaction(
                    title,
                    description,
                    amount,
                    category,
                    type,
                    selectedDate.getTime()
            );

            viewModel.addTransaction(transaction);
        } catch (NumberFormatException e) {
            binding.amountLayout.setError("Geçersiz tutar");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
