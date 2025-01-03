package com.tobrosgame.smartfinance.ui.budget;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.tobrosgame.smartfinance.R;
import com.tobrosgame.smartfinance.databinding.ItemBudgetBinding;
import com.tobrosgame.smartfinance.models.Budget;
import java.text.NumberFormat;
import java.util.Locale;

public class BudgetAdapter extends ListAdapter<Budget, BudgetAdapter.BudgetViewHolder> {

    private final BudgetClickListener listener;
    private final NumberFormat currencyFormatter;

    // Constructor
    public BudgetAdapter(BudgetClickListener listener) {
        super(new BudgetDiffCallback());
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBudgetBinding binding = ItemBudgetBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new BudgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = getItem(position);
        holder.bind(budget);
    }

    // ViewHolder sınıfı
    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetBinding binding;

        BudgetViewHolder(ItemBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Budget budget) {
            // Kategori adı ve ikonu
            binding.categoryText.setText(budget.getCategory());
            setCategoryIcon(budget.getCategory());

            // Bütçe tutarı
            binding.budgetAmountText.setText(currencyFormatter.format(budget.getPlannedAmount()));

            // İlerleme durumu
            updateProgress(budget);

            // Tıklama olayları
            binding.getRoot().setOnClickListener(v -> listener.onBudgetClick(budget));

            // Menü butonu
            binding.menuButton.setOnClickListener(v -> showPopupMenu(budget));
        }

        private void updateProgress(Budget budget) {
            // İlerleme yüzdesini hesapla
            double percentage = (budget.getSpentAmount() / budget.getPlannedAmount()) * 100;
            binding.budgetProgress.setProgress((int) percentage);

            // İlerleme çubuğu rengini duruma göre ayarla
            int color;
            if (percentage >= 90) {
                color = itemView.getContext().getColor(R.color.expense_red);
            } else if (percentage >= 75) {
                color = itemView.getContext().getColor(R.color.warning_yellow);
            } else {
                color = itemView.getContext().getColor(R.color.income_green);
            }
            binding.budgetProgress.setIndicatorColor(color);

            // Kalan tutarı göster
            double remaining = budget.getPlannedAmount() - budget.getSpentAmount();
            String remainingText = String.format(
                    "Kalan: %s (%d%%)",
                    currencyFormatter.format(remaining),
                    (int) (100 - percentage)
            );
            binding.remainingText.setText(remainingText);
            binding.remainingText.setTextColor(color);
        }

        private void setCategoryIcon(String category) {
            int iconRes;
            switch (category.toLowerCase()) {
                case "market":
                    iconRes = R.drawable.ic_shopping;
                    break;
                case "fatura":
                    iconRes = R.drawable.ic_bill;
                    break;
                case "ulaşım":
                    iconRes = R.drawable.ic_transport;
                    break;
                case "yemek":
                    iconRes = R.drawable.ic_food;
                    break;
                default:
                    iconRes = R.drawable.ic_misc;
                    break;
            }
            binding.categoryIcon.setImageResource(iconRes);
        }

        private void showPopupMenu(Budget budget) {
            PopupMenu popup = new PopupMenu(itemView.getContext(), binding.menuButton);
            popup.inflate(R.menu.budget_item_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    listener.onEditClick(budget);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }

    // DiffUtil callback
    static class BudgetDiffCallback extends DiffUtil.ItemCallback<Budget> {
        @Override
        public boolean areItemsTheSame(@NonNull Budget oldItem, @NonNull Budget newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Budget oldItem, @NonNull Budget newItem) {
            return oldItem.getPlannedAmount() == newItem.getPlannedAmount() &&
                    oldItem.getSpentAmount() == newItem.getSpentAmount() &&
                    oldItem.getCategory().equals(newItem.getCategory());
        }
    }

    // Click listener interface
    public interface BudgetClickListener {
        void onBudgetClick(Budget budget);
        void onEditClick(Budget budget);
    }
}
