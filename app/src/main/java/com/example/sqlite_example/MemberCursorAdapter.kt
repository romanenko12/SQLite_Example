package com.example.sqlite_example

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.sqlite_example.data.ClubOlympusContract.MemberEntry

class MemberCursorAdapter(context: Context?, c: Cursor?, autoRequery: Boolean) :
    CursorAdapter(context, c, autoRequery) {
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(
            R.layout.member_item, parent,
            false
        )
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val firstNameTextView: TextView = view.findViewById(R.id.firstNameTextView)
        val lastNameTextView: TextView = view.findViewById(R.id.lastNameTextView)
        val sportTextView: TextView = view.findViewById(R.id.sportNameTextView)
        val firstName =
            cursor.getString(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_FIRST_NAME))
        val lastName = cursor.getString(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_LAST_NAME))
        val sport = cursor.getString(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_SPORT))
        firstNameTextView.text = firstName
        lastNameTextView.text = lastName
        sportTextView.text = sport
    }
}