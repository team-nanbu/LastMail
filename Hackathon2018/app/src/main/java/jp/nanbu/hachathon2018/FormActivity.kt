package jp.nanbu.hachathon2018

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityHandler
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.models.nosql.MessagesDO
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.s3.AmazonS3Client
import jp.nanbu.hachathon2018.utils.DateUtils
import jp.nanbu.hachathon2018.utils.FileUtils
import jp.nanbu.hachathon2018.utils.PrefUtils
import kotlinx.android.synthetic.main.activity_form.*
import java.io.File
import java.lang.Exception
import kotlin.concurrent.thread

class FormActivity : AppCompatActivity() {

    private var credentialsProvider: AWSCredentialsProvider? = null
    private var awsConfiguration: AWSConfiguration? = null

    private var uniqueId: String? = null

    private val pref = PrefUtils(this)

    private var dynamoDBMapper: DynamoDBMapper? = null

    companion object {
        private val TAG = FormActivity::class.java.simpleName
        private const val REQUEST_CAMERA = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.title_activity_form)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        awsInitialize()
        dynamoDBinitialize()

        videoBootButton.setOnClickListener { bootCamera() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_save -> {
                saveWillData()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * カメラ起動
     */
    private fun bootCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CAMERA -> {
                val uri = data?.data ?: return // 保存したファイルのURIを取得 content://...
                val file = FileUtils.getVideoFileFromUri(this, uri) ?: return // URIをFILEのスキームに変換
                uploadWithTransferUtility(file)
            }

            else -> {
                // do nothing
            }
        }
    }

    /**
     * AWS S3にファイルをアップロード
     */
    private fun uploadWithTransferUtility(file: File?) {
        val userId = IdentityManager.getDefaultIdentityManager().cachedUserID.replace(":", "")
        if (uniqueId == null) uniqueId = userId + "_" + DateUtils.getTimeStamp()
        Log.d(TAG, uniqueId)

        val transferUtility = TransferUtility.builder()
                .context(this@FormActivity.applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))
                .build()

        val uploadObserver = transferUtility.upload("uploads/movies/$uniqueId.mp4", file)

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
                    // 完了時Videoボタン非表示、送信済みメッセージ表示
                    videoBootButton.visibility = View.GONE
                    shootMessageView.visibility = View.VISIBLE
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

    /**
     * AWS初期化
     */
    private fun awsInitialize() {
        AWSMobileClient.getInstance().initialize(this) {
            credentialsProvider = AWSMobileClient.getInstance().credentialsProvider
            awsConfiguration = AWSMobileClient.getInstance().configuration

            IdentityManager.getDefaultIdentityManager().getUserID(object : IdentityHandler {
                override fun handleError(exception: Exception?) {
                    Log.e(TAG, "Retrieving identity: ${exception?.message}")
                }

                override fun onIdentityId(identityId: String?) {
                    Log.d(TAG, "Identity = $identityId")
                }
            })
        }.execute()
    }

    /**
     * DynamoDBを初期化
     */
    private fun dynamoDBinitialize() {
        val client = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(client)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()
    }

    /**
     * 遺言送信
     */
    private fun saveWillData() {
        uniqueId ?: return
        val messagesDo = MessagesDO().apply {
            this.id = uniqueId
            this.userId = IdentityManager.getDefaultIdentityManager().cachedUserID.replace(":", "")
            this.message = willInputView.text.toString()
            this.movie = "$uniqueId.mp4"
            this.receiver = receiverInputView.text.toString()
            this.sender = pref.getPrefString(PrefUtils.SENDER_MAIL_ADDRESS, "sender@mail.com")
            this.senderName = pref.getPrefString(PrefUtils.SENDER_NAME, "senderName")
        }

        thread(start = true) {
            dynamoDBMapper?.save(messagesDo)
            finish()
        }
    }

}
