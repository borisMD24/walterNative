package com.walter;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class HapticFeedbackManager {

    private final Vibrator vibrator;
    private final boolean hasAmplitudeControl;

    public HapticFeedbackManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = vibratorManager.getDefaultVibrator();
        } else {
            this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        this.hasAmplitudeControl = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator.hasAmplitudeControl();
    }

    public void vibrate(float normalizedIntensity, int durationMs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int amplitude = hasAmplitudeControl
                    ? Math.max(1, Math.min(255, (int) (normalizedIntensity * 255f)))
                    : VibrationEffect.DEFAULT_AMPLITUDE;

            VibrationEffect effect = VibrationEffect.createOneShot(durationMs, amplitude);
            vibrator.vibrate(effect);
        } else {
            // Old-school devices : just vibrate with no control
            vibrator.vibrate(durationMs);
        }
    }
}
