/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.targetpractice

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.Camera
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.raywenderlich.android.targetpractice.common.Maze
import com.raywenderlich.android.targetpractice.common.Quat
import com.raywenderlich.android.targetpractice.common.Quaternion
import com.raywenderlich.android.targetpractice.common.Vect
import com.raywenderlich.android.targetpractice.common.helpers.CameraPermissionHelper
import com.raywenderlich.android.targetpractice.common.helpers.DisplayRotationHelper
import com.raywenderlich.android.targetpractice.common.helpers.FullScreenHelper
import com.raywenderlich.android.targetpractice.common.helpers.SnackbarHelper
import com.raywenderlich.android.targetpractice.common.helpers.TrackingStateHelper
import com.raywenderlich.android.targetpractice.common.rendering.BackgroundRenderer
import com.raywenderlich.android.targetpractice.common.rendering.Mode
import com.raywenderlich.android.targetpractice.common.rendering.ObjectRenderer
import com.raywenderlich.android.targetpractice.common.rendering.PlaneAttachment
import com.raywenderlich.android.targetpractice.common.rendering.PlaneRenderer
import com.raywenderlich.android.targetpractice.common.rendering.PointCloudRenderer
import kotlinx.android.synthetic.main.activity_main.surfaceView
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random


class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer, SensorEventListener {

  private var obj = ""
  private val TAG: String = MainActivity::class.java.simpleName
  private val MOVE_DELAY_MILLIS = 1000L
  private var handler: Handler? = null

  private var installRequested = false
  private var humsterGen=true

  private var mode: Mode = Mode.VIKING

  private var session: Session? = null

  // Tap handling and UI.
  private lateinit var gestureDetector: GestureDetector
  private lateinit var trackingStateHelper: TrackingStateHelper
  private lateinit var displayRotationHelper: DisplayRotationHelper
  private val messageSnackbarHelper: SnackbarHelper = SnackbarHelper()

  private val backgroundRenderer: BackgroundRenderer = BackgroundRenderer()
  private val planeRenderer: PlaneRenderer = PlaneRenderer()
  private val pointCloudRenderer: PointCloudRenderer = PointCloudRenderer()

  // TODO: Declare ObjectRenderers and PlaneAttachments here
  private val vikingObject = ObjectRenderer()
  private val cannonObject = ObjectRenderer()
  private val targetObject = ObjectRenderer()

  private var vikingAttachment: PlaneAttachment? = null
  private var cannonAttachment: PlaneAttachment? = null
  private var targetAttachment: PlaneAttachment? = null

  // Temporary matrix allocated here to reduce number of allocations and taps for each frame.
  private val maxAllocationSize = 16
  private val anchorMatrix = FloatArray(maxAllocationSize)
  private val queuedSingleTaps = ArrayBlockingQueue<MotionEvent>(maxAllocationSize)

  //Maze
  private val maze = Maze()
  private val qt = Quat()


  private var sPose = Pose.IDENTITY

//  val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//  val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

  lateinit var sensorManager: SensorManager
  lateinit var sensor: Sensor

  private var plusXMove = false
  private var minusXMove = false
  private var plusZMove = false
  private var minusZMove = false

  private var xPoint = 0f
  private var zPoint = 0f

  private var sPos = Triple(0, 0, 0)

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    trackingStateHelper = TrackingStateHelper(this@MainActivity)
    displayRotationHelper = DisplayRotationHelper(this@MainActivity)

    installRequested = false

    setupTapDetector()
    setupSurfaceView()

    maze.mazeData = maze.generateMaze(21, 21, 34)
    maze.startPos = maze.findStartPos(maze.mazeData)
    maze.endPos = maze.findEndPos(maze.mazeData)
    maze.showMaze(maze.mazeData)
    Log.d("maze", "\n")
    Log.d("maze", "startPos ${maze.startPos}")
    Log.d("maze", "endPos ${maze.endPos}")
    maze.showMazeWithPos(maze.mazeData, maze.startPos, maze.endPos)
    obj = maze.generateMazeObj(maze.mazeData)
    //Log.d("maze", obj)

    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    sensorManager.registerListener(this, sensor, 10000000) //10000 = 10ms
  }

  fun onRadioButtonClicked(view: View) {
    when (view.id) {
      R.id.radioCannon -> mode = Mode.CANNON
      R.id.radioTarget -> mode = Mode.TARGET
      else -> mode = Mode.VIKING
    }
  }

  private fun setupSurfaceView() {
    // Set up renderer.
    surfaceView.preserveEGLContextOnPause = true
    surfaceView.setEGLContextClientVersion(2)
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, maxAllocationSize, 0)
    surfaceView.setRenderer(this)
    surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    surfaceView.setWillNotDraw(false)
    surfaceView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
  }

  private fun setupTapDetector() {
    gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

      override fun onSingleTapUp(e: MotionEvent): Boolean {
        onSingleTap(e)
        return true
      }

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

//      override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//        var result = false
//        try {
//          val diffY = e2.y - e1.y
//          val diffX = e2.x - e1.x
//          if (Math.abs(diffX) > Math.abs(diffY)) {
//            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//              if (diffX > 0) {
//                onSwipeRight()
//              } else {
//                onSwipeLeft()
//              }
//              result = true
//            }
//          } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//            if (diffY > 0) {
//              onSwipeBottom()
//            } else {
//              onSwipeTop()
//            }
//            result = true
//          }
//        } catch (exception: Exception) {
//          exception.printStackTrace()
//        }
//
//        return result
//      }

//      override fun onScroll(
//        e1: MotionEvent?,
//        e2: MotionEvent?,
//        distanceX: Float,
//        distanceY: Float
//      ): Boolean {
//
//        try {
//          val diffY = e2.y - e1.y
//          val diffX = e2.x - e1.x
//          if (Math.abs(diffX) > Math.abs(diffY)) {
//            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//              if (diffX > 0) {
//                onSwipeRight()
//              } else {
//                onSwipeLeft()
//              }
//              result = true
//            }
//          } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//            if (diffY > 0) {
//              onSwipeBottom()
//            } else {
//              onSwipeTop()
//            }
//            result = true
//          }
//        } catch (exception: Exception) {
//          exception.printStackTrace()
//        }
//        return super.onScroll(e1, e2, distanceX, distanceY)
//      }

//      open fun onSwipeRight() {
//        when (mode) {
//          //Mode.VIKING -> vikingAttachment!!.companion.angle += 1.1f
//          Mode.CANNON -> cannonAttachment!!.companion.angle += 1f
//         // Mode.TARGET -> targetAttachment!!.companion.angle += 1.1f
//        }
//      }
//
//      open fun onSwipeLeft() {
//        when (mode) {
//         // Mode.VIKING -> vikingAttachment!!.companion.angle -= 0.1f
//          Mode.CANNON -> cannonAttachment!!.companion.angle -= 1f
//          //Mode.TARGET -> targetAttachment!!.companion.angle -= 0.1f
//        }
//      }
//
//      open fun onSwipeTop() {
//        when (mode) {
////          Mode.VIKING -> Mode.VIKING.scaleFactor += 0.1f
////          Mode.CANNON -> Mode.CANNON.scaleFactor += 0.1f
////          Mode.TARGET -> Mode.TARGET.scaleFactor += 0.1f
//        }
//      }
//
//      open fun onSwipeBottom() {
//        when (mode) {
////          Mode.VIKING -> Mode.VIKING.scaleFactor *= 0.8f
////          Mode.CANNON -> Mode.CANNON.scaleFactor -= 0.1f
////          Mode.TARGET -> Mode.TARGET.scaleFactor -= 0.1f
//        }
//      }

      override fun onDown(e: MotionEvent): Boolean {
        return true
      }
      override fun onLongPress(e: MotionEvent) {
        onLongTap(e)
      }
    })
  }
  private fun onLongTap(e: MotionEvent) {
    when (mode) {
      Mode.VIKING -> vikingAttachment = null
      Mode.CANNON -> cannonAttachment = null
      Mode.TARGET -> targetAttachment = null
    }
  }

  private fun onSingleTap(e: MotionEvent) {
    // Queue tap if there is space. Tap is lost if queue is full.
    queuedSingleTaps.offer(e)
  }

  override fun onResume() {
    super.onResume()

    if (session == null) {
      if (!setupSession()) {
        return
      }
    }

    try {
      session?.resume()
    } catch (e: CameraNotAvailableException) {
      messageSnackbarHelper.showError(this@MainActivity, getString(R.string.camera_not_available))
      session = null
      return
    }

    surfaceView.onResume()
    displayRotationHelper.onResume()
  }

  private fun setupSession(): Boolean {
    var exception: Exception? = null
    var message: String? = null

    try {
      when (ArCoreApk.getInstance().requestInstall(this@MainActivity, !installRequested)) {
        InstallStatus.INSTALL_REQUESTED -> {
          installRequested = true
          return false
        }
        InstallStatus.INSTALLED -> {
        }
        else -> {
          message = getString(R.string.arcore_install_failed)
        }
      }

      // Requesting Camera Permission
      if (!CameraPermissionHelper.hasCameraPermission(this@MainActivity)) {
        CameraPermissionHelper.requestCameraPermission(this@MainActivity)
        return false
      }

      // Create the session.
      session = Session(this@MainActivity)

    } catch (e: UnavailableArcoreNotInstalledException) {
      message = getString(R.string.please_install_arcore)
      exception = e
    } catch (e: UnavailableUserDeclinedInstallationException) {
      message = getString(R.string.please_install_arcore)
      exception = e
    } catch (e: UnavailableApkTooOldException) {
      message = getString(R.string.please_update_arcore)
      exception = e
    } catch (e: UnavailableSdkTooOldException) {
      message = getString(R.string.please_update_app)
      exception = e
    } catch (e: UnavailableDeviceNotCompatibleException) {
      message = getString(R.string.arcore_not_supported)
      exception = e
    } catch (e: Exception) {
      message = getString(R.string.failed_to_create_session)
      exception = e
    }

    if (message != null) {
      messageSnackbarHelper.showError(this@MainActivity, message)
      Log.e(TAG, getString(R.string.failed_to_create_session), exception)
      return false
    }

    return true
  }

  override fun onPause() {
    super.onPause()

    if (session != null) {
      displayRotationHelper.onPause()
      surfaceView.onPause()
      session!!.pause()
    }
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      results: IntArray
  ) {
    if (!CameraPermissionHelper.hasCameraPermission(this@MainActivity)) {
      Toast.makeText(
          this@MainActivity,
          getString(R.string.camera_permission_needed),
          Toast.LENGTH_LONG
      ).show()

      // Permission denied with checking "Do not ask again".
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this@MainActivity)) {
        CameraPermissionHelper.launchPermissionSettings(this@MainActivity)
      }
      finish()
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)

    FullScreenHelper.setFullScreenOnWindowFocusChanged(this@MainActivity, hasFocus)
  }

  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(this@MainActivity)
      planeRenderer.createOnGlThread(this@MainActivity, getString(R.string.model_grid_png))
      pointCloudRenderer.createOnGlThread(this@MainActivity)

      //Log.d("maze", "getString(R.string.model_cannon_obj), ${getString(R.string.model_cannon_obj)}")
      //File(getString(R.string.model_cannon_obj)).writeText(obj)
      //val inputStream = getApplicationContext()?.assets?.open("models/test2.obj")
//      try {
//        val inputStream: InputStream = assets.open("models/test2.obj")
//        var text = obj
//        inputStream.
//
//        f.writeText(text)
//      } catch (e:Exception){
//        Log.d(TAG, e.toString())
//      }

//      val writer = PrintWriter("maze.obj")  // java.io.PrintWriter
//      writer.append(obj)
//      writer.close()

//      val fos = openFileOutput("maze.obj", MODE_PRIVATE)
//      fos.write(obj.toByteArray())
//      fos.close()
//
//      val fos1 = openFileOutput("maze.obj", MODE_PRIVATE)

      // TODO - set up the objects
      // 1
      //vikingObject.createOnGlThread(this@MainActivity, getString(R.string.model_viking_obj), getString(R.string.model_viking_png))
      //vikingObject.createOnGlThread(this@MainActivity, getString(R.string.model_test_obj), getString(R.string.model_target_png))
      vikingObject.createOnGlThreadFromStringObj(this@MainActivity, obj, getString(R.string.model_target_png))
      cannonObject.createOnGlThread(this@MainActivity, getString(R.string.model_cube_obj), getString(R.string.model_cannon_png))
      targetObject.createOnGlThread(this@MainActivity, getString(R.string.model_target_obj), getString(R.string.model_humster_png))

      // 2
      targetObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
      vikingObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
      cannonObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
    } catch (e: IOException) {
      Log.e(TAG, getString(R.string.failed_to_read_asset), e)
    }
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    displayRotationHelper.onSurfaceChanged(width, height)
    GLES20.glViewport(0, 0, width, height)
  }

  override fun onDrawFrame(gl: GL10?) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

    session?.let {
      // Notify ARCore session that the view size changed
      displayRotationHelper.updateSessionIfNeeded(it)

      try {
        it.setCameraTextureName(backgroundRenderer.textureId)

        val frame = it.update()
        val camera = frame.camera

        // Handle one tap per frame.
        handleTap(frame, camera)
        drawBackground(frame)

        // Keeps the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

        // If not tracking, don't draw 3D objects, show tracking failure reason instead.
        if (!isInTrackingState(camera)) return

        val projectionMatrix = computeProjectionMatrix(camera)
        val viewMatrix = computeViewMatrix(camera)
        val lightIntensity = computeLightIntensity(frame)

        visualizeTrackedPoints(frame, projectionMatrix, viewMatrix)
        checkPlaneDetected()
        visualizePlanes(camera, projectionMatrix)



        drawObject(
          vikingObject,
          vikingAttachment,
          Mode.VIKING.scaleFactor,
          projectionMatrix,
          viewMatrix,
          lightIntensity
        )

        drawObject(
          cannonObject,
          cannonAttachment,
          Mode.CANNON.scaleFactor,
          projectionMatrix,
          viewMatrix,
          lightIntensity,
          isMove = true
        )

        drawObject(
          targetObject,
          targetAttachment,
          Mode.TARGET.scaleFactor,
          projectionMatrix,
          viewMatrix,
          lightIntensity
        )
        // TODO: Call drawObject() for Viking, Cannon and Target here
      } catch (t: Throwable) {
        Log.e(TAG, getString(R.string.exception_on_opengl), t)
      }
    }
  }

  private fun isInTrackingState(camera: Camera): Boolean {
    if (camera.trackingState == TrackingState.PAUSED) {
      messageSnackbarHelper.showMessage(
          this@MainActivity, TrackingStateHelper.getTrackingFailureReasonString(camera)
      )
      return false
    }

    return true
  }

  fun isAllowPlusXMove(): Boolean {
    val x: Int = sPos.first + (xPoint / .05f + 0.025f).toInt()
    val z: Int = sPos.third + (zPoint / .05f + 0.025f).toInt()
    Log.d("cord", "x = $x, z = $z")
    return maze.mazeData[x+1][z] != 1
  }
  fun isAllowMinusXMove(): Boolean {
    val x: Int = sPos.first + (xPoint / .05f + 0.025f).toInt()
    val z: Int = sPos.third + (zPoint / .05f + 0.025f).toInt()
    Log.d("cord", "x = $x, z = $z")
    return maze.mazeData[x-1][z] != 1
  }
  fun isAllowPlusZMove(): Boolean {
    val x: Int = sPos.first + (xPoint / .05f + 0.025f).toInt()
    val z: Int = sPos.third + (zPoint / .05f + 0.025f).toInt()
    Log.d("cord", "x = $x, z = $z")
    return maze.mazeData[x][z+1] != 1
  }
  fun isAllowMinusZMove(): Boolean {
    val x: Int = sPos.first + (xPoint / .05f + 0.025f).toInt()
    val z: Int = sPos.third + (zPoint / .05f + 0.025f).toInt()
    Log.d("cord", "x = $x, z = $z")
    return maze.mazeData[x][z-1] != 1
  }
  fun generateFood(): Array<Int> {
    val x: Int = (0..9).random()
    val z: Int = (0..11).random()
    val arr=arrayOf(x,z)
    Log.d("cord", "x = $x, z = $z")
    return arr
  }

  private fun drawObject(
      objectRenderer: ObjectRenderer,
      planeAttachment: PlaneAttachment?,
      scaleFactor: Float,
      projectionMatrix: FloatArray,
      viewMatrix: FloatArray,
      lightIntensity: FloatArray,
      isMove: Boolean = false
  ) {
    //Log.d("maze", "drawObject")
    if (planeAttachment?.isTracking == true) {
      val speed = 0.01f
      if (isMove) {

        //qt.quat = Quaternion(vikingAttachment!!.pose.rotationQuaternion[0], vikingAttachment!!.pose.rotationQuaternion[1], vikingAttachment!!.pose.rotationQuaternion[2], vikingAttachment!!.pose.rotationQuaternion[3])
        if (plusXMove && isAllowPlusXMove()) {
          qt.tempVect = Vect(speed, 0f, 0f)
          qt.tempVect = qt.quatMultVect(qt.quat, qt.tempVect)
          qt.vector = Vect(planeAttachment.pose.translation[0]+qt.tempVect.x, planeAttachment.pose.translation[1]+qt.tempVect.y, planeAttachment.pose.translation[2]+qt.tempVect.z)
          //qt.vector = Vect(speed, 0f, 0f)
          //qt.vector = qt.quatMultVect(qt.quat, qt.vector)
          sPose = Pose(floatArrayOf(qt.vector.x, qt.vector.y, qt.vector.z), planeAttachment.pose.rotationQuaternion)
          cannonAttachment = addSessionAnchorFromAttachmentWithPose(cannonAttachment, sPose)
          //sPose.toMatrix(anchorMatrix, 0)
          xPoint += speed
        } else if (minusXMove && isAllowMinusXMove()) {
          qt.tempVect = Vect(speed, 0f, 0f)
          qt.tempVect = qt.quatMultVect(qt.quat, qt.tempVect)
          qt.vector = Vect(planeAttachment.pose.translation[0]-qt.tempVect.x, planeAttachment.pose.translation[1]-qt.tempVect.y, planeAttachment.pose.translation[2]-qt.tempVect.z)
          //qt.vector = qt.quatMultVect(qt.quat, qt.vector)
          //qt.vector = Vect(-speed, 0f, 0f)
          //qt.vector = qt.quatMultVect(qt.quat, qt.vector)
          sPose = Pose(floatArrayOf(qt.vector.x, qt.vector.y, qt.vector.z), planeAttachment.pose.rotationQuaternion)
          cannonAttachment = addSessionAnchorFromAttachmentWithPose(cannonAttachment, sPose)
          //sPose.toMatrix(anchorMatrix, 0)
          xPoint -= speed
        } else if (plusZMove && isAllowPlusZMove()) {
          qt.tempVect = Vect(0f, 0f, speed)
          qt.tempVect = qt.quatMultVect(qt.quat, qt.tempVect)
          qt.vector = Vect(planeAttachment.pose.translation[0]+qt.tempVect.x, planeAttachment.pose.translation[1]+qt.tempVect.y, planeAttachment.pose.translation[2]+qt.tempVect.z)
          //qt.vector = Vect(speed, 0f, 0f)
          //qt.vector = qt.quatMultVect(qt.quat, qt.vector)
          sPose = Pose(floatArrayOf(qt.vector.x, qt.vector.y, qt.vector.z), planeAttachment.pose.rotationQuaternion)
          cannonAttachment = addSessionAnchorFromAttachmentWithPose(cannonAttachment, sPose)
          //sPose.toMatrix(anchorMatrix, 0)
          zPoint += speed
        } else if (minusZMove && isAllowMinusZMove()) {
          qt.tempVect = Vect(0f, 0f, speed)
          qt.tempVect = qt.quatMultVect(qt.quat, qt.tempVect)
          qt.vector = Vect(planeAttachment.pose.translation[0]-qt.tempVect.x, planeAttachment.pose.translation[1]-qt.tempVect.y, planeAttachment.pose.translation[2]-qt.tempVect.z)
          //qt.vector = qt.quatMultVect(qt.quat, qt.vector)
          //qt.vector = Vect(-speed, 0f, 0f)
          //qt.vector = qt.quatMultVect(qt.quat, qt.vector)
          sPose = Pose(floatArrayOf(qt.vector.x, qt.vector.y, qt.vector.z), planeAttachment.pose.rotationQuaternion)
          cannonAttachment = addSessionAnchorFromAttachmentWithPose(cannonAttachment, sPose)
          //sPose.toMatrix(anchorMatrix, 0)
          zPoint -= speed
        }
        //sPose.toMatrix(anchorMatrix, 0)
        planeAttachment.pose.toMatrix(anchorMatrix, 0)
      } else {
        planeAttachment.pose.toMatrix(anchorMatrix, 0)
      }
      //Log.d("maze", "sensor ${sensor}")
      //Log.d("maze", "sensor ${sensor}")
//      Log.d("maze", "planeAttachment.pose ${planeAttachment.pose}")
//      Log.d("maze", "planeAttachment.pose.translation ${planeAttachment.pose.translation}")
//      Log.d("maze", "planeAttachment.pose.translation.size ${planeAttachment.pose.translation.size}")
//      Log.d("maze", "planeAttachment.pose.translation[0] ${planeAttachment.pose.translation[0]}")
//      Log.d("maze", "planeAttachment.pose.translation[1] ${planeAttachment.pose.translation[1]}")
//      Log.d("maze", "planeAttachment.pose.translation[2] ${planeAttachment.pose.translation[2]}")
//      Log.d("maze", "planeAttachment.pose.rotationQuaternion ${planeAttachment.pose.rotationQuaternion}")
//      Log.d("maze", "planeAttachment.pose.rotationQuaternion.size ${planeAttachment.pose.rotationQuaternion.size}")
//      Log.d("maze", "planeAttachment.pose.rotationQuaternion[0] ${planeAttachment.pose.rotationQuaternion[0]}")
//      Log.d("maze", "planeAttachment.pose.rotationQuaternion[1] ${planeAttachment.pose.rotationQuaternion[1]}")
//      Log.d("maze", "planeAttachment.pose.rotationQuaternion[2] ${planeAttachment.pose.rotationQuaternion[2]}")
//      Log.d("maze", "planeAttachment.pose.rotationQuaternion[3] ${planeAttachment.pose.rotationQuaternion[3]}")
//      Log.d("maze", "planeAttachment.pose.xAxis ${planeAttachment.pose.xAxis}")
//      Log.d("maze", "planeAttachment.pose.xAxis.size ${planeAttachment.pose.xAxis.size}")
      // Update and draw the model
      objectRenderer.updateModelMatrix(anchorMatrix, scaleFactor)
      objectRenderer.draw(viewMatrix, projectionMatrix, lightIntensity)
    }
  }


  private fun drawBackground(frame: Frame) {
    backgroundRenderer.draw(frame)
  }

  private fun computeProjectionMatrix(camera: Camera): FloatArray {
    val projectionMatrix = FloatArray(maxAllocationSize)
    camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

    return projectionMatrix
  }

  private fun computeViewMatrix(camera: Camera): FloatArray {
    val viewMatrix = FloatArray(maxAllocationSize)
    camera.getViewMatrix(viewMatrix, 0)

    return viewMatrix
  }

  /**
   * Compute lighting from average intensity of the image.
   */
  private fun computeLightIntensity(frame: Frame): FloatArray {
    val lightIntensity = FloatArray(4)
    frame.lightEstimate.getColorCorrection(lightIntensity, 0)

    return lightIntensity
  }


  /**
   * Visualizes tracked points.
   */
  private fun visualizeTrackedPoints(
      frame: Frame,
      projectionMatrix: FloatArray,
      viewMatrix: FloatArray
  ) {
    // Use try-with-resources to automatically release the point cloud.
    frame.acquirePointCloud().use { pointCloud ->
      pointCloudRenderer.update(pointCloud)
      pointCloudRenderer.draw(viewMatrix, projectionMatrix)
    }
  }

  /**
   *  Visualizes planes.
   */
  private fun visualizePlanes(camera: Camera, projectionMatrix: FloatArray) {
    planeRenderer.drawPlanes(
        session!!.getAllTrackables(Plane::class.java),
        camera.displayOrientedPose,
        projectionMatrix
    )
  }

  /**
   * Checks if any tracking plane exists then, hide the message UI, otherwise show searchingPlane message.
   */
  private fun checkPlaneDetected() {
    if (hasTrackingPlane()) {
      messageSnackbarHelper.hide(this@MainActivity)
    } else {
      messageSnackbarHelper.showMessage(
          this@MainActivity,
          getString(R.string.searching_for_surfaces)
      )
    }
  }

  /**
   * Checks if we detected at least one plane.
   */
  private fun hasTrackingPlane(): Boolean {
    val allPlanes = session!!.getAllTrackables(Plane::class.java)

    for (plane in allPlanes) {
      if (plane.trackingState == TrackingState.TRACKING) {
        return true
      }
    }

    return false
  }

  /**
   * Handle a single tap per frame
   */
  private fun handleTap(frame: Frame, camera: Camera) {
    val tap = queuedSingleTaps.poll()

    if (tap != null && camera.trackingState == TrackingState.TRACKING) {
      // Check if any plane was hit, and if it was hit inside the plane polygon
      for (hit in frame.hitTest(tap)) {
        val trackable = hit.trackable

        if ((trackable is Plane
                && trackable.isPoseInPolygon(hit.hitPose)
                && PlaneRenderer.calculateDistanceToPlane(hit.hitPose, camera.pose) > 0)
            || (trackable is Point
                && trackable.orientationMode
                == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)
        ) {
          when (mode) {
            Mode.VIKING -> {
              sPos = Triple(maze.startPos.first, 0, maze.startPos.second)
              xPoint = 0f
              zPoint = 0f
              vikingAttachment = addSessionAnchorFromAttachment(vikingAttachment, hit)
              qt.quat = Quaternion(vikingAttachment!!.pose.rotationQuaternion[0], vikingAttachment!!.pose.rotationQuaternion[1], vikingAttachment!!.pose.rotationQuaternion[2], vikingAttachment!!.pose.rotationQuaternion[3])
              //Log.d("maze", "qt.quat ${qt.quat}")
              qt.vector = Vect(.025f + maze.startPos.first * .05f, 0f, .025f + maze.startPos.second * .05f)
              //qt.vector = Vect(.025f + 3 * .05f, 0f, .025f + 3 * .05f)
              //Log.d("maze", "qt.vector ${qt.vector}")
              qt.vector = qt.quatMultVect(qt.quat, qt.vector)
              //Log.d("maze", "qt.vector after mult ${qt.vector}")
              //cannonAttachment = addSessionAnchorFromAttachment(cannonAttachment, hit)
              cannonAttachment = addSessionAnchorFromAttachment(cannonAttachment, hit, qt.vector)
              //cannonAttachment = addSessionAnchorFromAttachment(cannonAttachment, hit, Triple(maze.startPos.first * .1f, maze.startPos.second * .1f, 0f))
            }
            //Mode.VIKING -> cannonAttachment = addSessionAnchorFromAttachment(cannonAttachment, hit)
            Mode.TARGET -> targetAttachment = addSessionAnchorFromAttachment(targetAttachment, hit)
          }
          // TODO: Create an anchor if a plane or an oriented point was hit
          break
        }

      }
    }
  }

  // TODO: Add addSessionAnchorFromAttachment() function here
  private fun addSessionAnchorFromAttachment(
    previousAttachment: PlaneAttachment?,
    hit: HitResult,
    offset: Vect = Vect(0f, 0f, 0f)
  ): PlaneAttachment? {
    // 1
    previousAttachment?.anchor?.detach()

    // 2
    val plane = hit.trackable as Plane
    val tr = floatArrayOf(hit.hitPose.translation[0]+offset.x,
      hit.hitPose.translation[1]+offset.y,
      hit.hitPose.translation[2]+offset.z)
    //val rq = floatArrayOf(hit.hitPose.rotationQuaternion[0], rotation, hit.hitPose.rotationQuaternion[2], hit.hitPose.rotationQuaternion[3])
    val offsetPose = Pose(tr, hit.hitPose.rotationQuaternion)
    //val anchor = session!!.createAnchor(hit.hitPose)
    val anchor = session!!.createAnchor(offsetPose)

    // 3
    return PlaneAttachment(plane, anchor)
  }

  private fun addSessionAnchorFromAttachmentWithPose(
    previousAttachment: PlaneAttachment?,
    pose: Pose,
    offset: Vect = Vect(0f, 0f, 0f)
  ): PlaneAttachment? {
    // 1
    val plane = previousAttachment!!.plane as Plane

    previousAttachment.anchor.detach()
    // 2
//    val tr = floatArrayOf(hit.hitPose.translation[0]+offset.x,
//      hit.hitPose.translation[1]+offset.y,
//      hit.hitPose.translation[2]+offset.z)
//    //val rq = floatArrayOf(hit.hitPose.rotationQuaternion[0], rotation, hit.hitPose.rotationQuaternion[2], hit.hitPose.rotationQuaternion[3])
//    val offsetPose = Pose(tr, hit.hitPose.rotationQuaternion)
//    //val anchor = session!!.createAnchor(hit.hitPose)
    val anchor = session!!.createAnchor(pose)

    // 3
    return PlaneAttachment(plane, anchor)
  }

  override fun onSensorChanged(event: SensorEvent?) {
//    if (event!!.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
//      Log.d("maze", "event.values: ")
//      for (i in 0 until event.values.size) {
//        //Log.d("maze", "event.values[$i] ${event.values[i]}")
//        if (event.values[1] <= -0.1) {
//          minusXMove = false
//          plusXMove = true
//          Log.d("maze", "plusXMove")
//        } else if (event.values[1] >= 0.1) {
//          plusXMove = false
//          minusXMove = true
//          Log.d("maze", "minusXMove")
//        } else if (event.values[1] > -0.1 && event.values[1] < 0.1) {
//          minusXMove = false
//          plusXMove = false
//          Log.d("maze", "NO MOVE")
//        }
//
//        if (event.values[0] <= -0.1) {
//          minusZMove = false
//          plusZMove = true
//          Log.d("maze", "plusZMove")
//        } else if (event.values[0] >= 0.1) {
//          plusZMove = false
//          minusZMove = true
//          Log.d("maze", "minusZMove")
//        } else if (event.values[0] > -0.1 && event.values[0] < 0.1) {
//          minusZMove = false
//          plusZMove = false
//          Log.d("maze", "NO MOVE")
//        }
//      }
//    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    Log.d("maze", "onAccuracyChanged")
  }
}