package com.example.sqlite_example.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.example.sqlite_example.data.ClubOlympusContract.MemberEntry
import java.lang.IllegalArgumentException

class OlympusContentProvider : ContentProvider() {
    private var dbOpenHelper: OlympusDbOpenHelper? = null

    companion object {
        private const val MEMBERS = 111
        private const val MEMBER_ID = 222

        // Creates a UriMatcher object.
        private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(
                ClubOlympusContract.AUTHORITY,
                ClubOlympusContract.PATH_MEMBERS, MEMBERS
            )
            uriMatcher.addURI(
                ClubOlympusContract.AUTHORITY, ClubOlympusContract.PATH_MEMBERS
                        + "/#", MEMBER_ID
            )
        }
    }

    override fun onCreate(): Boolean {
        dbOpenHelper = OlympusDbOpenHelper(context)
        return true
    }

    // content://com.example.sqlite_example/members/34
    // projection = { "lastName", "gender" }
    override fun query(
        uri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db: SQLiteDatabase = dbOpenHelper!!.readableDatabase
        val cursor: Cursor
        when (uriMatcher.match(uri)) {
            MEMBERS -> cursor = db.query(
                MemberEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder
            )
            MEMBER_ID -> {
                val _selection = MemberEntry._ID + "=?"
                val _selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                cursor = db.query(
                    MemberEntry.TABLE_NAME, projection, _selection,
                    _selectionArgs, null, null, sortOrder
                )
            }
            else -> throw IllegalArgumentException(
                "Can't query incorrect URI "
                        + uri
            )
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values!!.getAsString(MemberEntry.COLUMN_FIRST_NAME)
            ?: throw IllegalArgumentException("You have to input first name")
        values.getAsString(MemberEntry.COLUMN_LAST_NAME)
            ?: throw IllegalArgumentException("You have to input last name")
        val gender: Int = values.getAsInteger(MemberEntry.COLUMN_GENDER)
        require(
            gender == MemberEntry.GENDER_UNKNOWN || gender ==
                    MemberEntry.GENDER_MALE || gender == MemberEntry.GENDER_FEMALE
        ) { "You have to input correct gender" }
        values.getAsString(MemberEntry.COLUMN_SPORT)
            ?: throw IllegalArgumentException("You have to input sport")
        val db: SQLiteDatabase = dbOpenHelper!!.writableDatabase
        return when (uriMatcher.match(uri)) {
            MEMBERS -> {
                val id: Long = db.insert(
                    MemberEntry.TABLE_NAME,
                    null, values
                )
                if (id == -1L) {
                    Log.e(
                        "insertMethod", "Insertion of data in the table failed for "
                                + uri
                    )
                    return null
                }
                context!!.contentResolver.notifyChange(
                    uri,
                    null
                )
                ContentUris.withAppendedId(uri, id)
            }
            else -> throw IllegalArgumentException(
                "Insertion of data in " +
                        "the table failed for " + uri
            )
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db: SQLiteDatabase = dbOpenHelper!!.writableDatabase
        val match: Int = uriMatcher.match(uri)
        val rowsDeleted: Int
        when (match) {
            MEMBERS -> rowsDeleted = db.delete(
                MemberEntry.TABLE_NAME, selection,
                selectionArgs
            )
            MEMBER_ID -> {
                val _selection = MemberEntry._ID + "=?"
                val _selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsDeleted = db.delete(
                    MemberEntry.TABLE_NAME, _selection,
                    _selectionArgs
                )
            }
            else -> throw IllegalArgumentException(
                "Can't delete this URI "
                        + uri
            )
        }
        if (rowsDeleted != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return rowsDeleted
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (values!!.containsKey(MemberEntry.COLUMN_FIRST_NAME)) {
            values.getAsString(MemberEntry.COLUMN_FIRST_NAME)
                ?: throw IllegalArgumentException("You have to input first name")
        }
        if (values.containsKey(MemberEntry.COLUMN_LAST_NAME)) {
            values.getAsString(MemberEntry.COLUMN_LAST_NAME)
                ?: throw IllegalArgumentException("You have to input last name")
        }
        if (values.containsKey(MemberEntry.COLUMN_GENDER)) {
            val gender: Int = values.getAsInteger(MemberEntry.COLUMN_GENDER)
            require(
                gender == MemberEntry.GENDER_UNKNOWN || gender ==
                        MemberEntry.GENDER_MALE || gender == MemberEntry.GENDER_FEMALE
            ) { "You have to input correct gender" }
        }
        if (values.containsKey(MemberEntry.COLUMN_SPORT)) {
            values.getAsString(MemberEntry.COLUMN_SPORT)
                ?: throw IllegalArgumentException("You have to input sport")
        }
        val db: SQLiteDatabase = dbOpenHelper!!.writableDatabase
        val match: Int = uriMatcher.match(uri)
        val rowsUpdated: Int
        when (match) {
            MEMBERS -> rowsUpdated = db.update(
                MemberEntry.TABLE_NAME, values,
                selection, selectionArgs
            )
            MEMBER_ID -> {
                val _selection = MemberEntry._ID + "=?"
                val _selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsUpdated = db.update(
                    MemberEntry.TABLE_NAME, values,
                    _selection, _selectionArgs
                )
            }
            else -> throw IllegalArgumentException(
                "Can't update this URI "
                        + uri
            )
        }
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            MEMBERS -> MemberEntry.CONTENT_MULTIPLE_ITEMS
            MEMBER_ID -> MemberEntry.CONTENT_SINGLE_ITEM
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}