package jp.nanbu.hachathon2018

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ListView
import com.amazonaws.mobile.auth.core.IdentityHandler
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.models.nosql.MessagesDO
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import kotlinx.android.synthetic.main.activity_list.*
import java.lang.Exception
import kotlin.concurrent.thread

class ListActivity : AppCompatActivity() {

    companion object {
        private val TAG = ListActivity::class.java.simpleName
        private const val REQUEST_FORM = 1000
    }

    private var dynamoDBMapper: DynamoDBMapper? = null
    private var userId: String? = null
    private lateinit var receiverListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)
        setViews()

        dynamoDBinitialize()
        getAwsUserId()

        fab.setOnClickListener { view ->
            val intent = Intent(this, FormActivity::class.java)
            startActivityForResult(intent, REQUEST_FORM)
        }

        setListView()
    }

    private var messageList: List<MessagesDO> = emptyList()

    private fun setListView() {
        val userId = userId ?: return
        queryReceiverList(userId) {
            it?.let {
                messageList = it
                val adapter = MessageListAdapter(this, R.layout.message_list_layout, it)
                runOnUiThread {
                    receiverListView.adapter = adapter
                    receiverListView.setOnItemClickListener { parent, view, position, id ->
                        val i = Intent(applicationContext, VideoActivity::class.java)
                        i.putExtra(VideoActivity.MEDIA_KEY, it[position].movie)
                        startActivity(i)
                    }
                }
            }
        }
    }

    private fun setViews() {
        receiverListView = content.findViewById(R.id.receiverListView)
    }

    private fun getAwsUserId() {
        userId = IdentityManager.getDefaultIdentityManager().cachedUserID?.replace(":", "")
        if (userId == null) awsInitialize()
    }

    /**
     * AWS初期化
     */
    private fun awsInitialize() {
        AWSMobileClient.getInstance().initialize(this) {

            IdentityManager.getDefaultIdentityManager().getUserID(object : IdentityHandler {
                override fun handleError(exception: Exception?) {
                    Log.e(TAG, "Retrieving identity: ${exception?.message}")
                }

                override fun onIdentityId(identityId: String?) {
                    Log.d(TAG, "Identity = $identityId")
                    userId = identityId?.replace(":", "")
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

    private fun queryReceiverList(userId: String, callback: (List<MessagesDO>?) -> Unit) {
        thread(start = true) {
            val eav = HashMap<String, AttributeValue>()
            eav[":v1"] = AttributeValue().withS(userId)

            val scanExpression = DynamoDBScanExpression()
                    .withFilterExpression("begins_with(id,:v1)")
                    .withExpressionAttributeValues(eav)

            val message = dynamoDBMapper?.scan(MessagesDO::class.java, scanExpression)

            callback(message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_FORM -> setListView()
        }
    }
}
