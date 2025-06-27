package com.example.parkingagent.data.local

import android.content.Context
import android.content.SharedPreferences

import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException
import javax.inject.Inject

class SharedPreferenceManager @Inject constructor(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedPrefs()
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val fileName = TOKEN_MANAGER_NAME

        return try {
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: AEADBadTagException) {
            Log.w("SPM", "Encrypted prefs corrupted, deleting and recreating", e)
            context.deleteFile(fileName)
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Catch other keystore or crypto exceptions
            Log.w("SPM", "Error creating encrypted prefs, deleting and recreating", e)
            context.deleteFile(fileName)
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun getAccessToken(): String? = sharedPreferences.getString(TOKEN_KEY, null)

    fun saveAccessToken(token: String) {
        sharedPreferences.edit {
            putString(TOKEN_KEY, token)
        }
    }

    fun clearToken() {
        sharedPreferences.edit {
            remove(TOKEN_KEY)
        }
    }

    fun setLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit {
            putBoolean(IS_LOGGED_IN, isLoggedIn)
        }
    }

    fun getLoginStatus(): Boolean =
        sharedPreferences.getBoolean(IS_LOGGED_IN, false)

    fun setEntityId(entityId: Int) {
        sharedPreferences.edit {
            putInt(KEY_ENTITY_ID, entityId)
        }
    }

    fun getEntityId(): Int =
        sharedPreferences.getInt(KEY_ENTITY_ID, 0)

    fun setUserId(userId: Int) {
        sharedPreferences.edit {
            putInt(KEY_USER_ID, userId)
        }
    }

    fun getUserId(): Int =
        sharedPreferences.getInt(KEY_USER_ID, 0)

    fun saveIpAddress(ip: String) {
        sharedPreferences.edit {
            putString(KEY_IP, ip)
        }
    }


    fun getIpAddress(): String? =
        sharedPreferences.getString(KEY_IP, null)

    fun savePort(port: String) {
        sharedPreferences.edit {
            putString(KEY_PORT, port)
        }
    }


    fun getBaseUrl(): String {
        return sharedPreferences.getString(KEY_BASE_URL, "http://45.249.111.51/SmartPowerAPI/api/") ?: "http://45.249.111.51/SmartPowerAPI/api/"
    }

    fun getInstalledVersion(): String {
        return sharedPreferences.getString(APP_INSTALLED_VERSION, "0.0.1") ?: "0.0.1"
    }


    fun setCurrentAppVersion(name: String) {
        sharedPreferences.edit {
            putString(APP_CURRENT_VERSION, name)
        }
    }

    fun getCurrentAppVersion(): String? =
        sharedPreferences.getString(APP_CURRENT_VERSION, null)

    fun getPort(): String? =
        sharedPreferences.getString(KEY_PORT, null)

    fun setFullName(name: String) {
        sharedPreferences.edit {
            putString(KEY_FULL_NAME, name)
        }
    }

    fun getFullName(): String? =
        sharedPreferences.getString(KEY_FULL_NAME, null)

    fun setLocation(location: String) {
        sharedPreferences.edit {
            putString(KEY_LOCATION, location)
        }
    }

    fun getLocation(): String? = sharedPreferences.getString(KEY_LOCATION, null)

    fun saveSlipHeaderFooter(jsonString: String) {
        sharedPreferences.edit {
            putString(KEY_SLIP_HEADER_FOOTER, jsonString)
        }
    }

    fun getSlipHeaderFooter(): String? {
        return sharedPreferences.getString(KEY_SLIP_HEADER_FOOTER, null)
    }

    companion object {
        private const val TOKEN_MANAGER_NAME = "parking_token_pref"
        private const val TOKEN_KEY = "auth_token"
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ENTITY_ID = "entity_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IP = "ip_address"
        private const val KEY_PORT = "port_number"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_LOCATION = "location"
        private const val KEY_BASE_URL = "http://45.249.111.51/SmartPowerAPI/api/"
        private const val APP_INSTALLED_VERSION = "0.0.1"
        private const val APP_CURRENT_VERSION = ""
        private const val KEY_SLIP_HEADER_FOOTER = "slip_HF"



    }
}
