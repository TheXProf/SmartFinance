package com.tobrosgame.smartfinance.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tobrosgame.smartfinance.models.Transaction;
import com.tobrosgame.smartfinance.models.Budget;
import com.tobrosgame.smartfinance.database.dao.TransactionDao;
import com.tobrosgame.smartfinance.database.dao.BudgetDao;
import com.tobrosgame.smartfinance.utils.DateConverter;

@Database(
        entities = {Transaction.class, Budget.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    // DAO'ları tanımlıyoruz
    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();

    // Singleton pattern uyguluyoruz
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Veritabanını oluşturuyoruz
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finance_database"
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Veritabanı ilk oluşturulduğunda çalışacak kod
                                    // Örneğin varsayılan kategorileri ekleyebiliriz
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
