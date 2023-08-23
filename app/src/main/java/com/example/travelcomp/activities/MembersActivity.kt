package com.example.travelcomp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelcomp.R
import com.example.travelcomp.adapters.MemberListItemsAdapter
import com.example.travelcomp.databinding.ActivityBoardSeeBinding
import com.example.travelcomp.databinding.ActivityMembersBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.models.User
import com.example.travelcomp.utils.Constants

@Suppress("DEPRECATION")
class MembersActivity : BaseActivity() {
    private var binding: ActivityMembersBinding? = null
    private lateinit var mTravelDetails: Board
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val memberList: ArrayList<User>? = intent.getParcelableArrayListExtra(Constants.MEMBERS)

        if (memberList != null) {
            setupMembersList(memberList)
        }

        if (intent.hasExtra(Constants.TRAVEL_DETAIL)) {
            mTravelDetails = intent.getParcelableExtra<Board>(Constants.TRAVEL_DETAIL)!!
            mUserDetails = intent.getParcelableExtra<User>("user")!!
        }

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getOtherMembersListDetails(this, mTravelDetails.members)
    }

    fun setupMembersList(list: ArrayList<User>) {
        hideProgressDialog()
        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
        binding?.rvMembersList?.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list, mTravelDetails, mUserDetails)
        binding?.rvMembersList?.adapter = adapter
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            title = "Members"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }
}