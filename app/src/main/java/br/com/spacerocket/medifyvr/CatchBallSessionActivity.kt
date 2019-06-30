package br.com.spacerocket.medifyvr

import android.content.Intent
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.gson.Gson
import com.google.vr.sdk.audio.GvrAudioEngine
import com.google.vr.sdk.base.*
import java.io.IOException
import java.util.*
import javax.microedition.khronos.egl.EGLConfig

// Implementing the Runnable interface to implement threads.
class SimpleRunnable: Runnable {
    public override fun run() {
        println("${Thread.currentThread()} has run.")
    }
}

class CatchBallSessionActivity : GvrActivity(), GvrView.StereoRenderer  {

    var counter = 1

    inner class TargetThread: Thread() {
        public override fun run() {
            if (isLookingAtTarget()) {
                Log.d("LOOKING", "Estou olhando: " + counter)
                counter++
            }
        }
    }

    private val TAG = "HelloVrActivity"

    private val TARGET_MESH_COUNT = 3

    private val Z_NEAR = 0.01f
    private val Z_FAR = 10.0f

    // Convenience vector for extracting the position from a matrix via multiplication.
    private val POS_MATRIX_MULTIPLY_VEC = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
    private val FORWARD_VEC = floatArrayOf(0.0f, 0.0f, -1.0f, 1f)

    private val MIN_TARGET_DISTANCE = 3.0f
    private val MAX_TARGET_DISTANCE = 3.5f

    private val OBJECT_SOUND_FILE = "audio/HelloVR_Loop.ogg"
    private val SUCCESS_SOUND_FILE = "audio/HelloVR_Activation.ogg"

    private val FLOOR_HEIGHT = -2.0f

    private val ANGLE_LIMIT = 0.2f

    // The maximum yaw and pitch of the target object, in degrees. After hiding the target, its
    // yaw will be within [-MAX_YAW, MAX_YAW] and pitch will be within [-MAX_PITCH, MAX_PITCH].
    private val MAX_YAW = 100.0f
    private val MAX_PITCH = 25.0f

    private var targetLookingTimer: Long = 0
    private var points: Int = 0

    private val OBJECT_VERTEX_SHADER_CODE = arrayOf(
        "uniform mat4 u_MVP;",
        "attribute vec4 a_Position;",
        "attribute vec2 a_UV;",
        "varying vec2 v_UV;",
        "",
        "void main() {",
        "  v_UV = a_UV;",
        "  gl_Position = u_MVP * a_Position;",
        "}"
    )
    private val OBJECT_FRAGMENT_SHADER_CODE = arrayOf(
        "precision mediump float;",
        "varying vec2 v_UV;",
        "uniform sampler2D u_Texture;",
        "",
        "void main() {",
        "  // The y coordinate of this sample's textures is reversed compared to",
        "  // what OpenGL expects, so we invert the y coordinate.",
        "  gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));",
        "}"
    )

    private var objectProgram: Int = 0

    private var objectPositionParam: Int = 0
    private var objectUvParam: Int = 0
    private var objectModelViewProjectionParam: Int = 0

    private var targetDistance = MAX_TARGET_DISTANCE

    private var room: TexturedMesh? = null
    private var roomTex: Texture? = null
    private var targetObjectMeshes: ArrayList<TexturedMesh>? = null
    private var targetObjectNotSelectedTextures: ArrayList<Texture>? = null
    private var targetObjectSelectedTextures: ArrayList<Texture>? = null
    private var curTargetObject: Int = 0

    private var random: Random? = null

    private var targetPosition: FloatArray? = null
    private var camera: FloatArray? = null
    private var view: FloatArray? = null
    private var headView: FloatArray? = null
    private var modelViewProjection: FloatArray? = null
    private var modelView: FloatArray? = null

    private var modelTarget: FloatArray? = null
    private var modelRoom: FloatArray? = null

    private var tempPosition: FloatArray? = null
    private var headRotation: FloatArray? = null

    private var gvrAudioEngine: GvrAudioEngine? = null
    @Volatile
    private var sourceId = GvrAudioEngine.INVALID_ID
    @Volatile
    private var successSourceId = GvrAudioEngine.INVALID_ID

    private var MINIMAL_TIMER_TO_DISSEPEAR: Long = 250
    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeGvrView()

        val thread = TargetThread()
        thread.start()

        camera = FloatArray(16)
        view = FloatArray(16)
        modelViewProjection = FloatArray(16)
        modelView = FloatArray(16)
        // Target object first appears directly in front of user.
        targetPosition = floatArrayOf(0.0f, 0.0f, -MIN_TARGET_DISTANCE)
        tempPosition = FloatArray(4)
        headRotation = FloatArray(4)
        modelTarget = FloatArray(16)
        modelRoom = FloatArray(16)
        headView = FloatArray(16)

        // Initialize 3D audio engine.
        gvrAudioEngine = GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY)

        random = Random()

        val handle = Handler()
        //handle.postDelayed({ goToMain() }, 30000)
        val timerHandler = Handler()
        timerHandler.postDelayed({
           targetLookingTimer = 0
        }, 6000)

    }

    private fun goToMain() {
        val intentLogin = Intent(this, MainActivity::class.java)
        startActivity(intentLogin)
        finish()
    }

    fun initializeGvrView() {
        setContentView(R.layout.activity_catch_ball_session)
        val gvrView = findViewById<GvrView>(R.id.gvr_view)
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8)
        gvrView.setRenderer(this)
        gvrView.setTransitionViewEnabled(true)

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation()

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true)
        }

        setGvrView(gvrView)
    }

    public override fun onPause() {
        gvrAudioEngine?.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        gvrAudioEngine?.resume()
    }

    override fun onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        Log.i(TAG, "onSurfaceChanged")
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     *
     * OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    override fun onSurfaceCreated(config: EGLConfig) {
        Log.i(TAG, "onSurfaceCreated")
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE)

        objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position")
        objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV")
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP")

        Util.checkGlError("Object program params")

        Matrix.setIdentityM(modelRoom, 0)
        Matrix.translateM(modelRoom, 0, 0f, FLOOR_HEIGHT, 0f)

        // Avoid any delays during start-up due to decoding of sound files.
        Thread(
            Runnable {
                // Start spatial audio playback of OBJECT_SOUND_FILE at the model position. The
                // returned sourceId handle is stored and allows for repositioning the sound object
                // whenever the target position changes.
                gvrAudioEngine?.preloadSoundFile(OBJECT_SOUND_FILE)
                sourceId = gvrAudioEngine!!.createSoundObject(OBJECT_SOUND_FILE)
                gvrAudioEngine?.setSoundObjectPosition(
                    sourceId, targetPosition!![0], targetPosition!![1], targetPosition!![2]
                )
                gvrAudioEngine?.playSound(sourceId, true /* looped playback */)
                // Preload an unspatialized sound to be played on a successful trigger on the
                // target.
                gvrAudioEngine?.preloadSoundFile(SUCCESS_SOUND_FILE)
            })
            .start()

        updateTargetPosition()

        Util.checkGlError("onSurfaceCreated")

        try {
            room = TexturedMesh(this, "CubeRoom.obj", objectPositionParam, objectUvParam)
            roomTex = Texture(this, "CubeRoom_BakedDiffuse.png")
            targetObjectMeshes = ArrayList()
            targetObjectNotSelectedTextures = ArrayList()
            targetObjectSelectedTextures = ArrayList()
            targetObjectMeshes?.add(
                TexturedMesh(this, "Icosahedron.obj", objectPositionParam, objectUvParam)
            )
            targetObjectNotSelectedTextures?.add(Texture(this, "Icosahedron_Blue_BakedDiffuse.png"))
            targetObjectSelectedTextures?.add(Texture(this, "Icosahedron_Pink_BakedDiffuse.png"))
            targetObjectMeshes?.add(
                TexturedMesh(this, "QuadSphere.obj", objectPositionParam, objectUvParam)
            )
            targetObjectNotSelectedTextures?.add(Texture(this, "QuadSphere_Blue_BakedDiffuse.png"))
            targetObjectSelectedTextures?.add(Texture(this, "QuadSphere_Pink_BakedDiffuse.png"))
            targetObjectMeshes?.add(
                TexturedMesh(this, "TriSphere.obj", objectPositionParam, objectUvParam)
            )
            targetObjectNotSelectedTextures?.add(Texture(this, "TriSphere_Blue_BakedDiffuse.png"))
            targetObjectSelectedTextures?.add(Texture(this, "TriSphere_Pink_BakedDiffuse.png"))
        } catch (e: IOException) {
            Log.e(TAG, "Unable to initialize objects", e)
        }

        curTargetObject = random!!.nextInt(TARGET_MESH_COUNT)
    }

    /** Updates the target object position.  */
    private fun updateTargetPosition() {
        Matrix.setIdentityM(modelTarget, 0)
        Matrix.translateM(modelTarget, 0, targetPosition!![0], targetPosition!![1], targetPosition!![2])

        // Update the sound location to match it with the new target position.
        if (sourceId != GvrAudioEngine.INVALID_ID) {
            gvrAudioEngine?.setSoundObjectPosition(
                sourceId, targetPosition!![0], targetPosition!![1], targetPosition!![2]
            )
        }
        Util.checkGlError("updateTargetPosition")
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    override fun onNewFrame(headTransform: HeadTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)

        headTransform.getHeadView(headView, 0)

        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0)
        gvrAudioEngine?.setHeadRotation(
            headRotation!![0], headRotation!![1], headRotation!![2], headRotation!![3]
        )
        // Regular update call to GVR audio engine.
        gvrAudioEngine?.update()

        Util.checkGlError("onNewFrame")
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    override fun onDrawEye(eye: Eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // The clear color doesn't matter here because it's completely obscured by
        // the room. However, the color buffer is still cleared because it may
        // improve performance.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.eyeView, 0, camera, 0)

        // Build the ModelView and ModelViewProjection matrices
        // for calculating the position of the target object.
        val perspective = eye.getPerspective(Z_NEAR, Z_FAR)

        Matrix.multiplyMM(modelView, 0, view, 0, modelTarget, 0)
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0)
        drawTarget()

        // Set modelView for the room, so it's drawn in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, modelRoom, 0)
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0)
        drawRoom()
    }

    override fun onFinishFrame(viewport: Viewport) {}

    /** Draw the target object.  */
    fun drawTarget() {
        GLES20.glUseProgram(objectProgram)
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0)
        if (isLookingAtTarget()) {
            if (targetLookingTimer > -1) {
                targetLookingTimer++
                Log.d("COUNT TIMER", "cont: "+targetLookingTimer)
                if (targetLookingTimer == MINIMAL_TIMER_TO_DISSEPEAR) {
                    points++
                    hideTarget()
                    targetLookingTimer = 0
                    if (points == TARGET_MESH_COUNT) {
                       var intentNew = Intent(this, ResultActivity::class.java)
                       val patientString = intent.getStringExtra("PATIENT")
                       // val patient = Gson().fromJson<Paciente>(patientString, Paciente::class.java)
                        //intentNew.putExtra("PATIENT_NAME", patient.nome)
                        intentNew.putExtra("RESULT", points)
                        startActivity(intentNew)
                        finish()
                    }
                }
            }

            targetObjectSelectedTextures?.get(curTargetObject)?.bind()
        } else {
            targetLookingTimer = 0
            targetObjectNotSelectedTextures?.get(curTargetObject)?.bind()
        }
        targetObjectMeshes?.get(curTargetObject)?.draw()
        Util.checkGlError("drawTarget")
    }

    /** Draw the room.  */
    fun drawRoom() {
        GLES20.glUseProgram(objectProgram)
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0)
        roomTex?.bind()
        room?.draw()
        Util.checkGlError("drawRoom")
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    override fun onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger")

        if (isLookingAtTarget()) {
            successSourceId = gvrAudioEngine!!.createStereoSound(SUCCESS_SOUND_FILE)
            gvrAudioEngine?.playSound(successSourceId, false /* looping disabled */)
            hideTarget()
        }
    }

    /** Find a new random position for the target object.  */
    private fun hideTarget() {
        val rotationMatrix = FloatArray(16)
        val posVec = FloatArray(4)

        // Matrix.setRotateM takes the angle in degrees, but Math.tan takes the angle in radians, so
        // yaw is in degrees and pitch is in radians.
        val yawDegrees = (random!!.nextFloat() - 0.5f) * 2.0f * MAX_YAW
        val pitchRadians = Math.toRadians(((random!!.nextFloat() - 0.5f) * 2.0f * MAX_PITCH).toDouble()).toFloat()

        Matrix.setRotateM(rotationMatrix, 0, yawDegrees, 0.0f, 1.0f, 0.0f)
        targetDistance = random!!.nextFloat() * (MAX_TARGET_DISTANCE - MIN_TARGET_DISTANCE) + MIN_TARGET_DISTANCE
        targetPosition = floatArrayOf(0.0f, 0.0f, -targetDistance)
        Matrix.setIdentityM(modelTarget, 0)
        Matrix.translateM(modelTarget, 0, targetPosition!![0], targetPosition!![1], targetPosition!![2])
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelTarget, 12)

        targetPosition!![0] = posVec[0]
        targetPosition!![1] = Math.tan(pitchRadians.toDouble()).toFloat() * targetDistance
        targetPosition!![2] = posVec[2]

        updateTargetPosition()
        curTargetObject = random!!.nextInt(TARGET_MESH_COUNT)
    }

    /**
     * Check if user is looking at the target object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the target object.
     */
    private fun isLookingAtTarget(): Boolean {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelTarget, 0)
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0)

        val angle = Util.angleBetweenVectors(tempPosition, FORWARD_VEC)
        return angle < ANGLE_LIMIT
    }
}

