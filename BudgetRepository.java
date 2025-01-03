package com.tobrosgame.smartfinance.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.tobrosgame.smartfinance.database.AppDatabase;
import com.tobrosgame.smartfinance.database.dao.BudgetDao;
import com.tobrosgame.smartfinance.models.Budget;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {
    private final BudgetDao budgetDao;
    private final ExecutorService executorService;

    public BudgetRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Yeni bütçe oluşturma
    public void createBudget(Budget budget, OnBudgetCompleteListener listener) {
        executorService.execute(() -> {
            try {
                long id = budgetDao.insert(budget);
                budget.setId(id);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // Bütçe güncelleme
    public void updateBudget(Budget budget, OnBudgetCompleteListener listener) {
        executorService.execute(() -> {
            try {
                budgetDao.update(budget);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // Bütçe harcamasını güncelleme
    public void updateBudgetSpending(String category, double amount, OnBudgetCompleteListener listener) {
        executorService.execute(() -> {
            try {
                budgetDao.updateBudgetSpending(category, amount);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // Aktif bütçeleri getir
    public LiveData<List<Budget>> getActiveBudgets() {
        return budgetDao.getActiveBudgets();
    }

    // Kategori bazlı aktif bütçeyi getir
    public LiveData<Budget> getActiveBudgetForCategory(String category) {
        return budgetDao.getActiveBudgetForCategory(category);
    }

    // Aşılan bütçeleri getir
    public LiveData<List<Budget>> getOverspentBudgets() {
        return budgetDao.getOverspentBudgets();
    }

    // Bütçe özetini getir
    public LiveData<BudgetDao.BudgetSummary> getBudgetSummary() {
        return budgetDao.getBudgetSummary();
    }

    // Süresi dolan bütçeleri deaktive et
    public void deactivateExpiredBudgets(OnBudgetCompleteListener listener) {
        executorService.execute(() -> {
            try {
                budgetDao.deactivateExpiredBudgets(System.currentTimeMillis());
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    // Bütçe işlemleri için callback arayüzü
    public interface OnBudgetCompleteListener {
        void onSuccess();
        void onError(String error);
    }
}
