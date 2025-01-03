package com.tobrosgame.smartfinance.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tobrosgame.smartfinance.models.Transaction;
import com.tobrosgame.smartfinance.repository.TransactionRepository;
import java.util.Date;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository repository;

    // İşlem durumunu takip etmek için LiveData
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> success = new MutableLiveData<>();

    public TransactionViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    // Yeni işlem ekleme
    public void addTransaction(Transaction transaction) {
        isLoading.setValue(true);
        repository.insertTransaction(transaction, new TransactionRepository.OnTransactionCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                success.postValue("İşlem başarıyla eklendi");
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue("İşlem eklenirken hata oluştu: " + errorMessage);
            }
        });
    }

    // İşlem güncelleme
    public void updateTransaction(Transaction transaction) {
        isLoading.setValue(true);
        repository.updateTransaction(transaction, new TransactionRepository.OnTransactionCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                success.postValue("İşlem başarıyla güncellendi");
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue("İşlem güncellenirken hata oluştu: " + errorMessage);
            }
        });
    }

    // İşlem silme
    public void deleteTransaction(Transaction transaction) {
        isLoading.setValue(true);
        repository.deleteTransaction(transaction, new TransactionRepository.OnTransactionCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                success.postValue("İşlem başarıyla silindi");
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue("İşlem silinirken hata oluştu: " + errorMessage);
            }
        });
    }

    // Tüm işlemleri getir
    public LiveData<List<Transaction>> getAllTransactions() {
        return repository.getAllTransactions();
    }

    // Tarih aralığına göre işlemleri getir
    public LiveData<List<Transaction>> getTransactionsForPeriod(Date startDate, Date endDate) {
        return repository.getTransactionsBetweenDates(startDate, endDate);
    }

    // Mali durum özeti için yardımcı metod
    public LiveData<FinancialSummary> getFinancialSummary() {
        // Bu metod gelir, gider ve bakiye bilgilerini birleştirir
        return new LiveData<FinancialSummary>() {
            private final LiveData<Double> totalIncome = repository.getTotalIncome();
            private final LiveData<Double> totalExpense = repository.getTotalExpense();

            @Override
            protected void onActive() {
                super.onActive();
                // Gelir ve gider değişikliklerini dinle
            }
        };
    }

    // Durum bildirimleri için getter metodları
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSuccess() {
        return success;
    }

    // Mali durum özeti için yardımcı sınıf
    public static class FinancialSummary {
        private final double totalIncome;
        private final double totalExpense;
        private final double balance;

        public FinancialSummary(double totalIncome, double totalExpense) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.balance = totalIncome - totalExpense;
        }

        // Getter metodları
        public double getTotalIncome() { return totalIncome; }
        public double getTotalExpense() { return totalExpense; }
        public double getBalance() { return balance; }
    }
}
