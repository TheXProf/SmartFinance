package com.tobrosgame.smartfinance.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Uygulama genelinde kullanıcı tercihlerinin yönetimini sağlayan yardımcı sınıf.
 * Bu sınıf, SharedPreferences işlemlerini merkezi bir noktadan yönetir ve tip güvenliği sağlar.
 */
public class PreferenceManager {
    // SharedPreferences için sabit değerler
    private static final String PREF_NAME = "SmartFinancePrefs";

    // Tercih anahtarları için sabit değerler
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_NOTIFICATION_TIME = "notification_time";
    private static final String KEY_BUDGET_ALERT_THRESHOLD = "budget_alert_threshold";
    private static final String KEY_FIRST_DAY_OF_MONTH = "first_day_of_month";

    private final SharedPreferences preferences;

    // Singleton instance
    private static PreferenceManager instance;

    /**
     * Singleton pattern ile tek bir instance oluşturulmasını sağlar.
     * Bu sayede uygulama genelinde tutarlı bir tercih yönetimi sağlanır.
     */
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    private PreferenceManager(Context context) {
        // Context'in Application context olduğundan emin oluyoruz
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Para birimi ayarlarını yönetir.
     * Varsayılan değer olarak TRY (Türk Lirası) kullanılır.
     */
    public String getCurrency() {
        return preferences.getString(KEY_CURRENCY, "TRY");
    }

    public void setCurrency(String currency) {
        preferences.edit().putString(KEY_CURRENCY, currency).apply();
    }

    /**
     * Karanlık tema tercihini yönetir.
     * Varsayılan olarak sistem teması kullanılır (-1).
     * 0: Açık tema
     * 1: Koyu tema
     * -1: Sistem teması
     */
    public int getDarkMode() {
        return preferences.getInt(KEY_DARK_MODE, -1);
    }

    public void setDarkMode(int mode) {
        preferences.edit().putInt(KEY_DARK_MODE, mode).apply();
    }

    /**
     * Bildirim ayarlarını yönetir.
     * Varsayılan olarak bildirimler aktiftir.
     */
    public boolean isNotificationEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    /**
     * Günlük bildirim saatini yönetir.
     * Saat, gün içindeki dakika cinsinden saklanır (0-1439).
     * Varsayılan değer 20:00 (1200 dakika).
     */
    public int getNotificationTime() {
        return preferences.getInt(KEY_NOTIFICATION_TIME, 1200);
    }

    public void setNotificationTime(int minuteOfDay) {
        preferences.edit().putInt(KEY_NOTIFICATION_TIME, minuteOfDay).apply();
    }

    /**
     * Bütçe uyarı eşiğini yönetir.
     * Değer, yüzde cinsinden saklanır (0-100).
     * Varsayılan değer %80'dir.
     */
    public int getBudgetAlertThreshold() {
        return preferences.getInt(KEY_BUDGET_ALERT_THRESHOLD, 80);
    }

    public void setBudgetAlertThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 100) {
            preferences.edit().putInt(KEY_BUDGET_ALERT_THRESHOLD, threshold).apply();
        }
    }

    /**
     * Ayın ilk gününü yönetir (maaş günü vb. için).
     * 1-31 arası bir değer alır.
     * Varsayılan değer 1'dir.
     */
    public int getFirstDayOfMonth() {
        return preferences.getInt(KEY_FIRST_DAY_OF_MONTH, 1);
    }

    public void setFirstDayOfMonth(int day) {
        if (day >= 1 && day <= 31) {
            preferences.edit().putInt(KEY_FIRST_DAY_OF_MONTH, day).apply();
        }
    }

    /**
     * Tüm tercihleri varsayılan değerlerine sıfırlar.
     * Bu metod, ayarları sıfırla seçeneği için kullanılır.
     */
    public void resetToDefaults() {
        preferences.edit().clear().apply();
    }
}