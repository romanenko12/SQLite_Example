package com.example.sqlite_example

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.example.sqlite_example.data.ClubOlympusContract.MemberEntry

class MainActivity : AppCompatActivity(),
    LoaderManager.LoaderCallbacks<Cursor?> {
    private var memberCursorAdapter: MemberCursorAdapter? = null
    private var dataListView: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataListView = findViewById(R.id.dataListView)
        val floatingActionButton: FloatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                AddMemberActivity::class.java
            )
            startActivity(intent)
        }
        memberCursorAdapter = MemberCursorAdapter(
            this,
            null, false
        )
        dataListView!!.adapter = memberCursorAdapter
        dataListView!!.onItemClickListener = OnItemClickListener { _, _, _, id ->
            val intent = Intent(
                this@MainActivity,
                AddMemberActivity::class.java
            )
            val currentMemberUri = ContentUris
                .withAppendedId(MemberEntry.CONTENT_URI, id)
            intent.data = currentMemberUri
            startActivity(intent)
        }
        LoaderManager.getInstance(this).initLoader(
            MEMBER_LOADER,
            null, this
        )
    }

    companion object {
        private const val MEMBER_LOADER = 123
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        memberCursorAdapter!!.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        memberCursorAdapter!!.swapCursor(null)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val projection = arrayOf(
            MemberEntry._ID,
            MemberEntry.COLUMN_FIRST_NAME,
            MemberEntry.COLUMN_LAST_NAME,
            MemberEntry.COLUMN_SPORT
        )
        return CursorLoader(
            this,
            MemberEntry.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
    }
}