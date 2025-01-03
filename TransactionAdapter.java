package com.tobrosgame.smartfinance.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.ItemTransactionBinding;
import com.tobrosgame.smartfinance.models.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    // Tarih formatı için SimpleDateFormat nesnesi
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("tr"));

    // Para birimi formatı için NumberFormat nesnesi
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    // Constructor
    public TransactionAdapter() {
        super(new TransactionDiffCallback());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View binding kullanarak layout'u bağlıyoruz
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction);
    }

    // ViewHolder sınıfı - her bir işlem öğesinin görünümünü yönetir
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        TransactionViewHolder(@NonNull ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Transaction transaction) {
            // İşlem başlığı
            binding.titleText.setText(transaction.getTitle());

            // Kategori
            binding.categoryText.setText(transaction.getCategory());

            // Tarih
            binding.dateText.setText(dateFormat.format(transaction.getDate()));

            // Tutar
            String amount = currencyFormat.format(Math.abs(transaction.getAmount()));
            binding.amountText.setText(amount);

            // İşlem tipine göre renk ve ikon ayarla
            if (transaction.isIncome()) {
                binding.amountText.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.income_green)
                );
                binding.categoryIcon.setImageResource(R.drawable.ic_income);
            } else {
                binding.amountText.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.expense_red)
                );
                binding.categoryIcon.setImageResource(R.drawable.ic_expense);
            }

            // Kategoriye göre ikon seç
            setCategoryIcon(transaction.getCategory());

            // Tıklama olayını ayarla
            binding.getRoot().setOnClickListener(v -> {
                // İşlem detay sayfasına yönlendirme yapılacak
                // TODO: Implement click handling
            });
        }

        // Kategori ikonlarını ayarlayan yardımcı metod
        private void setCategoryIcon(String category) {
            int iconRes;
            switch (category.toLowerCase()) {
                case "market":
                    iconRes = R.drawable.ic_shopping;
                    break;
                case "ulaşım":
                    iconRes = R.drawable.ic_transport;
                    break;
                case "fatura":
                    iconRes = R.drawable.ic_bill;
                    break;
                case "yemek":
                    iconRes = R.drawable.ic_food;
                    break;
                case "maaş":
                    iconRes = R.drawable.ic_salary;
                    break;
                default:
                    iconRes = R.drawable.ic_misc;
                    break;
            }
            binding.categoryIcon.setImageResource(iconRes);
        }
    }

    // DiffUtil callback sınıfı - liste güncellemelerini verimli şekilde yönetir
    static class TransactionDiffCallback extends DiffUtil.ItemCallback<Transaction> {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getCategory().equals(newItem.getCategory()) &&
                    oldItem.getDate().equals(newItem.getDate());
        }
    }
}