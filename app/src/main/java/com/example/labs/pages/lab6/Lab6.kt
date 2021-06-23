package com.example.labs.pages.lab6

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.labs.R
import com.example.labs.pages.lab6.Crypto.AES256.cipher
import com.example.labs.pages.lab6.Crypto.ChCrypto
import com.google.android.material.textfield.TextInputEditText
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

class Lab6 : Fragment() {
    private lateinit var lab6ViewModel: Lab6ViewModel

    private lateinit var contentResolver: ContentResolver
    private var _src: Uri? = null
    private var _dest: Uri? = null

    private lateinit var keyInput: TextInputEditText
    private lateinit var resultTV: TextView


    private var mAction = ACTIONS.ENCRYPT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        lab6ViewModel = ViewModelProvider(this).get(Lab6ViewModel::class.java)

        val root = inflater.inflate(R.layout.lab6, container, false)

        contentResolver = requireContext().contentResolver

        val decryptBtn: Button = root.findViewById(R.id.decrypt)
        val encryptBtn: Button = root.findViewById(R.id.encrypt)

        keyInput = root.findViewById(R.id.key)


        encryptBtn.setOnClickListener {
            if (!checkPermissions()) {
                return@setOnClickListener
            }
            openFileFor(ACTIONS.ENCRYPT, PICK_SRC_TXT_FILE)
        }

        decryptBtn.setOnClickListener {
            if (!checkPermissions()) {
                return@setOnClickListener
            }
            openFileFor(ACTIONS.DECRYPT, PICK_SRC_TXT_FILE)
        }

        return root
    }

    private fun getKey(): String = keyGen(keyInput.text.toString())

    private fun keyGen(key: String): String = MD5(key)

    fun MD5(string: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(string.toByteArray()))
            .toString(16)
            .padStart(32, '0')
    }


    fun encrypt(key: String, text: String): String = ChCrypto.aesEncrypt(text, key)

    fun decrypt(key: String, text: String): String = ChCrypto.aesDecrypt(text, key)

    @ExperimentalStdlibApi
    fun cryptFile(action: String){
        when(action){
            ACTIONS.ENCRYPT -> {
                encrpytFile(_src!!, _dest!!, getKey())
            }
            ACTIONS.DECRYPT -> {
                decryptFile(_src!!, _dest!!, getKey())
            }
        }
    }

    @ExperimentalStdlibApi
    fun encrpytFile(src: Uri, dest: Uri, key: String) {
        try {
            contentResolver
                .openInputStream(src)
                ?.use { inputStream ->

                    val bfSize = 128
                    var bytes = 0

                    contentResolver
                        .openFileDescriptor(dest, "w")
                        ?.use { parcelFileDescriptor ->
                            FileOutputStream(parcelFileDescriptor
                                .fileDescriptor)
                                .use {
                                    while (bytes != -1) {
                                        val buffer = ByteArray(bfSize)
                                        bytes = inputStream.read(buffer)
                                        val aesCipher = cipher(
                                            Cipher.ENCRYPT_MODE,
                                            key
                                        )
                                        Log.d("bytes", bytes.toString())
                                        val byteCipherText: ByteArray
                                                = if(bytes != -1)
                                            aesCipher.update(buffer)
                                        else
                                            aesCipher.doFinal(buffer)

                                        it.write(byteCipherText)


                                    }
                                }
                        }
                }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    @ExperimentalStdlibApi
    fun decryptFile(src: Uri, dest: Uri, key: String){
        try {
            contentResolver
                .openInputStream(src)
                ?.use { inputStream ->
                    val bfSize = 128
                    var bytes = 0

                    contentResolver
                        .openFileDescriptor(dest, "w")
                        ?.use { parcelFileDescriptor ->
                            FileOutputStream(parcelFileDescriptor
                                .fileDescriptor)
                                .use {
                                    while (bytes != -1) {

                                        val buffer = ByteArray(bfSize)
                                        bytes = inputStream
                                            .read(buffer)
                                        val aesCipher = cipher(
                                            Cipher.DECRYPT_MODE,
                                            key
                                        )
                                        val byteCipherText: ByteArray
                                        = if(bytes != -1)
                                            aesCipher.update(buffer)
                                        else
                                          aesCipher.doFinal(buffer)
                                        it.write(byteCipherText)

                                    }
                                }
                        }
                }
        }

        catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        catch (e: IllegalBlockSizeException){
            e.printStackTrace()
        }

    }

    private fun openFileFor(action: String, fileType: Int) {
        val intentAction =
            if (fileType== PICK_SRC_TXT_FILE)
                Intent.ACTION_OPEN_DOCUMENT
            else
                Intent.ACTION_CREATE_DOCUMENT

        val intent = Intent(intentAction).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = TXT_MIME_TYPE
        }
        mAction = action
        startActivityForResult(intent, fileType)
    }


    fun permissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions(): Boolean {
        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        return permissionsGranted()
    }

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_SRC_TXT_FILE -> {
                    data?.data?.let {
                        _src = it
                        Log.d("FILE", it.toString())

                        openFileFor(mAction, PICK_DEST_TXT_FILE)
                    }
                }
                PICK_DEST_TXT_FILE -> {
                    data?.data?.let {
                        _dest = it
                        Log.d("FILE", it.toString())

                        cryptFile(mAction)
                    }
                }
            }
        }
    }


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val REQUEST_CODE_PERMISSIONS = 10

        private const val TXT_MIME_TYPE = "text/plain"

        private const val PICK_SRC_TXT_FILE = 777
        private const val PICK_DEST_TXT_FILE = 666

        object ACTIONS {
            const val ENCRYPT = "encrypt"
            const val DECRYPT = "decrypt"
        }
    }

}
