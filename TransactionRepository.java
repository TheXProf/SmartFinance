package com.tobrosgame.smartfinance.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.tobrosgame.smartfinance.database.AppDatabase;
import com.tobrosgame.smartfinance.database.dao.TransactionDao;
import com.tobrosgame.smartfinance.models.Transaction;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {
    private final TransactionDao transactionDao;
    private final ExecutorService executorService;

    // Repository'nin constructor'ı
    public TransactionRepository(Application application) {
        // Veritabanı bağlantısını al
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();

        // Arka plan işlemleri için ExecutorService oluştur
        executorService = Executors.newSingleThreadExecutor();
    }

    // Yeni işlem ekleme
    public void insertTransaction(Transaction transaction, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                long id = transactionDao.insert(transaction);
                transaction.setId(id);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // İşlem güncelleme
    public void updateTransaction(Transaction transaction, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                transactionDao.update(transaction);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // İşlem silme
    public void deleteTransaction(Transaction transaction, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                transactionDao.delete(transaction);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // Tüm işlemleri getir
    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    // Tarih aralığına göre işlemleri getir
    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate);
    }

    // Kategoriye göre işlemleri getir
    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return transactionDao.getTransactionsByCategory(category);
    }

    // İşlem tipine göre getir (Gelir/Gider)
    public LiveData<List<Transaction>> getTransactionsByType(String type) {
        return transactionDao.getTransactionsByType(type);
    }

    // Toplam gelir ve gider bilgisini getir
    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpense() {
        return transactionDao.getTotalExpense();
    }

    // İşlem sonuçları için callback arayüzü
    public interface OnTransactionCompleteListener {
        void onSuccess();
        void onError(String error);
    }
}
