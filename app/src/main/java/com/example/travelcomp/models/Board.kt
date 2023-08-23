package com.example.travelcomp.models

import android.os.Parcel
import android.os.Parcelable


data class Board(
    val name: String = "",
    val image: String = "",
    val createdBy: String = "",
    val members: ArrayList<String> = ArrayList(),
    var documentId: String = "",
    val date: String = "",
    val description: String = "",
    val city: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userID: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val feedback: HashMap<String, Feedback> = HashMap<String, Feedback>()

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(members)
        parcel.writeString(documentId)
        parcel.writeString(date)
        parcel.writeString(description)
        parcel.writeString(city)
        parcel.writeString(email)
        parcel.writeString(phoneNumber)
        parcel.writeString(userID)
        parcel.writeString(latitude)
        parcel.writeString(longitude)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Board> = object : Parcelable.Creator<Board> {
            override fun createFromParcel(source: Parcel): Board = Board(source)
            override fun newArray(size: Int): Array<Board?> = arrayOfNulls(size)
        }
    }
}
