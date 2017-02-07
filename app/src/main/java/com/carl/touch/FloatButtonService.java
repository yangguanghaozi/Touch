package com.carl.touch;

import android.accessibilityservice.AccessibilityService;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;




public class FloatButtonService extends AccessibilityService {

    private static final String TAG = "FloatButtonService";
    private static LinearLayout mFloatLayout;
    private static WindowManager.LayoutParams wmParams;
    private static WindowManager mWindowManager;
    private static ImageButton mFloatView;
    private static Vibrator vibrator;
    private boolean moveAction;
    private boolean taskStatus;
    private int clickCount = 0;
    private TouchEventHandler handler = new TouchEventHandler();

    private DevicePolicyManager deviceManger;
    private ComponentName componentName;

    private static final int ONE_CLICK = 0x01;
    private static final int TWO_CLICK = 0x02;
    private static final int THREE_CLICK = 0x03;
    private static final int FOUR_CLICK = 0x04;
    private static final int LONG_CLICK = 0x10;
    private static final int ALPHA_CHANGE = 0x11;

    private static final int alpha_delay = 3000;
    private static final int clickDelay = 300;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
        handler.sendEmptyMessageDelayed(ALPHA_CHANGE, alpha_delay);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        deviceManger = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, LockScreenAdmin.class);

        

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d(TAG, accessibilityEvent.toString());
        switch (accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (accessibilityEvent.getClassName().toString()
                        .equals("com.android.systemui.recents.RecentsActivity")) {
                    taskStatus = true;
                    mFloatView.setBackgroundResource(R.mipmap.button_home);
                } else {
                    taskStatus = false;
                    mFloatView.setBackgroundResource(R.mipmap.button_caffee);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        vibrator.cancel();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
    }

    @Override
    public void onInterrupt() {
    }

    private void slideToEdge() {
        Point size = new Point();
        mWindowManager.getDefaultDisplay().getSize(size);
        Log.i(TAG, size.toString());
        int dx1 = wmParams.x;
        int dx2 = size.x - wmParams.x;
        int dy1 = wmParams.y;
        int dy2 = size.y - wmParams.y;
        int min = Math.min(Math.min(dx1, dx2), Math.min(dy1, dy2));
        ValueAnimator anim;
        if (min == dx1) {
            anim = ValueAnimator.ofInt(wmParams.x, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    wmParams.x = (int) animation.getAnimatedValue();
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
            });
        } else if (min == dx2) {
            anim = ValueAnimator.ofInt(wmParams.x, size.x);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    wmParams.x = (int) animation.getAnimatedValue();
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
            });
        } else if (min == dy1) {
            anim = ValueAnimator.ofInt(wmParams.y, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    wmParams.y = (int) animation.getAnimatedValue();
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
            });
        } else {
            anim = ValueAnimator.ofInt(wmParams.y, size.y);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    wmParams.y = (int) animation.getAnimatedValue();
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
            });
        }
        anim.setDuration(200);
        anim.start();
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        wmParams.gravity = Gravity.START | Gravity.TOP;

        wmParams.x = 0;
        wmParams.y = 1024;

        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.windowAnimations = android.R.style.Animation_Translucent;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_view, null);
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatView = (ImageButton) mFloatLayout.findViewById(R.id.float_button);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        mFloatView.setClickable(true);
        mFloatView.setLongClickable(true);

        mFloatView.setOnTouchListener(new View.OnTouchListener() {

            private int pushX;
            private int pushY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mFloatView.setAlpha(0.9f);
                handler.removeMessages(ALPHA_CHANGE);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveAction = false;
                        pushX = (int) event.getRawX();
                        pushY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        if (Math.abs(pushX - x) > 32 || Math.abs(pushY - y) > 32 || moveAction) {
                            moveAction = true;
                            wmParams.x = x - mFloatView.getMeasuredWidth() / 2;
                            wmParams.y = y - mFloatView.getMeasuredHeight() / 2;
                            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        pushX = 0;
                        pushY = 0;
                        handler.sendEmptyMessageDelayed(ALPHA_CHANGE, alpha_delay);
                        if (moveAction) slideToEdge();
                        return moveAction;

                }
                return false;
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click");
                clickCount++;
                for (int i = 1; i < clickCount; i++) {
                    handler.removeMessages(i);
                }
                handler.sendEmptyMessageDelayed(clickCount, clickDelay);
            }
        });

        mFloatView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (moveAction) return true;
                Log.d(TAG, "long click");
                vibrator.vibrate(50);
                handler.sendEmptyMessage(LONG_CLICK);
                return true;
            }
        });
    }

    private void hide() {
        Intent intentClick = new Intent(this, NotificationBroadcastReceiver.class);
        intentClick.setAction("notification_clicked");
        intentClick.putExtra(NotificationBroadcastReceiver.TYPE, 1);
        PendingIntent pendingIntentClick = PendingIntent.getBroadcast(this, 0, intentClick, PendingIntent.FLAG_ONE_SHOT);

        Intent intentCancel = new Intent(this, NotificationBroadcastReceiver.class);
        intentCancel.setAction("notification_cancelled");
        intentCancel.putExtra(NotificationBroadcastReceiver.TYPE, 1);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0, intentCancel, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.button)
                .setContentTitle("Touch")
                .setContentText("Click to open Touch")
                .setContentIntent(pendingIntentClick)
                .setDeleteIntent(pendingIntentCancel);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
        mWindowManager.removeView(mFloatLayout);
    }

    private void lock(){
        boolean active = deviceManger.isAdminActive(componentName);
        if (active) {
            Log.d(TAG, "lock now");
            deviceManger.lockNow();
        }
        else {
            Log.d(TAG, "Start activity");
            Intent intentStartActivity = new Intent(this, LockScreenActivity.class);
            intentStartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentStartActivity);
        }
    }

    public static void resume() {
        mWindowManager.addView(mFloatLayout, wmParams);
    }

    class TouchEventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "hand " + msg.what);
            switch (msg.what) {
                case ALPHA_CHANGE:
                    mFloatView.setAlpha(0.2f);
                    break;
                case ONE_CLICK:
                    clickCount = 0;
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    vibrator.vibrate(10);
                    break;
                case TWO_CLICK:
                    clickCount = 0;
                    performGlobalAction(GLOBAL_ACTION_RECENTS);
                    vibrator.vibrate(10);
                    break;
                case THREE_CLICK:
                    clickCount = 0;
                    lock();
                    vibrator.vibrate(10);
                    break;
                case FOUR_CLICK:
                    clickCount = 0;
                    performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                case LONG_CLICK:
                    hide();
                    vibrator.vibrate(10);
                    break;
                default:
                    clickCount = 0;
                    Log.e(TAG, "Error msg what " + msg.what);
                    performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                    vibrator.vibrate(10);
                    break;
            }
            super.handleMessage(msg);
        }
    }





}