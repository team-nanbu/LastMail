package jp.nanbu.hachathon2018

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.MediaController
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import jp.nanbu.hachathon2018.utils.DateUtils
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File
import java.lang.Exception


class VideoActivity : AppCompatActivity() {

    companion object {
        const val MEDIA_KEY = "media_key"
        private const val TAG = "VideoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        supportActionBar?.hide()

        val mediaKey = intent.getStringExtra(MEDIA_KEY)
        videoView.setMediaController(MediaController(this))
        downloadWithTransferUtility(mediaKey) // 動画をS3からダウンロード後再生
    }

    private fun playVideoUrl(file: File) {
        Log.d(TAG, "playVideoUrl: ${file.path}")
        videoView.setVideoPath(file.path)
        videoView.setOnPreparedListener {
            videoView.holder.setSizeFromLayout()
            videoView.start()
        }
    }

    private fun downloadWithTransferUtility(moviePath: String) {
        val localVideoFile = File(Environment.getExternalStorageDirectory(), DateUtils.getTimeStamp() + ".mp4")
        val i = TransferUtility.builder()
                .context(this@VideoActivity.applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))
                .build()

        Log.d(TAG, "moviePath: $moviePath")

        val downloadObserver = i.download("uploads/movies/$moviePath", localVideoFile)
        downloadObserver.setTransferListener(object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

            }

            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED) {
                    // 動画再生
                    Log.d(TAG, "onStateChange: COMPLETED")
                    playVideoUrl(localVideoFile)
                }
            }

            override fun onError(id: Int, ex: Exception?) {

            }
        })
        if (downloadObserver.state == TransferState.COMPLETED) {
            // 動画再生
            playVideoUrl(localVideoFile)
        }
    }
}
