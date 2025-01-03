package com.tobrosgame.smartfinance.ui.settings;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.FragmentSettingsBinding;
import com.tobrosgame.smartfinance.utils.PreferenceManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.tobrosgame.smartfinance.utils.WorkManagerHelper;

import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private PreferenceManager preferenceManager;

    // Para birimi seçenekleri
    private final String[] currencies = {"TRY", "USD", "EUR", "GBP"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PreferenceManager'ı başlat
        preferenceManager = PreferenceManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCurrencySpinner();
        setupThemeSelection();
        setupNotificationSettings();
        setupBudgetAlertSettings();
        setupResetButton();

        // Mevcut ayarları yükle
        loadCurrentSettings();
    }

    private void setupCurrencySpinner() {
        // Para birimi spinner'ı için adapter oluştur
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                currencies
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.currencySpinner.setAdapter(adapter);

        // Para birimi değişikliğini dinle
        binding.currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = currencies[position];
                if (!selectedCurrency.equals(preferenceManager.getCurrency())) {
                    preferenceManager.setCurrency(selectedCurrency);
                    showRestartRecommendation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupThemeSelection() {
        // Tema seçimi için radio button'ları ayarla
        binding.themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int theme;
            if (checkedId == R.id.themeSystemDefault) {
                theme = -1; // Sistem varsayılanı
            } else if (checkedId == R.id.themeLightMode) {
                theme = 0; // Açık tema
            } else {
                theme = 1; // Koyu tema
            }

            if (theme != preferenceManager.getDarkMode()) {
                preferenceManager.setDarkMode(theme);
                updateTheme(theme);
            }
        });
    }

    private void setupNotificationSettings() {
        // Bildirim switch'ini ayarla
        binding.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setNotificationEnabled(isChecked);
            binding.notificationTimeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateNotificationSettings(isChecked);
        });

        // Bildirim saati seçimini ayarla
        binding.notificationTimeText.setOnClickListener(v -> showTimePickerDialog());
    }

    private void setupBudgetAlertSettings() {
        // Bütçe uyarı eşiği slider'ını ayarla
        binding.budgetAlertSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int threshold = (int) value;
                preferenceManager.setBudgetAlertThreshold(threshold);
                updateBudgetAlertText(threshold);
            }
        });
    }

    private void setupResetButton() {
        binding.resetSettingsButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Ayarları Sıfırla")
                    .setMessage("Tüm ayarlar varsayılan değerlerine döndürülecek. Bu işlem geri alınamaz.")
                    .setPositiveButton("Sıfırla", (dialog, which) -> {
                        preferenceManager.resetToDefaults();
                        loadCurrentSettings(); // Arayüzü güncelle
                        showRestartRecommendation();
                    })
                    .setNegativeButton("İptal", null)
                    .show();
        });
    }

    private void loadCurrentSettings() {
        // Para birimi ayarını yükle
        String currentCurrency = preferenceManager.getCurrency();
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currentCurrency)) {
                binding.currencySpinner.setSelection(i);
                break;
            }
        }

        // Tema ayarını yükle
        int darkMode = preferenceManager.getDarkMode();
        switch (darkMode) {
            case -1:
                binding.themeSystemDefault.setChecked(true);
                break;
            case 0:
                binding.themeLightMode.setChecked(true);
                break;
            case 1:
                binding.themeDarkMode.setChecked(true);
                break;
        }

        // Bildirim ayarlarını yükle
        boolean notificationsEnabled = preferenceManager.isNotificationEnabled();
        binding.notificationSwitch.setChecked(notificationsEnabled);
        binding.notificationTimeContainer.setVisibility(
                notificationsEnabled ? View.VISIBLE : View.GONE
        );
        updateNotificationTimeText(preferenceManager.getNotificationTime());

        // Bütçe uyarı eşiğini yükle
        int threshold = preferenceManager.getBudgetAlertThreshold();
        binding.budgetAlertSlider.setValue(threshold);
        updateBudgetAlertText(threshold);
    }

    private void showTimePickerDialog() {
        int currentMinutes = preferenceManager.getNotificationTime();
        int hour = currentMinutes / 60;
        int minute = currentMinutes % 60;

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute1) -> {
                    int newMinutes = hourOfDay * 60 + minute1;
                    preferenceManager.setNotificationTime(newMinutes);
                    updateNotificationTimeText(newMinutes);
                    updateNotificationSettings(true);
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void updateNotificationTimeText(int minutesOfDay) {
        int hour = minutesOfDay / 60;
        int minute = minutesOfDay % 60;
        binding.notificationTimeText.setText(
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        );
    }

    private void updateBudgetAlertText(int threshold) {
        binding.budgetAlertText.setText(
                String.format(Locale.getDefault(),
                        "Bütçe %d%% oranında kullanıldığında uyarı al", threshold)
        );
    }

    private void showRestartRecommendation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Yeniden Başlatma Önerisi")
                .setMessage("Yapılan değişikliklerin tam olarak uygulanması için uygulamayı yeniden başlatmanız önerilir.")
                .setPositiveButton("Tamam", null)
                .show();
    }

    private void updateTheme(int mode) {
        // Tema değişikliğini uygula
        // Not: Bu işlevi MainActivity'de implement edeceğiz
        if (getActivity() instanceof ThemeChangeListener) {
            ((ThemeChangeListener) getActivity()).onThemeChanged(mode);
        }
    }

    private void updateNotificationSettings(boolean enabled) {
        // Bildirim ayarlarını güncelle
        WorkManagerHelper workManagerHelper = new WorkManagerHelper(requireContext());
        if (enabled) {
            workManagerHelper.scheduleAllWork();
        } else {
            workManagerHelper.cancelAllWork();
        }
    }

    private void scheduleNotifications() {
        // Bildirimleri programla
        // Not: Bu işlevi daha sonra implement edeceğiz
    }

    private void cancelNotifications() {
        // Bildirimleri iptal et
        // Not: Bu işlevi daha sonra implement edeceğiz
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Tema değişikliği için interface
    public interface ThemeChangeListener {
        void onThemeChanged(int mode);
    }
}