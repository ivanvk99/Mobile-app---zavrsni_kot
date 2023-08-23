package com.example.travelcomp.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityBoardSeeBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.models.Feedback
import com.example.travelcomp.models.User
import com.example.travelcomp.utils.Constants


class BoardSeeActivity : BaseActivity() {

    private var binding: ActivityBoardSeeBinding? = null
    private lateinit var mTravelDetail: Board
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardSeeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

//        if (intent.hasExtra(Constants.INTENTBOARD)) {
//            mTravelDetail = (intent.getParcelableExtra(Constants.INTENTBOARD) as? Board)!!
//        }

        showProgressDialog(resources.getString(R.string.please_wait))
        createMemberDialog()
    }

    var boardDocumentId = ""

    override fun onResume() {
        super.onResume()
//        travelDetails(mTravelDetail)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
            mUserDetails = intent.getParcelableExtra("user")!!
        }
        FirestoreClass().getBoardDetails(this, boardDocumentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.TRAVEL_DETAIL, mTravelDetail)
                intent.putExtra("user", mUserDetails)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupActionBar(title: String) {

        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mTravelDetail.name
        }

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun travelDetails(board: Board) {
        mTravelDetail = board
        hideProgressDialog()
        setupActionBar(board.name)
        val userImage = binding?.ivBoardImage!!

        Glide
            .with(this@BoardSeeActivity)
            .load(board.image)
            .centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(userImage)

        binding?.textViewCreatedBy?.text = board.createdBy
        binding?.textViewDescription?.text = board.description
        binding?.textViewCity?.text = board.city
        binding?.textViewStartDate?.text = board.date
        binding?.textViewEmail?.text = board.email
        binding?.textViewNumber?.text = board.phoneNumber

        binding?.textViewEmail?.setOnClickListener {
            if (binding?.textViewEmail?.text!!.isNotEmpty()) {
                val myStrings = arrayOf(board.email)
                shareToGMail(myStrings, "Inquiry", "")
            }
        }

        binding?.textViewNumber?.setOnClickListener {
            if (binding?.textViewNumber?.text!!.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + board.phoneNumber)
                startActivity(intent)
            }
        }

        binding?.textViewCity?.setOnClickListener {
            if (board.latitude.isNotEmpty() && board.longitude.isNotEmpty()) {
                openGoogleDirection(
                    this@BoardSeeActivity,
                    board.latitude,
                    board.longitude,
                    board.city
                )
            }
        }
        if (getCurrentUserID() == board.userID || !mUserDetails.user)
            binding?.llButtons?.visibility = View.VISIBLE
        else
            binding?.llButtons?.visibility = View.GONE

        binding?.btnEdit?.setOnClickListener {
            val intent = Intent(this@BoardSeeActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.INTENTBOARD, board)
            startActivity(intent)
        }

        binding?.btnDelete?.setOnClickListener {
            FirestoreClass().deleteBoard(this@BoardSeeActivity, board)
        }

        binding?.btnMember?.setOnClickListener {
            dialog!!.show()
        }

        binding?.btnFeedbackSubmit?.setOnClickListener {
            val feedback = Feedback(
                getCurrentUserID(),
                binding?.ratingBar?.rating.toString(),
                binding?.edtFeedbackDis?.text.toString().trim()
            )

            board.feedback[feedback.id] = feedback
            FirestoreClass().addRating(this@BoardSeeActivity, boardDocumentId, board.feedback)
        }

        if (board.feedback.contains(getCurrentUserID())) {
            val feedback = board.feedback.getValue(getCurrentUserID())
            binding?.edtFeedbackDis?.setText(feedback.description)
            binding?.edtFeedbackDis?.isEnabled = false
            binding?.ratingBar?.rating = feedback.rating.toFloat()
            binding?.ratingBar?.setIsIndicator(true)
            binding?.btnFeedbackSubmit?.visibility = View.GONE
        }
//        else if (!mUserDetails.user){
//            for (feedback in board.feedback){
//
//            }
//        }
    }

    fun ratingSubmited() {
        binding?.edtFeedbackDis?.isEnabled = false
        binding?.ratingBar?.setIsIndicator(true)
        binding?.btnFeedbackSubmit?.visibility = View.GONE
    }

    var dialog: Dialog? = null
    fun createMemberDialog() {
        dialog = Dialog(this@BoardSeeActivity)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setContentView(R.layout.dialog_add_member)
        val edtEmail = dialog!!.findViewById<View>(R.id.edt_email) as EditText
        val btnAdd = dialog!!.findViewById<View>(R.id.btn_add) as CardView
        val btnClose = dialog!!.findViewById<View>(R.id.btn_close) as ImageView
        btnClose.setOnClickListener {
            dialog!!.dismiss()
        }
        btnAdd.setOnClickListener {
            if (edtEmail.text.toString().isNotEmpty()) {
                FirestoreClass().addMembers(
                    this@BoardSeeActivity,
                    edtEmail.text.toString().trim(),
                    mTravelDetail.documentId,
                    mTravelDetail.members
                )
            }
        }
        dialog?.setOnDismissListener {
            edtEmail.setText("")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun shareToGMail(email: Array<String>, subject: String?, content: String?) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_TEXT, content)
        emailIntent.setPackage("com.google.android.gm")
        if (intent.resolveActivity(packageManager) != null)
            startActivity(Intent.createChooser(emailIntent, "Send mail"))
        else
            Toast.makeText(this, "Gmail App is not installed", Toast.LENGTH_SHORT).show();
    }


    fun openGoogleDirection(
        context: Context,
        latitude: String,
        longitude: String,
        locationName: String
    ) {
        val uri =
            "http://maps.google.com/maps?daddr=$latitude,$longitude ($locationName)"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Only if initiating from a Broadcast Receiver
        val mapsPackageName = "com.google.android.apps.maps"
        if (isPackageExisted(context, mapsPackageName)) {
            i.setClassName(mapsPackageName, "com.google.android.maps.MapsActivity")
            i.setPackage(mapsPackageName)
        }
        context.startActivity(i)
    }

    fun isPackageExisted(context: Context, targetPackage: String?): Boolean {
        val pm = context.packageManager
        try {
            val info = pm.getPackageInfo(targetPackage!!, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }
}