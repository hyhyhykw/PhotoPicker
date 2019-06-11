package com.hy.picker.model

import android.text.TextUtils

/**
 * Created by donglua on 15/6/28.
 */
class PhotoDirectory {

    var id: String? = null
    var coverPath: String? = null
    var name: String? = null
    var dateAdded = 0L
    val photos = ArrayList<Photo>()

    fun addPhoto(photo: Photo) {
        photos.add(photo)
    }

    fun addPhoto(index: Int, photo: Photo) {
        photos.add(index, photo)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (javaClass != other?.javaClass) return false

        other as PhotoDirectory

        val hasId = !TextUtils.isEmpty(id)
        val otherHasId = !TextUtils.isEmpty(other.id)

        if (hasId && otherHasId) {
            return id == other.id && name == other.name
        }

        return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

}
