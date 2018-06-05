package jp.nanbu.hachathon2018

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import jp.nanbu.hachathon2018.utils.PrefUtils
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {

    private val pref = PrefUtils(this)

    companion object {
        private val TAG: String = LoginActivity::class.java.simpleName
        private const val REQUEST_CAMERA = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AWSMobileClient.getInstance().initialize(this) {
            Log.d(TAG, "AWSMobileClient is initialized")
        }.execute()

        registerButton.setOnClickListener {
            save()
        }

        shotPortraitButton.setOnClickListener {
            bootCamera()
        }
    }

    private fun save() {
        val senderName = senderName.text.toString()
        val senderMailAddress = senderMailAddress.text.toString()
        pref.put(PrefUtils.SENDER_NAME, senderName)
        pref.put(PrefUtils.SENDER_MAIL_ADDRESS, senderMailAddress)

        startListActivity()
    }

    private fun startListActivity() {
        val intent = Intent(applicationContext, ListActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * カメラ起動
     */
    private fun bootCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CAMERA -> {
                if (data != null && data.extras != null) {
                    val bitmap = data.extras.get("data") as Bitmap
                    portraitImage.setImageBitmap(bitmap)
                    saveBitmapOnFile(bitmap) { saveFile ->
                        uploadWithTransferUtility(saveFile)
                    }
                }
            }
        }
    }


    private fun saveBitmapOnFile(bitmap: Bitmap, callback: (File) -> Unit) {
        val userId = IdentityManager.getDefaultIdentityManager().cachedUserID.replace(":", "")
        val file = File(Environment.getExternalStorageDirectory(), "$userId.jpg")
        thread {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                callback(file)
            }
        }
    }

    /**
     * AWS S3にファイルをアップロード
     */
    private fun uploadWithTransferUtility(file: File?) {

        val transferUtility = TransferUtility.builder()
                .context(this@LoginActivity.applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))
                .build()

        val uploadObserver = transferUtility.upload("uploads/yeah/${file?.name}", file)

        // Attach a listener to the observer
        uploadObserver.setTransferListener(transferListener)

        // If you prefer to long-poll for updates
        if (uploadObserver.state == TransferState.COMPLETED) {

        }
    }

    /**
     * ファイル送信リスナー
     */
    private val transferListener = object : TransferListener {
        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            Log.d(TAG, "onProgressChanged")
        }

        override fun onStateChanged(id: Int, state: TransferState?) {
            Log.d(TAG, "onStateChanged")

            when (state) {
                TransferState.COMPLETED -> {
                    Log.d(TAG, "onComplete")
                }
                else -> {
                    /* Anything else */
                }
            }

        }

        override fun onError(id: Int, ex: java.lang.Exception?) {
            // 送信失敗メッセージ表示
            ex?.printStackTrace()
        }
    }

}
