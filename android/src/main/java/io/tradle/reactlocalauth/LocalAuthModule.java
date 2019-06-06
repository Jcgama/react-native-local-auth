
package io.tradle.reactlocalauth;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;

// source for main part from:
// https://github.com/googlesamples/android-ConfirmCredential/blob/master/Application/src/main/java/com/example/android/confirmcredential/MainActivity.java

public class LocalAuthModule extends ReactContextBaseJavaModule {

  private static final int AUTH_REQUEST = 18864;
  private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
  private static final String E_AUTH_CANCELLED = "LAErrorUserCancel";
  private static final String E_FAILED_TO_SHOW_AUTH = "E_FAILED_TO_SHOW_AUTH";
  private static final String E_ONE_REQ_AT_A_TIME = "E_ONE_REQ_AT_A_TIME";

  private final ReactApplicationContext reactContext;
  private KeyguardManager mKeyguardManager;
  private Promise authPromise;

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
      if (requestCode != AUTH_REQUEST || authPromise == null) return;

      if (resultCode == Activity.RESULT_CANCELED) {
        authPromise.reject(E_AUTH_CANCELLED, "User canceled");
      } else if (resultCode == Activity.RESULT_OK) {
        authPromise.resolve(true);
      }

      authPromise = null;
    }
  };

  public LocalAuthModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    reactContext.addActivityEventListener(mActivityEventListener);
    mKeyguardManager = (KeyguardManager) this.reactContext.getSystemService(Context.KEYGUARD_SERVICE);
  }

  @Override
  public String getName() {
    return "RNLocalAuth";
  }

  @ReactMethod
  public void isDeviceSecure(final Promise promise) {
    promise.resolve(mKeyguardManager.isDeviceSecure());
  }

  @ReactMethod
  public void authenticate(ReadableMap map, final Promise promise) {
    // Create the Confirm Credentials screen. You can customize the title and description. Or
    // we will provide a generic one for you if you leave it null
    Activity currentActivity = getCurrentActivity();

    if (authPromise != null) {
      promise.reject(E_ONE_REQ_AT_A_TIME, "Activity doesn't exist");
      return;
    }

    if (currentActivity == null) {
      promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "One auth request at a time");
      return;
    }

    // Store the promise to resolve/reject when picker returns data
    authPromise = promise;

    String reason = map.hasKey("reason") ? map.getString("reason") : null;
    String description = map.hasKey("description") ? map.getString("description") : null;
    try {
      final Intent authIntent = mKeyguardManager.createConfirmDeviceCredentialIntent(reason, description);
      currentActivity.startActivityForResult(authIntent, AUTH_REQUEST);
    } catch (Exception e) {
      authPromise.reject(E_FAILED_TO_SHOW_AUTH, e);
      authPromise = null;
    }
  }

  @ReactMethod
  public boolean hasTouchID() {
    final Activity activity = getCurrentActivity();
    if (activity == null) {
        return false;
    }

    boolean result = isFingerprintAuthAvailable();
    // if (result == FingerprintAuthConstants.IS_SUPPORTED) {
    //     reactSuccessCallback.invoke("Fingerprint");
    // } else {
    //     reactErrorCallback.invoke("Not supported.", result);
    // }
    return result;
  }

  private boolean isFingerprintAuthAvailable() {
    if (android.os.Build.VERSION.SDK_INT < 23) {
        // return FingerprintAuthConstants.NOT_SUPPORTED;
        return false;
    }

    final Activity activity = getCurrentActivity();
    if (activity == null) {
        // return FingerprintAuthConstants.NOT_AVAILABLE; // we can't do the check
        return false;
    }

    final KeyguardManager mkeyguardManager = getKeyguardManager();

    final FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);

    // if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
    //     return FingerprintAuthConstants.NOT_PRESENT;
    // }

    // if (mkeyguardManager == null || !mkeyguardManager.isKeyguardSecure()) {
    //     return FingerprintAuthConstants.NOT_AVAILABLE;
    // }

    // if (!fingerprintManager.hasEnrolledFingerprints()) {
    //     return FingerprintAuthConstants.NOT_ENROLLED;
    // }
    // return FingerprintAuthConstants.IS_SUPPORTED;
    if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
      return true;
    }
    return false;
  }

  private KeyguardManager getKeyguardManager() {
      if (mKeyguardManager != null) {
          return mKeyguardManager;
      }
      final Activity activity = getCurrentActivity();
      if (activity == null) {
          return null;
      }

      mKeyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);

      return mKeyguardManager;
  }

  private final class FingerprintAuthConstants {
    public static final int IS_SUPPORTED = 100;
    public static final int NOT_SUPPORTED = 101;
    public static final int NOT_PRESENT = 102;
    public static final int NOT_AVAILABLE = 103;
    public static final int NOT_ENROLLED = 104;
    public static final int AUTHENTICATION_FAILED = 105;
    public static final int AUTHENTICATION_CANCELED = 106;
  }
}
