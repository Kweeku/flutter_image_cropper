package vn.hunghd.flutter.plugins.imagecropper;


import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.graphics.Color;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * ImageCropperPlugin
 */
public class ImageCropperPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {
    private static final String CHANNEL = "plugins.hunghd.vn/image_cropper";
    private ImageCropperDelegate delegate;

    private ActivityPluginBinding activityPluginBinding;

    private void setupEngine(BinaryMessenger messenger) {
        MethodChannel channel = new MethodChannel(messenger, CHANNEL);
        channel.setMethodCallHandler(this);
    }

    public ImageCropperDelegate setupActivity(Activity activity) {
        delegate = new ImageCropperDelegate(activity);
        return delegate;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {

        if (call.method.equals("cropImage")) {
            delegate.startCrop(call, result);
        } else if (call.method.equals("recoverImage")) {
            delegate.recoverImage(call, result);
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
        setupEngine(flutterPluginBinding.getBinaryMessenger());
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        setupActivity(activityPluginBinding.getActivity());
        this.activityPluginBinding = activityPluginBinding;
        activityPluginBinding.addActivityResultListener(delegate);
        
        // Configure activity for proper status bar handling
        Activity activity = activityPluginBinding.getActivity();
        setupActivityForStatusBar(activity);
    }
    
    /**
     * Configure the activity for proper status bar handling
     * This ensures that the status bar doesn't cover action buttons
     * especially on Google Pixel devices
     * @param activity The activity to configure
     */
    private void setupActivityForStatusBar(Activity activity) {
        if (activity != null) {
            // Check if this is a Pixel device
            boolean isPixelDevice = isPixelDevice();
            
            if (isPixelDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Special handling for Pixel devices on Android 9.0 (API 28) and above
                activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );
                
                // Handle display cutouts for devices with notches (like Pixel 3 XL and newer)
                activity.getWindow().getAttributes().layoutInDisplayCutoutMode = 
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                
                // Ensure transparent status bar
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
                
                // Set system UI visibility flags for proper handling on Pixel devices
                activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // For Android 5.0 to 8.1, or non-Pixel devices on Android 9.0+
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
                
                // Set system UI visibility flags for proper handling
                activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
        }
    }
    
    /**
     * Check if the current device is a Google Pixel device
     * This is used to apply specific fixes for Pixel devices
     * @return true if the device is a Pixel device
     */
    private boolean isPixelDevice() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String model = Build.MODEL.toLowerCase();
        String brand = Build.BRAND.toLowerCase();
        
        return (manufacturer.contains("google") && (model.contains("pixel") || brand.contains("pixel")));
    }
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding flutterPluginBinding) {
        // no need to clear channel
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        // Restore any window configurations that were set
        if (activityPluginBinding != null && activityPluginBinding.getActivity() != null) {
            Activity activity = activityPluginBinding.getActivity();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Clear any flags that may have been set
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                
                // Reset system UI visibility to default
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
        
        activityPluginBinding.removeActivityResultListener(delegate);
        activityPluginBinding = null;
        delegate = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }


}
