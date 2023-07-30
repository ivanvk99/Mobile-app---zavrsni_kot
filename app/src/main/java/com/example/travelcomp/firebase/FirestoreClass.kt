package com.example.travelcomp.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.travelcomp.activities.*
import com.example.travelcomp.models.Board
import com.example.travelcomp.models.User
import com.example.travelcomp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(userInfo, SetOptions.merge()).addOnSuccessListener {
            activity.userRegisteredSuccess()
        }.addOnFailureListener{
            e->
            Log.e(activity.javaClass.simpleName, "error", e)
        }

    }

    fun getBoardDetails(activity: BoardSeeActivity, documentId : String){
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

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully")

                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName, "Error while creating a board", exception
                )
            }
    }

    fun getTravelsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.MEMBERS, getCurrentUserId())
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
                    is MyProfileActivity ->{
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


    fun getCurrentUserId(): String{

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
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


}