package com.cliffordlab.amoss.gui.ball;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cliffordlab.amoss.R;
import com.cliffordlab.amoss.gui.MainActivity;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BallGameActivity extends AppCompatActivity {

	public static final String TITLE = "Ball";

	BallView mBallView;
	HoleView mHoleView;
	Handler RedrawHandler = new Handler(); //so redraw occurs in main thread
	Timer mTmr = null;
	TimerTask mTsk = null;
	TimerTask colorTask;
	int mScrWidth, mScrHeight;
	android.graphics.PointF mBallPos, mBallSpd;
	private int score = 0;
	private TextView scoreLabel;
	private TextView timeLeft;
	private CountDownTimer mTimer;
	FrameLayout mainView;
	private int colorIndex = 0;
	private final Timer colorTimer = new Timer();
	Random rand = new Random();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ball_game);
		mainView = (FrameLayout) findViewById(R.id.main_view);
		getWindow().setFlags(0xFFFFFFFF, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//get screen dimensions
		Display display = getWindowManager().getDefaultDisplay();
		mScrWidth = display.getWidth();
		mScrHeight = display.getHeight();
		mBallPos = new android.graphics.PointF();
		mBallSpd = new android.graphics.PointF();

		//create variables for ball position and speed
		mBallPos.x = mScrWidth / 2;
		mBallPos.y = mScrHeight / 2;
		mBallSpd.x = 0;
		mBallSpd.y = 0;

		scheduleColorChange();

		int randX = rand.nextInt(mScrWidth);
		int randY = rand.nextInt(mScrHeight);

		mBallView = new BallView(this, mBallPos.x, mBallPos.y, 20);
		mHoleView = new HoleView(this, randX, randY, colorIndex);

		mainView.addView(mHoleView);
		mainView.addView(mBallView);
		mBallView.invalidate(); //call onDraw in BallView
		setLabels();

		//listener for accelerometer, use anonymous class for simplicity
		((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(
						new SensorEventListener() {
							@Override
							public void onSensorChanged(SensorEvent event) {
								//set ball speed based on phone tilt (ignore Z axis)
								mBallSpd.x = -event.values[0];
								mBallSpd.y = event.values[1];
								//timer event will redraw ball
							}

							@Override
							public void onAccuracyChanged(Sensor sensor, int accuracy) {
							}
						},
						((SensorManager) getSystemService(Context.SENSOR_SERVICE))
										.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
						SensorManager.SENSOR_DELAY_NORMAL);

		mTimer = new CountDownTimer(20000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				timeLeft.setText(millisUntilFinished / 1000 + "s " + "left");
				scoreLabel.setText(score + "");
			}

			@Override
			public void onFinish() {
				Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
				intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intentMain);
				finish();
			}
		}.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		//app moved to background, stop background threads
		mTmr.cancel(); //kill/release timer (our only background thread)
		mTmr = null;
		mTsk = null;
		colorTask = null;

	}

	private void dragBall() {
		mainView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mBallPos.x = event.getX();
				mBallPos.y = event.getY();
				return true;
			}
		});
	}

	private void scheduleColorChange() {
		if (colorIndex == 4) {
			colorTask = new TimerTask() {
				@Override
				public void run() {
					colorIndex = rand.nextInt(4);
				}
			};
			colorTimer.scheduleAtFixedRate(colorTask, 0, 4000);
		} else {
			colorTask = new TimerTask() {
				@Override
				public void run() {
					if (colorIndex <= 2) {
						colorIndex += 1;
					}
				}
			};
			colorTimer.scheduleAtFixedRate(colorTask, 15000, 15000);
		}
	}


	private void ballToHole() {
		Log.i("invalidate", "this is happening");
		if (mBallView.x >= mHoleView.x - 50 && mBallView.x <= mHoleView.x + 50) {
			if (mBallView.y >= mHoleView.y - 50 && mBallView.y <= mHoleView.y + 50) {
				Display display = getWindowManager().getDefaultDisplay();
				mScrWidth = display.getWidth();
				mScrHeight = display.getHeight();

				Random rand = new Random();
				int randX = rand.nextInt(mScrWidth);
				int randY = rand.nextInt(mScrHeight);

				mHoleView = new HoleView(this, randX, randY, colorIndex);
				mainView.addView(mHoleView);
				mHoleView.invalidate();
				Log.i("invalidate", "this is happening");
				score++;
			}
		}
	}

	private void setLabels() {
		timeLeft = (TextView) findViewById(R.id.timeLeftTextView);
		scoreLabel = (TextView) findViewById(R.id.scoreTextView);
		scoreLabel.setText(score + "");
	}

	@Override
	protected void onResume() {
		super.onResume();
		//create timer to move ball to new position
		mTmr = new Timer();
		mTsk = new TimerTask() {
			public void run() {
				mBallPos.x += mBallSpd.x;
				mBallPos.y += mBallSpd.y;

				if (mBallPos.x > mScrWidth) mBallPos.x = 0;
				if (mBallPos.y > mScrHeight) mBallPos.y = 0;
				if (mBallPos.x < 0) mBallPos.x = mScrWidth;
				if (mBallPos.y < 0) mBallPos.y = mScrHeight;

				if (mBallPos.x > mScrWidth) mBallPos.x = 0;
				if (mBallPos.y > mScrHeight) mBallPos.y = 0;
				if (mBallPos.x < 0) mBallPos.x = mScrWidth;
				if (mBallPos.y < 0) mBallPos.y = mScrHeight;
				//update ball class instance
				mBallView.x = mBallPos.x;
				mBallView.y = mBallPos.y;
				//redraw ball. Must run in background thread to prevent thread lock.
				RedrawHandler.post(new Runnable() {
					public void run() {
						mBallView.invalidate();
						ballToHole();
					}
				});
			}
		};

		mTmr.schedule(mTsk, 10, 10); //delay, period //start timer
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void onBackPressed() {

	}

}
