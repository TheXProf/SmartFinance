package com.tobrosgame.smartfinance.utils;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.tobrosgame.smartfinance.workers.BudgetCheckWorker;
import com.tobrosgame.smartfinance.workers.DailySummaryWorker;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * WorkManager görevlerini yöneten yardımcı sınıf.
 * Bu sınıf, düzenli bildirimleri ve kontrolleri programlar.
 */
public class WorkManagerHelper {
    private static final String DAILY_SUMMARY_WORK = "daily_summary_work";
    private static final String BUDGET_CHECK_WORK = "budget_check_work";

    private final Context context;
    private final PreferenceManager preferenceManager;

    public WorkManagerHelper(Context context) {
        this.context = context.getApplicationContext();
        this.preferenceManager = PreferenceManager.getInstance(context);
    }

    /**
     * Tüm düzenli görevleri başlatır veya günceller
     */
    public void scheduleAllWork() {
        scheduleDailySummary();
        scheduleBudgetChecks();
    }

    /**
     * Günlük özet bildirimlerini programlar
     */
    private void scheduleDailySummary() {
        // Tercih edilen bildirim saatini al
        int preferredMinute = preferenceManager.getNotificationTime();

        // Şu anki zamanla karşılaştırarak ilk çalışma zamanını hesapla
        Calendar now = Calendar.getInstance();
        Calendar firstRun = Calendar.getInstance();

        firstRun.set(Calendar.HOUR_OF_DAY, preferredMinute / 60);
        firstRun.set(Calendar.MINUTE, preferredMinute % 60);
        firstRun.set(Calendar.SECOND, 0);

        // Eğer belirlenen saat geçtiyse, yarına ayarla
        if (now.after(firstRun)) {
            firstRun.add(Calendar.DAY_OF_MONTH, 1);
        }

        // İlk çalışmaya kadar olan süreyi hesapla
        long initialDelay = firstRun.getTimeInMillis() - now.getTimeInMillis();

        // Çalışma kısıtlamalarını belirle
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // Günlük çalışma isteği oluştur
        PeriodicWorkRequest summaryWork = new PeriodicWorkRequest.Builder(
                DailySummaryWorker.class,
                24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        // Görevi programla
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        DAILY_SUMMARY_WORK,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        summaryWork
                );
    }

    /**
     * Bütçe kontrol görevini programlar
     * Bu görev daha sık çalışır (4 saatte bir)
     */
    private void scheduleBudgetChecks() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest budgetWork = new PeriodicWorkRequest.Builder(
                BudgetCheckWorker.class,
                4, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        BUDGET_CHECK_WORK,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        budgetWork
                );
    }

    /**
     * Tüm düzenli görevleri iptal eder
     */
    public void cancelAllWork() {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_SUMMARY_WORK);
        WorkManager.getInstance(context).cancelUniqueWork(BUDGET_CHECK_WORK);
    }
}