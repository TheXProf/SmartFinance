package com.tobrosgame.smartfinance.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.ui.MainActivity;

/**
 * Bildirim işlemlerini yöneten yardımcı sınıf.
 * Bu sınıf, uygulama genelinde bildirimlerin tutarlı bir şekilde oluşturulmasını
 * ve yönetilmesini sağlar.
 */
public class NotificationHelper {
    private final Context context;
    private final NotificationManagerCompat notificationManager;

    // Bildirim kanalları için sabit değerler
    public static final String CHANNEL_DAILY_SUMMARY = "daily_summary";
    public static final String CHANNEL_BUDGET_ALERTS = "budget_alerts";

    // Bildirim ID'leri
    private static final int NOTIFICATION_ID_DAILY = 1001;
    private static final int NOTIFICATION_ID_BUDGET = 1002;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    /**
     * Bildirim kanallarını oluşturur.
     * Android 8.0 (API level 26) ve üzeri için gereklidir.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Günlük özet bildirimleri için kanal
            NotificationChannel dailyChannel = new NotificationChannel(
                    CHANNEL_DAILY_SUMMARY,
                    "Günlük Özetler",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dailyChannel.setDescription("Günlük finansal durumunuz hakkında özetler");

            // Bütçe uyarıları için kanal
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_BUDGET_ALERTS,
                    "Bütçe Uyarıları",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Bütçe limitleri aşıldığında uyarılar");

            // Kanalları sisteme kaydet
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(dailyChannel);
            manager.createNotificationChannel(alertChannel);
        }
    }

    /**
     * Günlük özet bildirimi gönderir.
     * Bu bildirim, kullanıcının günlük finansal durumunu özetler.
     */
    public void showDailySummary(double totalIncome, double totalExpense, double balance) {
        // Ana uygulamaya yönlendirecek intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Para birimini formatla
        String formattedBalance = String.format("₺%.2f", balance);

        // Bildirimi oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DAILY_SUMMARY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Günlük Finansal Özet")
                .setContentText("Güncel bakiyeniz: " + formattedBalance)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format(
                                "Bugünkü gelirleriniz: ₺%.2f\n" +
                                        "Bugünkü giderleriniz: ₺%.2f\n" +
                                        "Güncel bakiyeniz: %s",
                                totalIncome, totalExpense, formattedBalance
                        )))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Bildirimi göster
        notificationManager.notify(NOTIFICATION_ID_DAILY, builder.build());
    }

    /**
     * Bütçe uyarı bildirimi gönderir.
     * Bu bildirim, bir bütçe kategorisi belirlenen eşiğe ulaştığında gösterilir.
     */
    public void showBudgetAlert(String category, double spent, double limit, int percentage) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String message = String.format(
                "%s kategorisinde bütçenizin %%%.0f'sına ulaştınız. " +
                        "(₺%.2f / ₺%.2f)",
                category, percentage, spent, limit
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Bütçe Uyarısı")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID_BUDGET, builder.build());
    }

    /**
     * Bildirimleri iptal eder.
     * Bu metod, bildirimlerin kapatılması durumunda çağrılır.
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
