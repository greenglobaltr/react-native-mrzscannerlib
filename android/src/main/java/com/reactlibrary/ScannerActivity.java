package com.reactlibrary;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.scansolutions.mrzscannerlib.MRZResultModel;
import com.scansolutions.mrzscannerlib.MRZScanner;
import com.scansolutions.mrzscannerlib.MRZScannerListener;
import com.scansolutions.mrzscannerlib.ScannerType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;


public class ScannerActivity extends AppCompatActivity implements MRZScannerListener {

    static int scannerType = 0;
    static MRZScannerListener mrzScannerListener;
    static final String OVERLAY_IMG_KEY = "MRZ_BITMAP_PATH";
    static final String OVERLAY_IMG_NAME = "MRZ_OVERLAY_IMG.jpg";
    private MRZScanner mrzScanner;
    static MRZScannerDismissedListener mrzScannerDismissed;
    static boolean flashToggleActivated = false;
    static RectF sScanningRect;
    static Boolean extractIdBack = false;
    static Boolean extractPassportFullImage = false;
    static Boolean extractSignature = false;
    static Boolean extractPortrait = false;
    static Boolean showFlashButton = true;

    static Boolean continuousScanningEnabled = false;
    static ScannerActivityListener scannerActivityListener;

    public static void startScanner(Context context,
                                    MRZScannerListener listener,
                                    String base64String,
                                    MRZScannerDismissedListener mrzScannerDismissedListener) {
        startScanner(context, listener, base64String, mrzScannerDismissedListener, null);
    }

    public static void startScanner(Context context,
                                    MRZScannerListener listener,
                                    String base64String,
                                    MRZScannerDismissedListener mrzScannerDismissedListener,
                                    RectF scanningRect) {
        startScanner(context, listener, base64String, mrzScannerDismissedListener, scanningRect, null);
    }

    public static void startScanner(Context context,
                                    MRZScannerListener listener,
                                    String base64String,
                                    MRZScannerDismissedListener mrzScannerDismissedListener,
                                    RectF scanningRect,
                                    ScannerActivityListener scannerActivityListener) {
        sScanningRect = scanningRect;
        mrzScannerListener = listener;
        mrzScannerDismissed = mrzScannerDismissedListener;
        ScannerActivity.scannerActivityListener = scannerActivityListener;
        Intent intent = new Intent(context, ScannerActivity.class);

        if (base64String != null && !"".equals(base64String)) {
            String imagePath = ScannerActivity.saveToInternalStorage(base64String, context);
            intent.putExtra(ScannerActivity.OVERLAY_IMG_KEY, imagePath);
        }

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (sScanningRect != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            Point displaySize = getDisplaySize(ScannerActivity.this);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.x = (int) (displaySize.x * (sScanningRect.left / 100.0f));
            lp.y = (int) (displaySize.y * (sScanningRect.top / 100.0f));
            lp.horizontalMargin = 0;
            lp.verticalMargin = 0;
            lp.width = (int) (displaySize.x * (sScanningRect.right / 100.0f));
            lp.height = (int) (displaySize.y * (sScanningRect.bottom / 100.0f));
            getWindow().setAttributes(lp);
        } else {
            setTheme(R.style.Theme_AppCompat_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        String imagePathId = getIntent().getStringExtra(OVERLAY_IMG_KEY);
        Bitmap overlayImage = loadImageFromStorage(imagePathId);

        mrzScanner = (MRZScanner) getFragmentManager().findFragmentById(R.id.scannerFragment);
        setupPartialView();
        mrzScanner.setScannerType(ScannerType.values()[scannerType]);
        mrzScanner.setCustomImageOverlay(overlayImage);
        mrzScanner.setContinuousScanningEnabled(continuousScanningEnabled);
        mrzScanner.setShowFlashButton(showFlashButton);

        MRZScanner.setExtractPortraitEnabled(extractPortrait);
        MRZScanner.setExtractFullPassportImageEnabled(extractPassportFullImage);
        MRZScanner.setExtractSignatureEnabled(extractSignature);
        MRZScanner.setExtractPortraitEnabled(extractPortrait);
        MRZScanner.setExtractIdBackImageEnabled(extractIdBack);

        if (flashToggleActivated) {
            mrzScanner.toggleFlash(true);
        }

        if (scannerActivityListener == null)
            scannerActivityListener = new ScannerActivityListener() {
                @Override
                public void closeScanner() {
                    ScannerActivity.this.finish();
                }

                @Override
                public void setContinuousScanningEnabled(Boolean enabled) {
                    if (mrzScanner != null) {
                        mrzScanner.setContinuousScanningEnabled(enabled);
                    }
                }

                @Override
                public void resumeScanning() {
                    if (mrzScanner != null) {
                        mrzScanner.resumeScanning();
                    }
                }
            };
    }

    private void setupPartialView() {
        if (sScanningRect != null) {
            Point displaySize = getDisplaySize(ScannerActivity.this);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mrzScanner.getView().getLayoutParams();
            params.width = (int) (displaySize.x * (sScanningRect.right / 100.0f));
            params.height = (int) (displaySize.y * (sScanningRect.bottom / 100.0f));
            mrzScanner.getView().setLayoutParams(params);
            if (sScanningRect.left == 0 && sScanningRect.top == 0 && sScanningRect.right == 1 && sScanningRect.bottom == 1) {
                mrzScanner.hidePreview();
            }
        }
    }

    public static void setExtractPortraitEnabled(boolean active) {
        // Update boolean statically both in ScannerActivity and MRZSsetcanner to make sure that
        // we handle this config method when called before and after mrzScanner is initialized
        // while providing default values declared in ScannerActivity that are different than the defaults in MRZScanner
        ScannerActivity.extractPortrait = active;
        MRZScanner.setExtractPortraitEnabled(active);
    }

    public static void setExtractFullPassportImageEnabled(boolean active) {
        // Update boolean statically both in ScannerActivity and MRZScanner to make sure that
        // we handle this config method when called before and after mrzScanner is initialized
        // while providing default values declared in ScannerActivity that are different than the defaults in MRZScanner
        ScannerActivity.extractPassportFullImage = active;
        MRZScanner.setExtractFullPassportImageEnabled(active);
    }

    public static void setShowFlashButton(boolean active) {
        showFlashButton = active;
    }

    public static void setExtractSignatureEnabled(boolean active) {
        // Update boolean statically both in ScannerActivity and MRZScanner to make sure that
        // we handle this config method when called before and after mrzScanner is initialized
        // while providing default values declared in ScannerActivity that are different than the defaults in MRZScanner
        ScannerActivity.extractSignature = active;
        MRZScanner.setExtractSignatureEnabled(active);
    }

    public static void setExtractIdBackImageEnabled(boolean active) {
        // Update boolean statically both in ScannerActivity and MRZScanner to make sure that
        // we handle this config method when called before and after mrzScanner is initialized
        // while providing default values declared in ScannerActivity that are different than the defaults in MRZScanner
        ScannerActivity.extractIdBack = active;
        MRZScanner.setExtractIdBackImageEnabled(active);
    }

    public static void setVibrateOnSuccessfulScan(boolean active) {
        MRZScanner.setEnableVibrationOnSuccess(active);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupPartialView();
    }

    @Override
    public void successfulScanWithResult(MRZResultModel mrzResultModel) {
        mrzScannerListener.successfulScanWithResult(mrzResultModel);
        if (!continuousScanningEnabled)
            finish();
    }

    @Override
    public void successfulScanWithDocumentImage(Bitmap bitmap) {
        mrzScannerListener.successfulScanWithDocumentImage(bitmap);
        finish();
    }

    @Override
    public void successfulIdFrontImageScan(Bitmap bitmap, Bitmap bitmap1) {
        mrzScannerListener.successfulIdFrontImageScan(bitmap, bitmap1);
        finish();
    }

    public void toggleFlash(boolean active) {
        if (mrzScanner != null) {
            mrzScanner.toggleFlash(active);
        }
    }

    @Override
    public void scanImageFailed() {
        mrzScannerListener.scanImageFailed();
        finish();
    }

    @Override
    public void permissionsWereDenied() {
        mrzScannerListener.permissionsWereDenied();
        finish();
    }

    private static Bitmap loadImageFromStorage(String path) {
        Bitmap b = null;
        try {
            File f = new File(path, OVERLAY_IMG_NAME);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mrzScannerDismissed.scannerWasDismissed();
    }

    private static String saveToInternalStorage(String base64String, Context context) {
        Bitmap bitmap = RNMrzscannerlibModule.decodeBase64(base64String);

        File directory = context.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, ScannerActivity.OVERLAY_IMG_NAME);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return size;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scannerActivityListener = null;
        mrzScanner = null;
    }

    static void setContinuousScanningEnabled(Boolean enabled) {
        continuousScanningEnabled = enabled;
        if (scannerActivityListener != null) {
            scannerActivityListener.setContinuousScanningEnabled(enabled);
        }
    }

    static void setIgnoreDuplicatesEnabled(Boolean enabled) {
        MRZScanner.setIgnoreDuplicates(enabled);
    }

}