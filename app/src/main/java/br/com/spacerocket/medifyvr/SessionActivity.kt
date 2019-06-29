package br.com.spacerocket.medifyvr

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.vr.sdk.widgets.video.VrVideoView
import kotlinx.android.synthetic.main.activity_session.*
import java.io.IOException

class SessionActivity : AppCompatActivity() {

    /**
     * Preserve the video's state when rotating the phone.
     */
    private val STATE_IS_PAUSED = "isPaused"
    private val STATE_PROGRESS_TIME = "progressTime"
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during [.onRestoreInstanceState] rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private val STATE_VIDEO_DURATION = "videoDuration"

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    val LOAD_VIDEO_STATUS_UNKNOWN = 0
    val LOAD_VIDEO_STATUS_SUCCESS = 1
    val LOAD_VIDEO_STATUS_ERROR = 2

    private var loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN

    /** Tracks the file to be loaded across the lifetime of this app.  */
    private var fileUri: Uri? = null

    /** Configuration information for the video.  */
    private val videoOptions = VrVideoView.Options()

    private var backgroundVideoLoaderTask: VideoLoaderTask? = null


    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private var seekBar: SeekBar? = null
    private var statusText: TextView? = null

    private var volumeToggle: ImageButton? = null
    private var isMuted: Boolean = false

    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * [VrVideoView.pauseVideo] after loading the video.
     */
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        handleIntent(intent)
        //video_view.setEventListener(ActivityEventListener())
    }

    override fun onNewIntent(intent: Intent) {
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent)
        // Load the new video.
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW == intent.action) {

            fileUri = intent.data
            if (fileUri == null) {

            } else {

            }

            videoOptions.inputFormat = intent.getIntExtra("inputFormat", VrVideoView.Options.FORMAT_DEFAULT)
            videoOptions.inputType = intent.getIntExtra("inputType", VrVideoView.Options.TYPE_MONO)
        } else {

            fileUri = null
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundVideoLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundVideoLoaderTask?.cancel(true)
        }
        backgroundVideoLoaderTask = VideoLoaderTask()
        backgroundVideoLoaderTask?.execute(Pair.create(fileUri, videoOptions))
    }

    /**
     * Helper class to manage threading.
     */
    internal inner class VideoLoaderTask : AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean>() {
        override fun doInBackground(vararg fileInformation: Pair<Uri, VrVideoView.Options>): Boolean? {
            try {
                if (fileInformation == null || fileInformation.size < 1
                    || fileInformation[0] == null || fileInformation[0].first == null
                ) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.
                    val options = VrVideoView.Options()
                    options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER
                    video_view.loadVideoFromAsset("dizzi.mp4", options)
                } else {
                    video_view.loadVideo(fileInformation[0].first, fileInformation[0].second)
                }
            } catch (e: IOException) {
                // An error here is normally due to being unable to locate the file.
                loadVideoStatus = LOAD_VIDEO_STATUS_ERROR
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                video_view.post {
                    Toast
                        .makeText(this@SessionActivity, "Error opening file. ", Toast.LENGTH_LONG)
                        .show()
                }
            }

            return true
        }
    }
}
