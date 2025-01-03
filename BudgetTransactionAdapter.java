package com.tobrosgame.smartfinance.ui.budget;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.tobrosgame.smartfinance.databinding.ItemBudgetTransactionBinding;
import com.tobrosgame.smartfinance.models.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BudgetTransactionAdapter extends ListAdapter<Transaction, BudgetTransactionAdapter.TransactionViewHolder> {

    // Tarih ve para birimi formatları için gerekli nesneler
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy", new Locale("tr"));
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    // Constructor
    public BudgetTransactionAdapter() {
        super(new TransactionDiffCallback());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View binding kullanarak layout'u bağla
        ItemBudgetTransactionBinding binding = ItemBudgetTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        // Mevcut pozisyondaki işlemi al ve görüntüle
        Transaction transaction = getItem(position);
        holder.bind(transaction);
    }

    // ViewHolder sınıfı - her bir işlem öğesinin görünümünü yönetir
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetTransactionBinding binding;

        TransactionViewHolder(ItemBudgetTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Transaction transaction) {
            // Tarih bilgisini formatla ve göster
            binding.dateText.setText(dateFormatter.format(transaction.getDate()));

            // İşlem başlığını göster
            binding.titleText.setText(transaction.getTitle());

            // Tutarı formatla ve göster
            String amount = transaction.isExpense() ?
                    "- " + currencyFormatter.format(Math.abs(transaction.getAmount())) :
                    "+ " + currencyFormatter.format(Math.abs(transaction.getAmount()));

            binding.amountText.setText(amount);

            // İşlem tipine göre renk ayarla (gelir: yeşil, gider: kırmızı)
            int textColor = transaction.isExpense() ?
                    itemView.getContext().getColor(com.tobrosgame.smartfinance.R.color.expense_red) :
                    itemView.getContext().getColor(com.tobrosgame.smartfinance.R.color.income_green);

            binding.amountText.setTextColor(textColor);
        }
    }

    // DiffUtil callback sınıfı - liste güncellemelerini verimli hale getirir
    static class TransactionDiffCallback extends DiffUtil.ItemCallback<Transaction> {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            // İşlemleri ID'lerine göre karşılaştır
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            // İşlemlerin içeriklerini karşılaştır
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDate().equals(newItem.getDate());
        }
    }
}