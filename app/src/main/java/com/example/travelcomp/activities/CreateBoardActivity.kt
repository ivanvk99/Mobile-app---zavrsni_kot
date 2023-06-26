package com.example.travelcomp.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityCreateBoardBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private var binding: ActivityCreateBoardBinding? = null

    private lateinit var mUserName: String

    private var mBoardImageURL: String = ""
    private var mStartDate: String = ""
    private var mEndDate: String = ""
    private var membersArrayList: ArrayList<String> = ArrayList()
    private lateinit var description: String
    private lateinit var city: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME) ?: ""
        }


        binding?.ivBoardImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        // Date Picker Button
        val datePickerButton = findViewById<Button>(R.id.btn_date_range_picker)
        datePickerButton.setOnClickListener {
            showDatePicker(datePickerButton)
        }

        setupActionBar()

        binding?.btnCreate?.setOnClickListener {
            description = binding?.etBoardDescription?.text.toString()
            city = binding?.actvCitySelection?.text.toString()
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    // Show the date picker dialog
    private fun showDatePicker(datePickerButton: Button) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val selectedDate = selectedCalendar.time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate)

                // Set the selected date to the appropriate field
                if (mStartDate.isEmpty()) {
                    // Validate if the selected start date is in the past
                    if (selectedCalendar.before(Calendar.getInstance())) {
                        // Display an error message or handle the case as needed
                        // For example, show a Toast message
                        Toast.makeText(this, "Please select a future date as the start date", Toast.LENGTH_SHORT).show()
                    } else {
                        mStartDate = formattedDate
                        datePickerButton.text = formattedDate
                    }
                } else {
                    mEndDate = formattedDate
                    datePickerButton.text = formattedDate
                }
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.show()
    }

    private fun createBoard() {
        membersArrayList.add(getCurrentUserID())

        val board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            membersArrayList,
            mStartDate,
            mEndDate,
            description,
            city

        )

        FirestoreClass().createBoard(this, board)
    }
        private fun uploadBoardImage() {
            showProgressDialog(resources.getString(R.string.please_wait))

            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis() + "." +
                        Constants.getFileExtension(this, mSelectedImageFileUri)
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("Downloadable Image URL", uri.toString())
                    mBoardImageURL = uri.toString()

                    createBoard()

                }

            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }


        fun boardCreatedSuccessfully() {

            val intent = Intent(this, BoardSeeActivity::class.java)
            intent.putExtra("boardName", binding?.etBoardName?.text.toString())
            intent.putExtra("startDate", mStartDate)
            intent.putExtra("endDate", mEndDate)
            intent.putExtra("description", binding?.etBoardDescription?.text.toString())
            intent.putExtra("city", binding?.actvCitySelection?.text.toString())
            // Add any other necessary data

            startActivity(intent)

        }


        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Constants.showImageChooser(this)
            } else {
                if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Constants.showImageChooser(this)
                    } else {
                        Toast.makeText(
                            this,
                            "You denied the permission for storage.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        }


        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
                mSelectedImageFileUri = data.data


                try {
                    Glide
                        .with(this@CreateBoardActivity)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(binding?.ivBoardImage as CircleImageView)
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            }
        }

        private fun setupActionBar() {
            val toolbar = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
                title = resources.getString(R.string.create_board_title)
            }
            toolbar.setNavigationOnClickListener { onBackPressed() }

        }

}