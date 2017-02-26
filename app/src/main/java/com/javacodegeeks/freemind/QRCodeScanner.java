package com.javacodegeeks.freemind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javacodegeeks.freemind.ImageTargets.ImageTargetRenderer;
import com.javacodegeeks.freemind.ui.SampleAppMenu.SampleAppMenu;
import com.javacodegeeks.freemind.ui.SampleAppMenu.SampleAppMenuGroup;
import com.javacodegeeks.freemind.ui.SampleAppMenu.SampleAppMenuInterface;
import com.javacodegeeks.freemind.utils.LoadingDialogHandler;
import com.javacodegeeks.freemind.utils.SampleApplication3DModel;
import com.javacodegeeks.freemind.utils.SampleApplicationGLView;
import com.javacodegeeks.freemind.utils.Teapot;
import com.javacodegeeks.freemind.utils.Texture;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class QRCodeScanner extends Activity implements SampleApplicationControl,
		SampleAppMenuInterface {
public class QRCodeScanner extends Activity {
	/** Called when the activity is first created. */

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private boolean isRepeatedMode;
	private static final String LOGTAG = "ImageTargets";
	private DatabaseReference mDatabase;
	private DatabaseReference boothRef;

	private SampleApplicationSession vuforiaAppSession;
	private QRCodeScanner mActivity;
	private SampleAppRenderer mSampleAppRenderer;

	private Vector<Texture> mTextures;

	private int shaderProgramID;
	private int vertexHandle;
	private int textureCoordHandle;
	private int mvpMatrixHandle;
	private int texSampler2DHandle;

	private DataSet mCurrentDataset;
	private int mCurrentDatasetSelectionIndex = 0;
	private int mStartDatasetsIndex = 0;
	private int mDatasetsNumber = 0;
	private ArrayList<String> mDatasetStrings = new ArrayList<String>();

	// Our OpenGL view:
	private SampleApplicationGLView mGlView;

	// Our renderer:
	private ImageTargetRenderer mRenderer;

	private GestureDetector mGestureDetector;

	private boolean mSwitchDatasetAsap = false;
	private boolean mFlash = false;
	private boolean mContAutofocus = false;
	private boolean mExtendedTracking = false;

	private View mFlashOptionView;

	private RelativeLayout mUILayout;

	private SampleAppMenu mSampleAppMenu;

	LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

	// Alert Dialog used to display SDK errors
	private AlertDialog mErrorDialog;

	boolean mIsDroidDevice = false;

	private Teapot mTeapot;

	private float kBuildingScale = 0.012f;
	private SampleApplication3DModel mBuildingsModel;

	private boolean mIsActive = false;
	private boolean mModelIsLoaded = false;

	private static final float OBJECT_SCALE_FLOAT = 0.003f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//boothTextView = (TextView) findViewById(R.id.);

	}

	@Override
	public boolean menuProcess(int command) {
		return false;
	}

	// Process Single Tap event to trigger autofocus
	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener
	{
		// Used to set autofocus one second after a manual focus is triggered
		private final Handler autofocusHandler = new Handler();


		@Override
		public boolean onDown(MotionEvent e)
		{
			return true;
		}


		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			// Generates a Handler to trigger autofocus
			// after 1 second
			autofocusHandler.postDelayed(new Runnable()
			{
				public void run()
				{
					boolean result = CameraDevice.getInstance().setFocusMode(
							CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

					if (!result)
						Log.e("SingleTapUp", "Unable to trigger focus");
				}
			}, 1000L);

			return true;
		}
	}

	// We want to load specific textures from the APK, which we will later use
	// for rendering.

	private void loadTextures()
	{
		mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
				getAssets()));
		mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
				getAssets()));
		mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
				getAssets()));
		mTextures.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg",
				getAssets()));
	}


	// Called when the activity will start interacting with the user.
	@Override
	protected void onResume()
	{
		Log.d(LOGTAG, "onResume");
		super.onResume();

		// This is needed for some Droid devices to force portrait
		if (mIsDroidDevice)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		try
		{
			vuforiaAppSession.resumeAR();
		} catch (SampleApplicationException e)
		{
			Log.e(LOGTAG, e.getString());
		}

		// Resume the GL view:
		if (mGlView != null)
		{
			mGlView.setVisibility(View.VISIBLE);
			mGlView.onResume();
		}

	}


	// Callback for configuration changes the activity handles itself
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		Log.d(LOGTAG, "onConfigurationChanged");
		super.onConfigurationChanged(config);

		vuforiaAppSession.onConfigurationChanged();
	}


	// Called when the system is about to start resuming a previous activity.
	@Override
	protected void onPause()
	{
		Log.d(LOGTAG, "onPause");
		super.onPause();

		if (mGlView != null)
		{
			mGlView.setVisibility(View.INVISIBLE);
			mGlView.onPause();
		}

		// Turn off the flash
		if (mFlashOptionView != null && mFlash)
		{
			// OnCheckedChangeListener is called upon changing the checked state
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				((Switch) mFlashOptionView).setChecked(false);
			} else
			{
				((CheckBox) mFlashOptionView).setChecked(false);
			}
		}

		try
		{
			vuforiaAppSession.pauseAR();
		} catch (SampleApplicationException e)
		{
			Log.e(LOGTAG, e.getString());
		}
	}


	// The final call you receive before your activity is destroyed.
	@Override
	protected void onDestroy()
	{
		Log.d(LOGTAG, "onDestroy");
		super.onDestroy();

		try
		{
			vuforiaAppSession.stopAR();
		} catch (SampleApplicationException e)
		{
			Log.e(LOGTAG, e.getString());
		}

		// Unload texture:
		mTextures.clear();
		mTextures = null;

		System.gc();
	}


	// Initializes AR application components.
	private void initApplicationAR()
	{
		// Create OpenGL ES view:
		int depthSize = 16;
		int stencilSize = 0;
		boolean translucent = Vuforia.requiresAlpha();

		mGlView = new SampleApplicationGLView(this);
		mGlView.init(translucent, depthSize, stencilSize);

		mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
		mRenderer.setTextures(mTextures);
		mGlView.setRenderer(mRenderer);
	}


	private void startLoadingAnimation()
	{
		mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
				null);

		mUILayout.setVisibility(View.INVISIBLE);
		mUILayout.setBackgroundColor(Color.BLACK);

		// Gets a reference to the loading dialog
		loadingDialogHandler.mLoadingDialogContainer = mUILayout
				.findViewById(R.id.loading_indicator);

		// Shows the loading indicator at start
		loadingDialogHandler
				.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

		// Adds the inflated layout to the view
		addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

	}


	// Methods to load and destroy tracking data.
	@Override
	public boolean doLoadTrackersData()
	{
		TrackerManager tManager = TrackerManager.getInstance();
		ObjectTracker objectTracker = (ObjectTracker) tManager
				.getTracker(ObjectTracker.getClassType());
		if (objectTracker == null)
			return false;

		if (mCurrentDataset == null)
			mCurrentDataset = objectTracker.createDataSet();

		if (mCurrentDataset == null)
			return false;

		if (!mCurrentDataset.load(
				mDatasetStrings.get(mCurrentDatasetSelectionIndex),
				STORAGE_TYPE.STORAGE_APPRESOURCE))
			return false;

		if (!objectTracker.activateDataSet(mCurrentDataset))
			return false;

		int numTrackables = mCurrentDataset.getNumTrackables();
		for (int count = 0; count < numTrackables; count++)
		{
			Trackable trackable = mCurrentDataset.getTrackable(count);
			if(isExtendedTrackingActive())
			{
				trackable.startExtendedTracking();
			}

			String name = "Current Dataset : " + trackable.getName();
			trackable.setUserData(name);
			Log.d(LOGTAG, "UserData:Set the following user data "
					+ (String) trackable.getUserData());
		}

		return true;
	}


	@Override
	public boolean doUnloadTrackersData()
	{
		// Indicate if the trackers were unloaded correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		ObjectTracker objectTracker = (ObjectTracker) tManager
				.getTracker(ObjectTracker.getClassType());
		if (objectTracker == null)
			return false;

		if (mCurrentDataset != null && mCurrentDataset.isActive())
		{
			if (objectTracker.getActiveDataSet(0).equals(mCurrentDataset)
					&& !objectTracker.deactivateDataSet(mCurrentDataset))
			{
				result = false;
			} else if (!objectTracker.destroyDataSet(mCurrentDataset))
			{
				result = false;
			}

			mCurrentDataset = null;
		}

		return result;
	}


	@Override
	public void onInitARDone(SampleApplicationException exception)
	{

		if (exception == null)
		{
			initApplicationAR();

			mRenderer.setActive(true);

			// Now add the GL surface view. It is important
			// that the OpenGL ES surface view gets added
			// BEFORE the camera is started and video
			// background is configured.
			addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));

			// Sets the UILayout to be drawn in front of the camera
			mUILayout.bringToFront();

			// Sets the layout background to transparent
			mUILayout.setBackgroundColor(Color.TRANSPARENT);

			try
			{
				vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
			} catch (SampleApplicationException e)
			{
				Log.e(LOGTAG, e.getString());
			}

			boolean result = CameraDevice.getInstance().setFocusMode(
					CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

			if (result)
				mContAutofocus = true;
			else
				Log.e(LOGTAG, "Unable to enable continuous autofocus");

			mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
					mGlView, mUILayout, null);
			setSampleAppMenuSettings();

		} else
		{
			Log.e(LOGTAG, exception.getString());
			showInitializationErrorMessage(exception.getString());
		}
	}


	// Shows initialization error messages as System dialogs
	public void showInitializationErrorMessage(String message)
	{
	}


	@Override
	public void onVuforiaUpdate(State state)
	{
		if (mSwitchDatasetAsap)
		{
			mSwitchDatasetAsap = false;
			TrackerManager tm = TrackerManager.getInstance();
			ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
					.getClassType());
			if (ot == null || mCurrentDataset == null
					|| ot.getActiveDataSet(0) == null)
			{
				Log.d(LOGTAG, "Failed to swap datasets");
				return;
			}

			doUnloadTrackersData();
			doLoadTrackersData();
		}
	}


	@Override
	public boolean doInitTrackers()
	{
		// Indicate if the trackers were initialized correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		Tracker tracker;

		// Trying to initialize the image tracker
		tracker = tManager.initTracker(ObjectTracker.getClassType());
		if (tracker == null)
		{
			Log.e(
					LOGTAG,
					"Tracker not initialized. Tracker already initialized or the camera is already started");
			result = false;
		} else
		{
			Log.i(LOGTAG, "Tracker successfully initialized");
		}
		return result;
	}


	@Override
	public boolean doStartTrackers()
	{
		// Indicate if the trackers were started correctly
		boolean result = true;

		Tracker objectTracker = TrackerManager.getInstance().getTracker(
				ObjectTracker.getClassType());
		if (objectTracker != null)
			objectTracker.start();

		return result;
	}


	@Override
	public boolean doStopTrackers()
	{
		// Indicate if the trackers were stopped correctly
		boolean result = true;

		Tracker objectTracker = TrackerManager.getInstance().getTracker(
				ObjectTracker.getClassType());
		if (objectTracker != null)
			objectTracker.stop();

		return result;
	}


	@Override
	public boolean doDeinitTrackers()
	{
		// Indicate if the trackers were deinitialized correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		tManager.deinitTracker(ObjectTracker.getClassType());

		return result;
	}


	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// Process the Gestures
		if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
			return true;

		return mGestureDetector.onTouchEvent(event);
	}


	public boolean isExtendedTrackingActive()
	{
		return mExtendedTracking;
	}

	final public static int CMD_BACK = -1;
	final public static int CMD_EXTENDED_TRACKING = 1;
	final public static int CMD_AUTOFOCUS = 2;
	final public static int CMD_FLASH = 3;
	final public static int CMD_CAMERA_FRONT = 4;
	final public static int CMD_CAMERA_REAR = 5;
	final public static int CMD_DATASET_START_INDEX = 6;


	// This method sets the menu's settings
	private void setSampleAppMenuSettings()
	{
		SampleAppMenuGroup group;

		group = mSampleAppMenu.addGroup("", false);
		group.addTextItem(getString(R.string.menu_back), -1);

		group = mSampleAppMenu.addGroup("", true);
		group.addSelectionItem(getString(R.string.menu_extended_tracking),
				CMD_EXTENDED_TRACKING, false);
		group.addSelectionItem(getString(R.string.menu_contAutofocus),
				CMD_AUTOFOCUS, mContAutofocus);
		mFlashOptionView = group.addSelectionItem(
				getString(R.string.menu_flash), CMD_FLASH, false);

		Camera.CameraInfo ci = new Camera.CameraInfo();
		boolean deviceHasFrontCamera = false;
		boolean deviceHasBackCamera = false;
		for (int i = 0; i < Camera.getNumberOfCameras(); i++)
		{
			Camera.getCameraInfo(i, ci);
			if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				deviceHasFrontCamera = true;
			else if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
				deviceHasBackCamera = true;
		}

		if (deviceHasBackCamera && deviceHasFrontCamera)
		{
			group = mSampleAppMenu.addGroup(getString(R.string.menu_camera),
					true);
			group.addRadioItem(getString(R.string.menu_camera_front),
					CMD_CAMERA_FRONT, false);
			group.addRadioItem(getString(R.string.menu_camera_back),
					CMD_CAMERA_REAR, true);
		}

		group = mSampleAppMenu
				.addGroup(getString(R.string.menu_datasets), true);
		mStartDatasetsIndex = CMD_DATASET_START_INDEX;
		mDatasetsNumber = mDatasetStrings.size();

		group.addRadioItem("Stones & Chips", mStartDatasetsIndex, true);
		group.addRadioItem("Tarmac", mStartDatasetsIndex + 1, false);

		mSampleAppMenu.attachMenu();
	}

	public void scanBar(View v) {
		try {
			isRepeatedMode = true;
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(QRCodeScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	public void scanQR(View v) {
		try {
			isRepeatedMode = false;
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(QRCodeScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {

				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return downloadDialog.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if(isRepeatedMode) {
					if (!contents.equals("bobcob")) {
						scanQR(null);
					}
				}

				Toast toast = Toast.makeText(this, "Content:" + contents, Toast.LENGTH_LONG);
				Bitmap overlay = BitmapFactory.decodeResource(null, R.id.button);
				Bitmap.createBitmap(overlay,0,0,0,0);
				Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
				toast.show();
				if(contents.equals("B1")) {
					Intent itemIntent = new Intent(this, ItemActivity.class);
					startActivity(itemIntent);
				}
			}
		}
	}
}