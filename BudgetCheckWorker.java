package com.tobrosgame.smartfinance.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.tobrosgame.smartfinance.database.AppDatabase;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.utils.NotificationHelper;
import com.tobrosgame.smartfinance.utils.PreferenceManager;
import java.util.List;

/**
 * Bütçe durumunu kontrol eden ve gerektiğinde uyarı bildirimi gönderen worker sınıfı.
 * Bu sınıf düzenli olarak:
 * 1. Tüm aktif bütçeleri kontrol eder
 * 2. Belirlenen eşik değerini aşan bütçeler için uyarı gönderir
 * 3. Süresi dolan bütçeleri deaktive eder
 */
public class BudgetCheckWorker extends Worker {
    private final Context context;
    private final NotificationHelper notificationHelper;
    private final PreferenceManager preferenceManager;
    private final AppDatabase database;

    public BudgetCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
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

            // Bütçe uyarı eşiğini al
            int threshold = preferenceManager.getBudgetAlertThreshold();

            // Aktif bütçeleri kontrol et
            List<Budget> activeBudgets = database.budgetDao().getActiveBudgetsSync();

            for (Budget budget : activeBudgets) {
                checkBudget(budget, threshold);
            }

            // Süresi dolan bütçeleri deaktive et
            deactivateExpiredBudgets(activeBudgets);

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    /**
     * Tek bir bütçeyi kontrol eder ve gerekirse uyarı gönderir
     */
    private void checkBudget(Budget budget, int threshold) {
        double spentPercentage = budget.getSpentPercentage();

        // Eşik değeri aşıldıysa ve bütçe aktifse uyarı gönder
        if (budget.isActive() && spentPercentage >= threshold) {
            notificationHelper.showBudgetAlert(
                    budget.getCategory(),
                    budget.getSpentAmount(),
                    budget.getPlannedAmount(),
                    (int) spentPercentage
            );
        }
    }

    /**
     * Süresi dolan bütçeleri deaktive eder
     */
    private void deactivateExpiredBudgets(List<Budget> budgets) {
        for (Budget budget : budgets) {
            if (budget.isExpired() && budget.isActive()) {
                budget.setActive(false);
                database.budgetDao().updateSync(budget);
            }
        }
    }
}