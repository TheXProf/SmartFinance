package com.tobrosgame.smartfinance.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.tobrosgame.smartfinance.database.AppDatabase;
import com.tobrosgame.smartfinance.models.Transaction;
import com.tobrosgame.smartfinance.utils.NotificationHelper;
import com.tobrosgame.smartfinance.utils.PreferenceManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Günlük finansal özet bildirimi gönderen worker sınıfı.
 * Bu sınıf her gün çalışarak:
 * 1. O günün işlemlerini analiz eder
 * 2. Toplam gelir ve giderleri hesaplar
 * 3. Kullanıcıya günlük özet bildirimi gönderir
 */
public class DailySummaryWorker extends Worker {
    private final Context context;
    private final NotificationHelper notificationHelper;
    private final PreferenceManager preferenceManager;
    private final AppDatabase database;

    public DailySummaryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
        this.preferenceManager = PreferenceManager.getInstance(context);
        this.database = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Bildirimler kapalıysa çalışma
            if (!preferenceManager.isNotificationEnabled()) {
                return Result.success();
            }

            // Bugünün başlangıç ve bitiş zamanlarını hesapla
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startOfDay = calendar.getTime();

            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endOfDay = calendar.getTime();

            // Bugünün işlemlerini getir
            List<Transaction> todaysTransactions = database.transactionDao()
                    .getTransactionsBetweenDatesSync(startOfDay, endOfDay);

            // Günlük toplam gelir ve giderleri hesapla
            double totalIncome = 0;
            double totalExpense = 0;

            for (Transaction transaction : todaysTransactions) {
                if (transaction.isIncome()) {
                    totalIncome += transaction.getAmount();
                } else {
                    totalExpense += transaction.getAmount();
                }
            }

            // Mevcut bakiyeyi hesapla
            double totalBalance = database.transactionDao().getTotalBalanceSync();

            // Sadece işlem varsa bildirim gönder
            if (todaysTransactions.size() > 0) {
                notificationHelper.showDailySummary(totalIncome, totalExpense, totalBalance);
            }

            return Result.success();
        } catch (Exception e) {
            // Hata durumunda tekrar denenmesi için failure döndür
            return Result.failure();
        }
    }
}