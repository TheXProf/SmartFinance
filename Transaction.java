package com.tobrosgame.smartfinance.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.tobrosgame.smartfinance.utils.DateConverter;
import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;           // İşlem başlığı (örn: "Market Alışverişi")
    private String description;     // Detaylı açıklama
    private double amount;          // İşlem tutarı
    private String category;        // İşlem kategorisi (örn: Market, Fatura, Maaş)
    private String type;            // İşlem tipi (INCOME/EXPENSE)

    @TypeConverters(DateConverter.class)
    private Date date;             // İşlem tarihi

    private String paymentMethod;   // Ödeme yöntemi (Nakit/Kart/Havale)
    private Long budgetId;         // İlişkili bütçe ID'si (varsa)
    private boolean isRecurring;    // Tekrarlayan işlem mi?
    private String recurringPeriod; // Tekrar periyodu (eğer tekrarlıysa)
    private String attachmentPath;  // Fiş/fatura fotoğrafı yolu (varsa)
    private String notes;          // Ek notlar

    // Varsayılan constructor - Room için gerekli
    public Transaction() {
        this.date = new Date();  // Varsayılan olarak şu anki tarih
        this.isRecurring = false;  // Varsayılan olarak tekrarlanmayan
    }

    // Temel işlem oluşturma constructor'ı
    public Transaction(String title, double amount, String category, String type) {
        this();  // Varsayılan değerleri ayarla
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
    }

    // Tam detaylı constructor
    public Transaction(String title, String description, double amount,
                       String category, String type, Date date, String paymentMethod) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.date = date;
        this.paymentMethod = paymentMethod;
    }

    // Getter ve Setter metodları
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurringPeriod() {
        return recurringPeriod;
    }

    public void setRecurringPeriod(String recurringPeriod) {
        this.recurringPeriod = recurringPeriod;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Yardımcı metodlar

    /**
     * İşlemin gelir olup olmadığını kontrol eder
     */
    public boolean isIncome() {
        return "INCOME".equals(type);
    }

    /**
     * İşlemin gider olup olmadığını kontrol eder
     */
    public boolean isExpense() {
        return "EXPENSE".equals(type);
    }

    /**
     * İşlem tutarını işaretiyle birlikte formatlar
     * Gelirler için +, giderler için - işareti kullanılır
     */
    public String getFormattedAmount() {
        String sign = isExpense() ? "-" : "+";
        return String.format("%s₺%.2f", sign, Math.abs(amount));
    }

    /**
     * İşlemin bir bütçeye bağlı olup olmadığını kontrol eder
     */
    public boolean hasBudget() {
        return budgetId != null;
    }

    /**
     * İşlemin ek dosyası (fiş/fatura) olup olmadığını kontrol eder
     */
    public boolean hasAttachment() {
        return attachmentPath != null && !attachmentPath.isEmpty();
    }

    /**
     * İşlemin açıklaması olup olmadığını kontrol eder
     * Yoksa başlığı döndürür
     */
    public String getDisplayDescription() {
        return description != null && !description.isEmpty() ? description : title;
    }
}