package com.tobrosgame.smartfinance.ui.budget;

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
import com.tobrosgame.smartfinance.databinding.FragmentAddBudgetBinding;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.viewmodels.BudgetViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddBudgetFragment extends Fragment {
    private FragmentAddBudgetBinding binding;
    private BudgetViewModel viewModel;
    private Calendar startDate;
    private Calendar endDate;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy", new Locale("tr"));

    // Bütçe periyot seçenekleri
    private final String[] periodOptions = {"Aylık", "3 Aylık", "6 Aylık", "Yıllık"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Tarihleri başlangıçta bugün olarak ayarla
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1); // Varsayılan olarak 1 ay sonrası
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategorySpinner();
        setupPeriodSpinner();
        setupDatePickers();
        setupAmountInput();
        setupSaveButton();
        observeViewModel();
    }

    private void setupCategorySpinner() {
        // Kategori spinner'ını ayarla
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                viewModel.getAvailableCategories()
        );
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.categorySpinner.setAdapter(categoryAdapter);
    }

    private void setupPeriodSpinner() {
        // Periyot spinner'ını ayarla
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                periodOptions
        );
        periodAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.periodSpinner.setAdapter(periodAdapter);

        // Periyot seçimi değiştiğinde bitiş tarihini otomatik güncelle
        binding.periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateEndDateBasedOnPeriod(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateEndDateBasedOnPeriod(int position) {
        endDate.setTime(startDate.getTime()); // Başlangıç tarihinden başla
        switch (position) {
            case 0: // Aylık
                endDate.add(Calendar.MONTH, 1);
                break;
            case 1: // 3 Aylık
                endDate.add(Calendar.MONTH, 3);
                break;
            case 2: // 6 Aylık
                endDate.add(Calendar.MONTH, 6);
                break;
            case 3: // Yıllık
                endDate.add(Calendar.YEAR, 1);
                break;
        }
        binding.endDateInput.setText(dateFormatter.format(endDate.getTime()));
    }

    private void setupDatePickers() {
        // Başlangıç tarihi seçici
        binding.startDateLayout.setEndIconOnClickListener(v -> showStartDatePicker());
        binding.startDateInput.setOnClickListener(v -> showStartDatePicker());
        binding.startDateInput.setText(dateFormatter.format(startDate.getTime()));

        // Bitiş tarihi seçici
        binding.endDateLayout.setEndIconOnClickListener(v -> showEndDatePicker());
        binding.endDateInput.setOnClickListener(v -> showEndDatePicker());
        binding.endDateInput.setText(dateFormatter.format(endDate.getTime()));
    }

    private void showStartDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    startDate.set(year, month, dayOfMonth);
                    binding.startDateInput.setText(dateFormatter.format(startDate.getTime()));
                    // Başlangıç tarihi değiştiğinde periyoda göre bitiş tarihini güncelle
                    updateEndDateBasedOnPeriod(binding.periodSpinner.getSelectedItemPosition());
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    endDate.set(year, month, dayOfMonth);
                    binding.endDateInput.setText(dateFormatter.format(endDate.getTime()));
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        );
        // Başlangıç tarihinden önceki tarihleri seçmeyi engelle
        dialog.getDatePicker().setMinDate(startDate.getTimeInMillis());
        dialog.show();
    }

    private void setupAmountInput() {
        binding.amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateAmount(s.toString());
            }
        });
    }

    private void validateAmount(String amount) {
        if (amount.isEmpty()) {
            binding.amountLayout.setError("Tutar girilmesi zorunludur");
            return;
        }
        try {
            double value = Double.parseDouble(amount);
            if (value <= 0) {
                binding.amountLayout.setError("Tutar sıfırdan büyük olmalıdır");
            } else {
                binding.amountLayout.setError(null);
            }
        } catch (NumberFormatException e) {
            binding.amountLayout.setError("Geçerli bir tutar giriniz");
        }
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveBudget();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Tutar kontrolü
        if (binding.amountInput.getText().toString().isEmpty()) {
            binding.amountLayout.setError("Tutar girilmesi zorunludur");
            isValid = false;
        }

        // Kategori kontrolü
        if (binding.categorySpinner.getSelectedItem() == null) {
            Snackbar.make(binding.getRoot(), "Lütfen kategori seçiniz", Snackbar.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void saveBudget() {
        try {
            double amount = Double.parseDouble(binding.amountInput.getText().toString());
            String category = binding.categorySpinner.getSelectedItem().toString();
            String period = binding.periodSpinner.getSelectedItem().toString();

            Budget budget = new Budget(
                    category,
                    amount,
                    startDate.getTime(),
                    endDate.getTime(),
                    period
            );

            viewModel.createBudget(budget);
        } catch (NumberFormatException e) {
            binding.amountLayout.setError("Geçerli bir tutar giriniz");
        }
    }

    private void observeViewModel() {
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
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
