package com.tobrosgame.smartfinance.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.ActivityMainBinding;
import com.tobrosgame.smartfinance.ui.settings.SettingsFragment;
import com.tobrosgame.smartfinance.utils.PreferenceManager;
import com.tobrosgame.smartfinance.utils.WorkManagerHelper;

public class MainActivity extends AppCompatActivity implements SettingsFragment.ThemeChangeListener {
    private ActivityMainBinding binding;
    private NavController navController;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tercihleri yükle ve temayı ayarla
        preferenceManager = PreferenceManager.getInstance(this);
        applyTheme(preferenceManager.getDarkMode());

        // View binding başlat
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Navigation controller'ı ayarla
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // Bottom navigation ile navigation controller'ı bağla
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        }

        // Bottom navigation görünürlüğünü yönet
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Bazı sayfalarda bottom navigation'ı gizle
            if (destination.getId() == R.id.addTransactionFragment ||
                    destination.getId() == R.id.addBudgetFragment) {
                binding.bottomNavigationView.setVisibility(View.GONE);
            } else {
                binding.bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        // İlk kurulum ve bildirimleri başlat
        setupInitialPreferences();
    }

    /**
     * İlk kurulum tercihlerini ve bildirim sistemini ayarlar.
     * Uygulama ilk kez açıldığında veya güncelleme sonrasında çağrılır.
     */
    private void setupInitialPreferences() {
        // WorkManager görevlerini başlat
        if (preferenceManager.isNotificationEnabled()) {
            new WorkManagerHelper(this).scheduleAllWork();
        }
    }

    /**
     * Tema değişikliğini uygular.
     * Bu metod hem başlangıçta hem de kullanıcı tema değiştirdiğinde çağrılır.
     *
     * @param mode Tema modu (-1: sistem varsayılanı, 0: açık tema, 1: koyu tema)
     */
    private void applyTheme(int mode) {
        switch (mode) {
            case -1: // Sistem varsayılanı
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 0:  // Açık tema
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:  // Koyu tema
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    /**
     * SettingsFragment'ten gelen tema değişikliği olayını yakalar
     */
    @Override
    public void onThemeChanged(int mode) {
        applyTheme(mode);
    }

    /**
     * Geri tuşu davranışını yönetir.
     * Navigation component ile entegre çalışır.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;  // Memory leak önlemek için binding'i temizle
    }
}