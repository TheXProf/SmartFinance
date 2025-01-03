package com.tobrosgame.smartfinance.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.tobrosgame.smartfinance.utils.DateConverter;
import java.util.Date;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String category;        // Bütçe kategorisi
    private double plannedAmount;   // Planlanan harcama limiti
    private double spentAmount;     // Şu ana kadar harcanan miktar

    @TypeConverters(DateConverter.class)
    private Date startDate;         // Bütçe başlangıç tarihi

    @TypeConverters(DateConverter.class)
    private Date endDate;           // Bütçe bitiş tarihi

    private String period;          // MONTHLY, WEEKLY, YEARLY
    private boolean isActive;       // Bütçe aktif mi?
    private String notes;           // Bütçe notları

    // Varsayılan constructor - Room için gerekli
    public Budget() {
        this.isActive = true;  // Yeni bütçeler varsayılan olarak aktif
        this.spentAmount = 0;  // Başlangıçta harcama sıfır
    }

    // Ana constructor
    public Budget(String category, double plannedAmount, Date startDate, Date endDate, String period) {
        this();  // Varsayılan değerleri ayarla
        this.category = category;
        this.plannedAmount = plannedAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }

    // Getter ve Setter metodları
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(double plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Yardımcı metodlar

    /**
     * Bütçenin yüzde kaçının harcandığını hesaplar
     * @return Harcanan yüzde (0-100 arası)
     */
    public double getSpentPercentage() {
        if (plannedAmount <= 0) return 0;
        return (spentAmount / plannedAmount) * 100;
    }

    /**
     * Kalan bütçe miktarını hesaplar
     * @return Kalan miktar
     */
    public double getRemainingAmount() {
        return plannedAmount - spentAmount;
    }

    /**
     * Bütçenin aşılıp aşılmadığını kontrol eder
     * @return Bütçe aşıldıysa true
     */
    public boolean isOverspent() {
        return spentAmount > plannedAmount;
    }

    /**
     * Bütçenin belirli bir eşik değerini aşıp aşmadığını kontrol eder
     * @param threshold Eşik değeri (yüzde olarak, 0-100 arası)
     * @return Eşik aşıldıysa true
     */
    public boolean isThresholdExceeded(double threshold) {
        return getSpentPercentage() >= threshold;
    }

    /**
     * Bütçenin geçerlilik süresinin dolup dolmadığını kontrol eder
     * @return Süre dolduysa true
     */
    public boolean isExpired() {
        return endDate != null && new Date().after(endDate);
    }
}