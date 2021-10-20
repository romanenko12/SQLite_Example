package com.example.sqlite_example.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.sqlite_example.data.ClubOlympusContract.MemberEntry

class OlympusDbOpenHelper(context: Context?) : SQLiteOpenHelper(
    context, ClubOlympusContract.DATABASE_NAME,
    null, ClubOlympusContract.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_MEBERS_TABLE = ("CREATE TABLE " + MemberEntry.TABLE_NAME + "("
                + MemberEntry._ID + " INTEGER PRIMARY KEY,"
                + MemberEntry.COLUMN_FIRST_NAME + " TEXT,"
                + MemberEntry.COLUMN_LAST_NAME + " TEXT,"
                + MemberEntry.COLUMN_GENDER + " INTEGER NOT NULL,"
                + MemberEntry.COLUMN_SPORT + " TEXT" + ")")
        db.execSQL(CREATE_MEBERS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + ClubOlympusContract.DATABASE_NAME)
        onCreate(db)
    }
}