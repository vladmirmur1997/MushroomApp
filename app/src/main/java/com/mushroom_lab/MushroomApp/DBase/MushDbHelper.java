package com.mushroom_lab.MushroomApp.DBase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

public class MushDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mushs.db"; // название бд
    private static final int SCHEMA = 1; // версия базы данных
    public static final String TABLE = "mushs1"; // название таблицы в бд
    // названия столбцов
    public static final String FOREST_NUM = "forest"; public String ID = "_id";
    public static final String WALK_NUM = "walk";
    public static final String TYPE = "type";
    public static final String C_X = "x"; public static final String C_Y = "y";

    public MushDbHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS mushs1 (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FOREST_NUM
                + " INTEGER, " + WALK_NUM + " INTEGER, " + TYPE
                + " TEXT, " + C_X + " REAL, " + C_Y + " REAL );");
        // добавление начальных данных
        db.execSQL("INSERT INTO "+ TABLE +" (" + FOREST_NUM + ", " + WALK_NUM + ", " + TYPE
                + ", " + C_X  + ", " + C_Y + ") VALUES (0, 0 ,'Белый', 0, 0);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        //onCreate(db);
    }
}
