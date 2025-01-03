package com.tobrosgame.smartfinance.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.tobrosgame.smartfinance.models.Transaction;
import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {
    // Temel CRUD (Create, Read, Update, Delete) işlemleri
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    // Tüm işlemleri tarihe göre sıralı getir
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    // Belirli bir tarih aralığındaki işlemleri getir
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate);

    // Kategoriye göre işlemleri getir
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    // İşlem tipine göre getir (Gelir/Gider)
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByType(String type);

    // Toplam gelir
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    LiveData<Double> getTotalIncome();

    // Toplam gider
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    LiveData<Double> getTotalExpense();

    // Kategori bazında toplam harcama
    @Query("SELECT category, SUM(amount) as total FROM transactions " +
            "WHERE type = 'EXPENSE' GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getCategoryTotals();

    // Kategori toplamları için yardımcı sınıf
    static class CategoryTotal {
        public String category;
        public double total;
    }
}