package com.hy.picker.utils

import android.content.Context
import com.hy.picker.R
import java.util.*

/**
 *
 * Permissions.
 * Created by Zhenjie Yan on 2017/8/4.
 */
object Permission {

    const val READ_CALENDAR = "android.permission.READ_CALENDAR"
    const val WRITE_CALENDAR = "android.permission.WRITE_CALENDAR"

    const val CAMERA = "android.permission.CAMERA"

    const val READ_CONTACTS = "android.permission.READ_CONTACTS"
    const val WRITE_CONTACTS = "android.permission.WRITE_CONTACTS"
    const val GET_ACCOUNTS = "android.permission.GET_ACCOUNTS"

    const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"
    const val ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"

    const val RECORD_AUDIO = "android.permission.RECORD_AUDIO"

    const val READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"
    const val CALL_PHONE = "android.permission.CALL_PHONE"
    const val READ_CALL_LOG = "android.permission.READ_CALL_LOG"
    const val WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG"
    const val ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL"
    const val ADD_VOICEMAIL_MANIFEST = "android.permission.ADD_VOICEMAIL"
    const val USE_SIP = "android.permission.USE_SIP"
    const val PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS"
    const val READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS"
    const val ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS"

    const val BODY_SENSORS = "android.permission.BODY_SENSORS"

    const val SEND_SMS = "android.permission.SEND_SMS"
    const val RECEIVE_SMS = "android.permission.RECEIVE_SMS"
    const val READ_SMS = "android.permission.READ_SMS"
    const val RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH"
    const val RECEIVE_MMS = "android.permission.RECEIVE_MMS"

    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"

    /**
     * Turn permissions into text.
     */
    fun transformText(context: Context, vararg permissions: String): List<String> {
        return transformText(context, listOf(*permissions))
    }

    /**
     * Turn permissions into text.
     */
    fun transformText(context: Context, vararg groups: Array<String>): List<String> {
        val permissionList = ArrayList<String>()
        for (group in groups) {
            permissionList.addAll(listOf(*group))
        }
        return transformText(context, permissionList)
    }

    /**
     * Turn permissions into text.
     */
    fun transformText(context: Context, permissions: List<String>): List<String> {
        val textList = ArrayList<String>()
        for (permission in permissions) {
            when (permission) {
                READ_CALENDAR, WRITE_CALENDAR -> {
                    val message = context.getString(R.string.picker_permission_name_calendar)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }

                CAMERA -> {
                    val message = context.getString(R.string.picker_permission_name_camera)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                READ_CONTACTS, WRITE_CONTACTS -> {
                    val message = context.getString(R.string.picker_permission_name_contacts)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                GET_ACCOUNTS -> {
                    val message = context.getString(R.string.picker_permission_name_accounts)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> {
                    val message = context.getString(R.string.picker_permission_name_location)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                RECORD_AUDIO -> {
                    val message = context.getString(R.string.picker_permission_name_microphone)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                READ_PHONE_STATE, CALL_PHONE,
                READ_CALL_LOG, WRITE_CALL_LOG,
                ADD_VOICEMAIL, ADD_VOICEMAIL_MANIFEST,
                USE_SIP, PROCESS_OUTGOING_CALLS,
                READ_PHONE_NUMBERS, ANSWER_PHONE_CALLS -> {
                    val message = context.getString(R.string.picker_permission_name_phone)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                BODY_SENSORS -> {
                    val message = context.getString(R.string.picker_permission_name_sensors)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                SEND_SMS, RECEIVE_SMS,
                READ_SMS, RECEIVE_WAP_PUSH,
                RECEIVE_MMS -> {
                    val message = context.getString(R.string.picker_permission_name_sms)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE -> {
                    val message = context.getString(R.string.picker_permission_name_storage)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
            }
        }
        return textList
    }

}

