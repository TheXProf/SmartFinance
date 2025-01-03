package com.tobrosgame.smartfinance.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.tobrosgame.smartfinance.models.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    // Aktif bütçeleri getir
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY startDate DESC")
    LiveData<List<Budget>> getActiveBudgets();

    // Kategori bazlı aktif bütçeyi getir
    @Query("SELECT * FROM budgets WHERE category = :category AND isActive = 1 LIMIT 1")
    LiveData<Budget> getActiveBudgetForCategory(String category);

    // Bütçe aşımı olan kategorileri getir
    @Query("SELECT * FROM budgets WHERE spentAmount > plannedAmount AND isActive = 1")
    LiveData<List<Budget>> getOverspentBudgets();

    // Bütçe kullanım yüzdesini güncelle
    @Query("UPDATE budgets SET spentAmount = spentAmount + :amount " +
            "WHERE category = :category AND isActive = 1")
    void updateBudgetSpending(String category, double amount);

    // Süresi dolmuş bütçeleri deaktive et
    @Query("UPDATE budgets SET isActive = 0 WHERE endDate < :currentDate AND isActive = 1")
    void deactivateExpiredBudgets(long currentDate);

    // Bütçe durumu özeti
    @Query("SELECT " +
            "SUM(plannedAmount) as totalPlanned, " +
            "SUM(spentAmount) as totalSpent " +
            "FROM budgets WHERE isActive = 1")
    LiveData<BudgetSummary> getBudgetSummary();

    // Bütçe özeti için yardımcı sınıf
    static class BudgetSummary {
        public double totalPlanned;
        public double totalSpent;

        public double getRemainingPercentage() {
            return ((totalPlanned - totalSpent) / totalPlanned) * 100;
        }
    }
}
