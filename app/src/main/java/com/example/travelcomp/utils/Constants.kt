package com.example.travelcomp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.travelcomp.activities.MyProfileActivity


object Constants {
    const val USERS: String = "users"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val EMAIL: String = "email"
    const val MOBILE: String = "mobile"
    const val BOARDS: String = "boards"
    const val CITIES: String = "city"
    const val CITYLIST: String = "cityList"
    const val DOCUMENT_ID: String = "documentId"
    const val TRAVEL_DETAIL: String = "travel_detail"
    const val ID: String = "id"
    const val MEMBERS: String = "members"
    const val INTENTBOARD: String = "board"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}