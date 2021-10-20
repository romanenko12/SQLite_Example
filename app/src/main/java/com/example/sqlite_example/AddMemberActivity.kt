package com.example.sqlite_example

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.sqlite_example.data.ClubOlympusContract.MemberEntry

class AddMemberActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor?> {
    private var currentMemberUri: Uri? = null
    private var firstNameEditText: EditText? = null
    private var lastNameEditText: EditText? = null
    private var sportEditText: EditText? = null
    private var genderSpinner: Spinner? = null
    private var gender = 0
    private var spinnerAdapter: ArrayAdapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)
        val intent: Intent = intent
        currentMemberUri = intent.data
        if (currentMemberUri == null) {
            title = "Add a Member"
            invalidateOptionsMenu()
        } else {
            title = "Edit the Member"
            LoaderManager.getInstance(this).initLoader(
                EDIT_MEMBER_LOADER,
                null, this
            )
        }
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        sportEditText = findViewById(R.id.sportEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.array_gender, android.R.layout.simple_spinner_item
        )
        spinnerAdapter!!.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        genderSpinner!!.adapter = spinnerAdapter
        genderSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                val selectedGender = parent.getItemAtPosition(position) as String
                if (!TextUtils.isEmpty(selectedGender)) {
                    gender = when (selectedGender) {
                        "Male" -> {
                            MemberEntry.GENDER_MALE
                        }
                        "Female" -> {
                            MemberEntry.GENDER_FEMALE
                        }
                        else -> {
                            MemberEntry.GENDER_UNKNOWN
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                gender = 0
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (currentMemberUri == null) {
            val menuItem = menu.findItem(R.id.delete_member)
            menuItem.isVisible = false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_member_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_member -> {
                saveMember()
                return true
            }
            R.id.delete_member -> {
                showDeleteMemberDialog()
                return true
            }
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveMember() {
        val firstName: String = firstNameEditText!!.text.toString().trim { it <= ' ' }
        val lastName: String = lastNameEditText!!.text.toString().trim { it <= ' ' }
        val sport: String = sportEditText!!.text.toString().trim { it <= ' ' }
        when {
            TextUtils.isEmpty(firstName) -> {
                Toast.makeText(
                    this,
                    "Input the first name",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            TextUtils.isEmpty(lastName) -> {
                Toast.makeText(
                    this,
                    "Input the last name",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            TextUtils.isEmpty(sport) -> {
                Toast.makeText(
                    this,
                    "Input the sport",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            gender == MemberEntry.GENDER_UNKNOWN -> {
                Toast.makeText(
                    this,
                    "Choose the gender",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            else -> {
                val contentValues = ContentValues()
                contentValues.put(MemberEntry.COLUMN_FIRST_NAME, firstName)
                contentValues.put(MemberEntry.COLUMN_LAST_NAME, lastName)
                contentValues.put(MemberEntry.COLUMN_SPORT, sport)
                contentValues.put(MemberEntry.COLUMN_GENDER, gender)
                if (currentMemberUri == null) {
                    val contentResolver: ContentResolver = contentResolver
                    contentResolver.insert(
                        MemberEntry.CONTENT_URI,
                        contentValues
                    )!!
                    Toast.makeText(
                        this,
                        "Data saved", Toast.LENGTH_LONG
                    ).show()
                } else {
                    val rowsChanged: Int = contentResolver.update(
                        currentMemberUri!!,
                        contentValues, null, null
                    )
                    if (rowsChanged == 0) {
                        Toast.makeText(
                            this,
                            "Saving of data in the table failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Member updated", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun showDeleteMemberDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want delete the member?")
        builder.setPositiveButton("Delete"
        ) { _, _ -> deleteMember() }
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteMember() {
        if (currentMemberUri != null) {
            val rowsDeleted: Int = contentResolver.delete(
                currentMemberUri!!,
                null, null
            )
            if (rowsDeleted == 0) {
                Toast.makeText(
                    this,
                    "Deleting of data from the table failed",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Member is deleted",
                    Toast.LENGTH_LONG
                ).show()
            }
            finish()
        }
    }

    companion object {
        private const val EDIT_MEMBER_LOADER = 111
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        if (data!!.moveToFirst()) {
            val firstNameColumnIndex = data.getColumnIndex(
                MemberEntry.COLUMN_FIRST_NAME
            )
            val lastNameColumnIndex = data.getColumnIndex(
                MemberEntry.COLUMN_LAST_NAME
            )
            val genderColumnIndex = data.getColumnIndex(
                MemberEntry.COLUMN_GENDER
            )
            val sportColumnIndex = data.getColumnIndex(
                MemberEntry.COLUMN_SPORT
            )
            val firstName = data.getString(firstNameColumnIndex)
            val lastName = data.getString(lastNameColumnIndex)
            val gender = data.getInt(genderColumnIndex)
            val sport = data.getString(sportColumnIndex)
            firstNameEditText!!.setText(firstName)
            lastNameEditText!!.setText(lastName)
            sportEditText!!.setText(sport)
            when (gender) {
                MemberEntry.GENDER_MALE -> genderSpinner!!.setSelection(1)
                MemberEntry.GENDER_FEMALE -> genderSpinner!!.setSelection(2)
                MemberEntry.GENDER_UNKNOWN -> genderSpinner!!.setSelection(0)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {}

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val projection = arrayOf(
            MemberEntry._ID,
            MemberEntry.COLUMN_FIRST_NAME,
            MemberEntry.COLUMN_LAST_NAME,
            MemberEntry.COLUMN_GENDER,
            MemberEntry.COLUMN_SPORT
        )
        return CursorLoader(
            this,
            currentMemberUri!!,
            projection,
            null,
            null,
            null
        )
    }
}