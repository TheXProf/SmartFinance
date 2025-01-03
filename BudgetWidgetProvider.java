package com.tobrosgame.smartfinance.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.database.AppDatabase;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.ui.MainActivity;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetWidgetProvider extends AppWidgetProvider {

    private static final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Her widget için güncelleme yap
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Widget görünümünü oluştur
        @SuppressLint("RemoteViewLayout") RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_budget_summary);

        // Widget'a tıklandığında uygulamayı aç
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent);

        // Veritabanından bütçe verilerini al
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            List<Budget> activeBudgets = db.budgetDao().getActiveBudgetsSync();

            // Toplam ve kalan bütçeyi hesapla
            double totalBudget = 0;
            double totalSpent = 0;

            for (Budget budget : activeBudgets) {
                totalBudget += budget.getPlannedAmount();
                totalSpent += budget.getSpentAmount();
            }

            double remainingAmount = totalBudget - totalSpent;
            int progress = totalBudget > 0 ?
                    (int) ((totalSpent / totalBudget) * 100) : 0;

            // UI güncellemelerini ana thread'de yap
            appWidgetManager.updateAppWidget(appWidgetId, views);

            // Widget görünümünü güncelle
            views.setTextViewText(R.id.totalBudgetAmount,
                    currencyFormatter.format(totalBudget));
            views.setTextViewText(R.id.remainingAmount,
                    currencyFormatter.format(remainingAmount));
            views.setProgressBar(R.id.budgetProgress, 100, progress, false);

            // İlerleme çubuğu rengini duruma göre ayarla
            int colorResId;
            if (progress >= 90) {
                colorResId = R.color.expense_red;
            } else if (progress >= 75) {
                colorResId = R.color.warning_yellow;
            } else {
                colorResId = R.color.income_green;
            }
            views.setInt(R.id.budgetProgress, "setProgressTintList",
                    context.getColor(colorResId));

            // Son güncelleme zamanını göster
            views.setTextViewText(R.id.lastUpdateText,
                    "Son güncelleme: " + android.text.format.DateFormat.format(
                            "HH:mm", System.currentTimeMillis()));

            // Widget'ı güncelle
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    @Override
    public void onEnabled(Context context) {
        // Widget ilk eklendiğinde çağrılır
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Son widget kaldırıldığında çağrılır
        super.onDisabled(context);
    }
}