package com.example.travelcomp.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.travelcomp.MyApp
import com.example.travelcomp.activities.BaseActivity
import com.example.travelcomp.activities.BoardSeeActivity
import com.example.travelcomp.activities.CreateBoardActivity
import com.example.travelcomp.activities.LogInActivity
import com.example.travelcomp.activities.MainActivity
import com.example.travelcomp.activities.MembersActivity
import com.example.travelcomp.activities.MyProfileActivity
import com.example.travelcomp.activities.SignUpActivity
import com.example.travelcomp.models.Board
import com.example.travelcomp.models.Feedback
import com.example.travelcomp.models.User
import com.example.travelcomp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId())
            .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "error", e)
            }

    }

    fun getBoardDetails(activity: BoardSeeActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                activity.travelDetails(document.toObject(Board::class.java)!!)
            }

            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while fetching boards", e)
                // Handle failure
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        val newRef = mFireStore.collection(Constants.BOARDS).document()
        board.documentId = newRef.id
        newRef.set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully")

                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully(board.documentId)
            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName, "Error while creating a board", exception
                )
            }
    }

    fun updateBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS).document(board.documentId).set(board)
            .addOnSuccessListener {
                Toast.makeText(activity, "Board update successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully(board.documentId)
            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName, "Error while creating a board", exception
                )
            }
    }

    fun deleteBoard(activity: BaseActivity, board: Board) {
        activity.showProgressDialog("Deleting...")
        mFireStore.collection(Constants.BOARDS).document(board.documentId).delete()
            .addOnSuccessListener {
                Toast.makeText(activity, "Board delete successfully", Toast.LENGTH_SHORT).show()
                activity.finish()
            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName, "Error while creating a board", exception
                )
            }
    }

    fun getTravelsList(activity: MainActivity) {
//        .whereArrayContains(Constants.MEMBERS, getCurrentUserId())
        mFireStore.collection(Constants.BOARDS)
            .get()
            .addOnSuccessListener { documents ->
                val boardList: ArrayList<Board> = ArrayList()
                for (document in documents) {
                    val board = document.toObject(Board::class.java)
                    board?.documentId = document.id
                    board?.let { boardList.add(it) }
                }
                Log.d(activity.javaClass.simpleName, "Number of boards: ${boardList.size}")
                activity.populateTravelListToUI(boardList)
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while fetching boards", e)
                // Handle failure
            }
    }

    fun getCitiesList(activity: MainActivity) {
        mFireStore.collection(Constants.CITIES)
            .get()
            .addOnSuccessListener { documents ->
                val cityList: ArrayList<String> = ArrayList()
                for (document in documents) {
                    val variable = document.get(Constants.CITYLIST) as List<String>
                    cityList.addAll(variable)
                }
                MyApp.instance.allCityList.addAll(cityList)
                Log.d(activity.javaClass.simpleName, "Number of boards: ${cityList.size}")
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while fetching boards", e)
                // Handle failure
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserId()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.e(activity.javaClass.simpleName, "Profile Data updated successfully!")

                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun loadUserData(activity: Activity, readTravelsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val loggedInUser = document.toObject(User::class.java)!!
                when (activity) {
                    is LogInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readTravelsList)
                    }

                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is LogInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }


    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getOtherMembersListDetails(activity: MembersActivity, memberIds: ArrayList<String>) {
        val db = FirebaseFirestore.getInstance()
        val membersCollection = mFireStore.collection(Constants.USERS)
        val membersList: ArrayList<User> = ArrayList()

        membersCollection.whereIn(Constants.ID, memberIds)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                for (i in document.documents) {
                    // Convert all the document snapshot to the object using the data model class.
                    val user = i.toObject(User::class.java)!!
                    membersList.add(user)
                }

                activity.setupMembersList(membersList)
            }
            .addOnFailureListener { exception ->
                Log.e(activity.javaClass.simpleName, "Error getting members list", exception)
                // Handle failure
            }
    }

    fun deleteMember(context: Context, documentId: String, tempMemberId: ArrayList<String>) {
        mFireStore.collection(Constants.BOARDS).document(documentId)
            .update(
                Constants.MEMBERS,
                tempMemberId
            )
        Toast.makeText(context, "Member is deleted", Toast.LENGTH_LONG).show()
    }

    fun addMembers(
        activity: BoardSeeActivity,
        email: String,
        documentId: String,
        tempMemberId: ArrayList<String>
    ) {
        activity.showProgressDialog("Adding member...")
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.size() == 0) {
                    Toast.makeText(activity, "Email not found", Toast.LENGTH_LONG).show()
                } else {
                    val user = document.documents[0].toObject(User::class.java)!!
                    if (tempMemberId.contains(user.id)) {
                        Toast.makeText(activity, "Member is existed", Toast.LENGTH_LONG).show()
                        activity.hideProgressDialog()
                    } else {
                        tempMemberId.add(user.id)
                        mFireStore.collection(Constants.BOARDS).document(documentId)
                            .update(Constants.MEMBERS, tempMemberId)
                            .addOnSuccessListener {
                                Toast.makeText(activity, "Member is Added", Toast.LENGTH_LONG)
                                    .show()
                                activity.dialog!!.dismiss()
                                activity.hideProgressDialog()
                            }
                    }
//                    for (snapShot in document) {
//                        val user = snapShot.toObject(User::class.java)!!
//
//                    }
                }

            }
    }

    fun addRating(
        activity: BoardSeeActivity,
        documentId: String,
        myMap: HashMap<String, Feedback>
    ) {
        activity.showProgressDialog("Submit...")
        mFireStore.collection(Constants.BOARDS).document(documentId)
            .update("feedback", myMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Feedback submitted", Toast.LENGTH_LONG)
                    .show()
                activity.hideProgressDialog()
                activity.ratingSubmited()
            }
    }
}