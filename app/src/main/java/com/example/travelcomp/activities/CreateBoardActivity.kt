package com.example.travelcomp.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelcomp.MyApp
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityCreateBoardBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.utils.Constants
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
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
    private var docID: String = ""
    private var mEndDate: String = ""
    private var mLatitude: String = ""
    private var mLongitude: String = ""
    private var membersArrayList: ArrayList<String> = ArrayList()
    private lateinit var description: String
    private lateinit var email: String
    private lateinit var phoneNumber: String
    private lateinit var city: String
    var dialog1: Dialog? = null
    var oldBoard: Board? = null
    var autocompleteFragment: AutocompleteSupportFragment? = null


    private fun isPositionInArrayList(position: Int, arrayList: List<Any>): Boolean {
        return position in arrayList.indices
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                API_KEY_PART_1 + API_KEY_PART_2 + API_KEY_PART_3 + API_KEY_PART_4,
                Locale.US
            )
        }

        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment!!.setHint("Enter a city name")
        autocompleteFragment!!.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                autocompleteFragment!!.setText("")

                if (binding?.framelayout?.visibility == View.VISIBLE) {
                    binding?.framelayout?.visibility = View.GONE
                    binding?.cardLinear?.visibility = View.VISIBLE
                }
                mLatitude = place.latLng.latitude.toString()
                mLongitude = place.latLng.longitude.toString()

                binding?.actvCitySelection?.setText(
                    getCompleteAddressString(
                        place.latLng.latitude,
                        place.latLng.longitude
                    )
                )
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
//                Toast.makeText(MapLocationActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        })

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME) ?: ""
        }

        if (intent.hasExtra(Constants.INTENTBOARD)) {
            oldBoard = intent.getParcelableExtra(Constants.INTENTBOARD) as? Board
            binding?.etBoardDescription?.setText(oldBoard!!.description)
            binding?.actvCitySelection?.setText(oldBoard!!.city)
            binding?.etEmailInfo?.setText(oldBoard!!.email)
            binding?.etPhoneInfo?.setText(oldBoard!!.phoneNumber)
            binding?.etBoardName?.setText(oldBoard!!.name)
            Glide
                .with(this@CreateBoardActivity)
                .load(oldBoard!!.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivBoardImage!!)

            val date = oldBoard!!.date
            val dates = date.split("-")
            if (dates.isNotEmpty()) {
                if (isPositionInArrayList(0, dates)) {
                    binding?.btnStartDate?.text = dates[0]
                    mStartDate = dates[0]
                }

                if (isPositionInArrayList(1, dates)) {
                    binding?.btnEndDate?.text = dates[1]
                    mEndDate = dates[1]
                }
            } else {
                binding?.btnStartDate?.text = oldBoard!!.date
                mStartDate = oldBoard!!.date
            }
            binding?.btnCreate?.text = "Update"
            mUserName = oldBoard!!.createdBy
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
        binding?.btnStartDate?.setOnClickListener {
            showDatePicker(true)
        }

        binding?.btnEndDate?.setOnClickListener {
            if (mStartDate.isNotEmpty()) {
                showDatePicker(false)
            } else {
                Toast.makeText(
                    this,
                    "Please select start date",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setupActionBar()

        binding?.actvCitySelection?.setOnClickListener {
            if (binding?.framelayout?.visibility == View.GONE) {
                binding?.framelayout?.visibility = View.VISIBLE
                binding?.cardLinear?.visibility = View.GONE
            }
//            dialog1!!.show()
        }

        binding?.btnCreate?.setOnClickListener {
            description = binding?.etBoardDescription?.text.toString()
            city = binding?.actvCitySelection?.text.toString()
            email = binding?.etEmailInfo?.text.toString()
            phoneNumber = binding?.etPhoneInfo?.text.toString()
            if (binding?.etBoardName?.text.toString().isNotEmpty()) {
                if (mStartDate.isNotEmpty() && mEndDate.isNotEmpty()) {
                    if (description.isNotEmpty()) {
                        if (city.isNotEmpty()) {
                            if (email.isNotEmpty() && isEmailValid(email)) {
                                if (phoneNumber.isNotEmpty() && isPhoneNumberValid(phoneNumber)) {
                                    if (mSelectedImageFileUri != null) {
                                        uploadBoardImage()
                                    } else {
                                        showProgressDialog(resources.getString(R.string.please_wait))
                                        createBoard()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@CreateBoardActivity,
                                        "Please check phone number",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@CreateBoardActivity,
                                    "Please check email",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@CreateBoardActivity,
                                "Please enter city",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@CreateBoardActivity,
                            "Please enter description",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@CreateBoardActivity,
                        "Please select start & end date",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@CreateBoardActivity,
                    "Please enter board name",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        showCitiesDialog()
    }

    fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val phoneNumberPattern = "^[+]?[0-9]{10,13}\$"
        return phoneNumber.matches(phoneNumberPattern.toRegex())
    }

    fun getCompleteAddressString(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addressText = ""

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                addressText = address.locality ?: ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }

    fun isDateRangeValid(startDate: Date, endDate: Date): Boolean {
        return !endDate.before(startDate) && endDate != startDate
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Show the date picker dialog
    private fun showDatePicker(isStartDate: Boolean) {
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
                val formattedDate = dateFormat.format(selectedDate)

                // Set the selected date to the appropriate field
                if (isStartDate) {
                    if (mStartDate.isEmpty() || mStartDate != formattedDate) {
                        // Validate if the selected start date is in the past
                        if (selectedCalendar.before(Calendar.getInstance())) {
                            // Display an error message or handle the case as needed
                            // For example, show a Toast message
                            Toast.makeText(
                                this,
                                "Please select a future date as the start date",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            mStartDate = formattedDate
                            binding?.btnStartDate?.text = formattedDate
                            binding?.btnEndDate?.text = "End date"
                            mEndDate = ""
                        }
                    }
                } else {
                    if (mEndDate.isEmpty() || mEndDate != formattedDate) {
                        // Validate if the selected start date is in the past
                        val startDate = dateFormat.parse(mStartDate)
                        val endDate = dateFormat.parse(formattedDate)

                        if (isDateRangeValid(startDate, endDate)) {
                            if (selectedCalendar.before(Calendar.getInstance())) {
                                // Display an error message or handle the case as needed
                                // For example, show a Toast message
                                Toast.makeText(
                                    this,
                                    "Please select a future date as the end date",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                mEndDate = formattedDate
                                binding?.btnEndDate?.text = formattedDate
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Date range is not valid.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            currentYear,
            currentMonth,
            currentDay
        )

        if (isStartDate)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        else {
            val oneDayInMillis = 24 * 60 * 60 * 1000L // Milliseconds in a day
            datePickerDialog.datePicker.minDate = dateStringToMillis(mStartDate) + oneDayInMillis
        }

        datePickerDialog.show()
    }

    fun dateStringToMillis(dateString: String): Long {
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }

    private fun createBoard() {
        membersArrayList.add(getCurrentUserID())
        if (oldBoard != null) {
            docID = oldBoard!!.documentId
            if (oldBoard!!.image.isNotEmpty())
                mBoardImageURL = oldBoard!!.image
        }
        val board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            membersArrayList,
            docID,
            "$mStartDate-$mEndDate",
            description,
            city,
            email,
            phoneNumber,
            getCurrentUserID(),
            mLatitude,
            mLongitude
        )

        if (oldBoard != null) {
            FirestoreClass().updateBoard(this, board)
        } else {
            FirestoreClass().createBoard(this, board)
        }
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


    fun boardCreatedSuccessfully(documentId: String) {
//        val intent = Intent(this, BoardSeeActivity::class.java)
//        intent.putExtra("boardName", binding?.etBoardName?.text.toString())
//        intent.putExtra("startDate", mStartDate)
//        intent.putExtra("endDate", mEndDate)
//        intent.putExtra("description", binding?.etBoardDescription?.text.toString())
//        intent.putExtra("city", binding?.actvCitySelection?.text.toString())
//        // Add any other necessary data
//        startActivity(intent)

        val intent = Intent(this@CreateBoardActivity, MainActivity::class.java)
//        intent.putExtra(Constants.DOCUMENT_ID, documentId)
        startActivity(intent)
        finishAffinity()
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
            title = if (oldBoard != null)
                resources.getString(R.string.update_board_title)
            else
                resources.getString(R.string.create_board_title)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

    override fun onBackPressed() {
        if (binding?.framelayout?.visibility == View.VISIBLE) {
            binding?.framelayout?.visibility = View.GONE
            binding?.cardLinear?.visibility = View.VISIBLE
            return
        }
        super.onBackPressed()
    }

    private fun showCitiesDialog() {
        dialog1 = Dialog(this@CreateBoardActivity)
        dialog1!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog1!!.setContentView(R.layout.dialog_cities)
        val edtSearch = dialog1!!.findViewById<View>(R.id.edt_search) as EditText
        val btnClose = dialog1!!.findViewById<View>(R.id.btn_close) as ImageView
        val cityRecyclerview = dialog1!!.findViewById<View>(R.id.city_recyclerview) as RecyclerView
        btnClose.setOnClickListener { dialog1!!.dismiss() }
        val adapter = LanguageAdapter(
            this@CreateBoardActivity,
            MyApp.instance.allCityList,
            object : RecyclerViewItemClick {
                override fun onItemClick(dataItem: String) {
                    binding?.actvCitySelection!!.setText(dataItem)
                    dialog1!!.dismiss()
                }
            })
        val manager =
            LinearLayoutManager(this@CreateBoardActivity, LinearLayoutManager.VERTICAL, false)
        cityRecyclerview.layoutManager = manager
        cityRecyclerview.adapter = adapter
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                // filter your list from your input
                filter(s.toString(), adapter)
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        })
    }

    fun filter(text: String, adapter: LanguageAdapter) {
        val temp: ArrayList<String> = arrayListOf()
        for (d in MyApp.instance.allCityList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.trim { it <= ' ' }
                    .lowercase(Locale.getDefault())
                    .contains(text.trim { it <= ' ' }.lowercase(Locale.getDefault()))) {
                temp.add(d)
            }
        }
        //update recyclerview
        adapter.updateList(temp)
    }

    class LanguageAdapter(
        private val activity: Activity,
        items: List<String>,
        recyclerViewItemClick: RecyclerViewItemClick?
    ) :
        RecyclerView.Adapter<LanguageAdapter.MyViewHolde>() {
        var items: List<String>
        var recyclerViewItemClick: RecyclerViewItemClick?

        init {
            this.items = items
            this.recyclerViewItemClick = recyclerViewItemClick
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolde {
            val view: View =
                LayoutInflater.from(activity).inflate(R.layout.item_city, parent, false)
            return MyViewHolde(view)
        }

        override fun onBindViewHolder(holder: MyViewHolde, position: Int) {
            val cityName: String = items[position]
            holder.txt_city_name.setText(cityName)
            holder.itemView.setOnClickListener {
                if (recyclerViewItemClick != null) recyclerViewItemClick!!.onItemClick(
                    cityName
                )
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class MyViewHolde(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txt_city_name: TextView

            init {
                txt_city_name = itemView.findViewById<View>(R.id.txt_city_name) as TextView
            }
        }

        fun updateList(list: List<String>) {
            items = list
            notifyDataSetChanged()
        }
    }

    interface RecyclerViewItemClick {
        fun onItemClick(dataItem: String)
    }

    val API_KEY_PART_1 = "ALzaSyAzcfa-"
    val API_KEY_PART_2 = "gLndXQqF6"
    val API_KEY_PART_3 = "_Wp-eFX12Y"
    val API_KEY_PART_4 = "CqBSIk5c"
}