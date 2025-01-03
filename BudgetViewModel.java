package com.tobrosgame.smartfinance.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.tobrosgame.smartfinance.database.dao.BudgetDao.BudgetSummary;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.repository.BudgetRepository;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {
    private final BudgetRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> success = new MutableLiveData<>();

    public BudgetViewModel(Application application) {
        super(application);
        repository = new BudgetRepository(application);
    }

    // Yeni bütçe oluşturma
    public void createBudget(Budget budget) {
        isLoading.setValue(true);
        repository.createBudget(budget, new BudgetRepository.OnBudgetCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                success.postValue("Bütçe başarıyla oluşturuldu");
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue("Bütçe oluşturulurken hata: " + errorMessage);
            }
        });
    }

    // Bütçe güncelleme
    public void updateBudget(Budget budget) {
        isLoading.setValue(true);
        repository.updateBudget(budget, new BudgetRepository.OnBudgetCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                success.postValue("Bütçe başarıyla güncellendi");
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue("Bütçe güncellenirken hata: " + errorMessage);
            }
        });
    }

    // Harcama ekleyerek bütçeyi güncelleme
    public void addExpenseToBudget(String category, double amount) {
        isLoading.setValue(true);
        repository.updateBudgetSpending(category, amount, new BudgetRepository.OnBudgetCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                checkBudgetWarnings(category);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue("Harcama eklenirken hata: " + errorMessage);
            }
        });
    }

    // Bütçe uyarılarını kontrol et
    private void checkBudgetWarnings(String category) {
        LiveData<Budget> budgetLiveData = repository.getActiveBudgetForCategory(category);
        Budget budget = budgetLiveData.getValue();

        if (budget != null) {
            double spentPercentage = (budget.getSpentAmount() / budget.getPlannedAmount()) * 100;

            if (spentPercentage >= 90) {
                error.postValue("Uyarı: " + category + " bütçeniz %90'ın üzerinde kullanıldı!");
            } else if (spentPercentage >= 75) {
                warning.postValue("Dikkat: " + category + " bütçenizin %75'ini kullandınız.");
            }
        }
    }

    // Aktif bütçeleri getir
    public LiveData<List<Budget>> getActiveBudgets() {
        return repository.getActiveBudgets();
    }

    // Aşılan bütçeleri getir
    public LiveData<List<Budget>> getOverspentBudgets() {
        return repository.getOverspentBudgets();
    }

    // Bütçe özetini getir
    public LiveData<BudgetSummary> getBudgetSummary() {
        return repository.getBudgetSummary();
    }

    // Süresi geçmiş bütçeleri kontrol et ve deaktive et
    public void checkAndDeactivateExpiredBudgets() {
        repository.deactivateExpiredBudgets(new BudgetRepository.OnBudgetCompleteListener() {
            @Override
            public void onSuccess() {
                // İşlem sessizce tamamlandı
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Bütçe kontrolü sırasında hata: " + errorMessage);
            }
        });
    }

    // Durum getter metodları
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getSuccess() { return success; }
}
