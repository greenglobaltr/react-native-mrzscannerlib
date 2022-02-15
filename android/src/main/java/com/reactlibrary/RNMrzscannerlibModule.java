
package com.reactlibrary;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.scansolutions.mrzscannerlib.MRZResultModel;
import com.scansolutions.mrzscannerlib.MRZScanner;
import com.scansolutions.mrzscannerlib.MRZScannerListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.reactlibrary.ScannerActivity.continuousScanningEnabled;
import static com.reactlibrary.ScannerActivity.scannerType;

enum DocumentReturnType {
    BASE_64,
    FILE_STORAGE
}

public class RNMrzscannerlibModule extends ReactContextBaseJavaModule implements MRZScannerListener, MRZScannerDismissedListener {

    public static int activeThreads = 0;
    DocumentReturnType documentReturnType = DocumentReturnType.BASE_64;
    private MRZScanner mrzScanner;

    public RNMrzscannerlibModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void resumeScanner() {
        if (ScannerActivity.scannerActivityListener != null) {
            ScannerActivity.scannerActivityListener.resumeScanning();
        }
    }

    @ReactMethod
    public void setShowFlashButton(Boolean active) {
        ScannerActivity.setShowFlashButton(active);
    }

    @ReactMethod
    public void setShowCloseButton(Boolean active) {
    }

    @ReactMethod
    public void closeScanner() {
        if (ScannerActivity.scannerActivityListener != null) {
            ScannerActivity.scannerActivityListener.closeScanner();
        } else if (mrzScanner != null) {
            closeMRZFragment();
        }
    }

    @ReactMethod
    public void scanFromGallery() {
        Activity mContext = getCurrentActivity();
        MRZScanner.scanFromGallery(mContext, RNMrzscannerlibModule.this);
    }

    @ReactMethod
    public void setDocumentImageReturnType(Integer type) {
        documentReturnType = DocumentReturnType.values()[type];
    }

    @ReactMethod
    public void setContinuousScanningEnabled(Boolean enabled) {
        ScannerActivity.setContinuousScanningEnabled(enabled);
        if (mrzScanner != null) {
            mrzScanner.setContinuousScanningEnabled(continuousScanningEnabled);
        }
    }

    @ReactMethod
    public void setIgnoreDuplicatesEnabled(Boolean enabled) {
        ScannerActivity.setIgnoreDuplicatesEnabled(enabled);
    }

    @ReactMethod
    public void scanImage(final String base64String) {
        if (activeThreads < 2) {
            activeThreads++;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap image = decodeBase64(base64String);
                    MRZScanner.scanBitmapReactNative(image, getCurrentActivity(), RNMrzscannerlibModule.this);
                    activeThreads--;
                }
            }).start();
        }
    }

    @ReactMethod
    public void setVibrateOnSuccessfulScan(Boolean active) {
        ScannerActivity.setVibrateOnSuccessfulScan(active);
    }

    @ReactMethod
    public String getSdkVersion() {
        return MRZScanner.sdkVersion();
    }

    @ReactMethod
    public void scanBitmap(String base64string) {
        Bitmap bitmap = decodeBase64(base64string);
        MRZScanner.scanBitmap(bitmap, getCurrentActivity(), this);
    }

    @ReactMethod
    public void startScannerWithCustomOverlay(String base64string) {
        ScannerActivity.startScanner(getCurrentActivity(), this, base64string, this);
    }

    @ReactMethod
    public void startScanner() {
        ScannerActivity.startScanner(getCurrentActivity(), this, null, this);
    }

    @ReactMethod
    public void startPartialViewScanner(final int x, final int y, final int width, final int height) {
        if (x == 0 && y == 0 && width == 1 && height == 1) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Point displaySize = getDisplaySize(getCurrentActivity());
                    int scaledWidth = (int) (displaySize.x * (width / 100.0f));
                    int scaledHeight = (int) (displaySize.y * (height / 100.0f));
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(scaledWidth, scaledHeight);
                    layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                    layoutParams.leftMargin = (int) (displaySize.x * (x / 100.0f));
                    layoutParams.topMargin = (int) (displaySize.y * (y / 100.0f));

                    FrameLayout frameLayout = new FrameLayout(getCurrentActivity());
                    frameLayout.setId(R.id.mrz_fragment_id);
                    getCurrentActivity().addContentView(frameLayout, layoutParams);

                    FragmentTransaction fragmentTransaction = getCurrentActivity().getFragmentManager().beginTransaction();

                    mrzScanner = new MRZScanner();
                    mrzScanner.scannerListener = RNMrzscannerlibModule.this;

                    MRZScanner.setExtractPortraitEnabled(ScannerActivity.extractPortrait);
                    MRZScanner.setExtractFullPassportImageEnabled(ScannerActivity.extractPassportFullImage);
                    MRZScanner.setExtractSignatureEnabled(ScannerActivity.extractSignature);
                    MRZScanner.setExtractPortraitEnabled(ScannerActivity.extractPortrait);
                    MRZScanner.setExtractIdBackImageEnabled(ScannerActivity.extractIdBack);

                    fragmentTransaction.add(frameLayout.getId(), mrzScanner, "MRZ_FRAGMENT");
                    fragmentTransaction.commit();

                    mrzScanner.setContinuousScanningEnabled(continuousScanningEnabled);
                    mrzScanner.hidePreview();
                }
            });
        } else {
            ScannerActivity.startScanner(getCurrentActivity(), this, null, this, new RectF(x, y, width, height));
        }
    }

    @ReactMethod
    public int registerWithLicenseKey(String keyString) {
        return MRZScanner.registerWithLicenseKey(getCurrentActivity(), keyString);
    }

    @ReactMethod
    public void toggleFlash(Boolean active) {
        ScannerActivity.flashToggleActivated = active;
        Activity scannerActivity = getCurrentActivity();
        if (scannerActivity instanceof ScannerActivity) {
            ((ScannerActivity) scannerActivity).toggleFlash(active);
        }
    }

    @ReactMethod
    public void setExtractPortraitEnabled(Boolean active) {
        ScannerActivity.setExtractPortraitEnabled(active);
    }

    @ReactMethod
    public void setExtractIdBackEnabled(Boolean active) {
        ScannerActivity.setExtractIdBackImageEnabled(active);
    }

    @ReactMethod
    public void setExtractSignatureEnabled(Boolean active) {
        ScannerActivity.setExtractSignatureEnabled(active);
    }

    @ReactMethod
    public void setExtractFullPassportImageEnabled(Boolean active) {
        ScannerActivity.setExtractFullPassportImageEnabled(active);
    }

    @ReactMethod
    void setScannerType(int type) {
        scannerType = type;
    }

    @ReactMethod
    public void setPassportActive(boolean passportActive) {
        MRZScanner.setPassportActive(passportActive);
    }

    @ReactMethod
    public void setIDActive(boolean idActive) {
        MRZScanner.setIDActive(idActive);
    }

    @ReactMethod
    public void setVisaActive(boolean visaActive) {
        MRZScanner.setVisaActive(visaActive);
    }

    @ReactMethod
    public void setMaxThreads(int maxThreads) {
        MRZScanner.setMaxThreads(maxThreads);
    }

    @Override
    public String getName() {
        return "RNMrzscannerlib";
    }

    private String convertMapToB64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    static Bitmap decodeBase64(String string) {
        byte[] decodedString = Base64.decode(string, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public void successfulScanWithResult(MRZResultModel mrzResultModel) {
        WritableMap body = Arguments.createMap();
        body.putString("document_type_raw", mrzResultModel.documentTypeRaw);
        body.putString("document_type_readable", mrzResultModel.documentTypeReadable);
        body.putString("issuing_country", mrzResultModel.issuingCountry);
        body.putString("surname", mrzResultModel.surnamesReadable());
        body.putString("document_number", mrzResultModel.documentNumber);
        body.putString("nationality", mrzResultModel.nationality);
        body.putString("dob_raw", mrzResultModel.dobRaw);
        body.putString("dob_readable", mrzResultModel.dobReadable);
        body.putString("sex", mrzResultModel.sex);
        body.putString("est_issuing_date_raw", mrzResultModel.estIssuingDateRaw);
        body.putString("est_issuing_date_readable", mrzResultModel.estIssuingDateReadable);
        body.putString("expiration_date_raw", mrzResultModel.expirationDateRaw);
        body.putString("expiration_date_readable", mrzResultModel.expirationDateReadable);
        body.putString("given_names_readable", mrzResultModel.givenNamesReadable());
        body.putString("optionals", mrzResultModel.optionalsReadable());
        body.putString("raw_result", mrzResultModel.rawResult);

        if (documentReturnType == DocumentReturnType.FILE_STORAGE) {
            long timestamp = System.currentTimeMillis();
            if (mrzResultModel.idBack != null) {
                body.putString("idBackImagePath", saveAsFile(mrzResultModel.idBack, "idBack" + timestamp));
            }

            if (mrzResultModel.idFront != null) {
                body.putString("idFrontImagePath", saveAsFile(mrzResultModel.idFront, "idFront" + timestamp));
            }

            if (mrzResultModel.portrait != null) {
                body.putString("portraitImagePath", saveAsFile(mrzResultModel.portrait, "portrait" + timestamp));
            }

            if (mrzResultModel.signature != null) {
                body.putString("signatureImagePath", saveAsFile(mrzResultModel.signature, "signature" + timestamp));
            }

            if (mrzResultModel.fullImage != null) {
                body.putString("passportImagePath", saveAsFile(mrzResultModel.fullImage, "passport" + timestamp));
            }
        } else {
            if (mrzResultModel.idBack != null) {
                body.putString("idBack", convertMapToB64(mrzResultModel.idBack));
            }

            if (mrzResultModel.idFront != null) {
                body.putString("idFront", convertMapToB64(mrzResultModel.idFront));
            }

            if (mrzResultModel.portrait != null) {
                body.putString("portrait", convertMapToB64(mrzResultModel.portrait));
            }

            if (mrzResultModel.signature != null) {
                body.putString("signature", convertMapToB64(mrzResultModel.signature));
            }

            if (mrzResultModel.fullImage != null) {
                body.putString("passportImage", convertMapToB64(mrzResultModel.fullImage));
            }
        }
        if (mrzScanner != null && !continuousScanningEnabled) {
            closeMRZFragment();
        }

        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("successfulScanEmittedEvent", body);
    }

    private void closeMRZFragment() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getCurrentActivity().getFragmentManager().popBackStack();
                FrameLayout frameLayout = getCurrentActivity().findViewById(R.id.mrz_fragment_id);
                if (frameLayout != null) {
                    ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                }
            }
        });
    }

    @Override
    public void successfulScanWithDocumentImage(Bitmap bitmap) {
        String result = "";
        if (documentReturnType == DocumentReturnType.FILE_STORAGE) {
            long timestamp = System.currentTimeMillis();
            if (bitmap != null) {
                result = saveAsFile(bitmap, "fullImage" + timestamp);
            }
        } else {
            result = convertMapToB64(bitmap);
        }
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("successfulDocumentScanEmittedEvent", result);
    }

    @Override
    public void successfulIdFrontImageScan(Bitmap bitmap, Bitmap bitmap1) {
        WritableMap body = Arguments.createMap();

        if (documentReturnType == DocumentReturnType.FILE_STORAGE) {
            long timestamp = System.currentTimeMillis();
            if (bitmap != null) {
                body.putString("fullImagePath", saveAsFile(bitmap, "fullImage" + timestamp));
            }

            if (bitmap1 != null) {
                body.putString("portraitImagePath", saveAsFile(bitmap1, "portraitImage" + timestamp));
            }
        } else {
            if (bitmap != null) {
                body.putString("fullImage", convertMapToB64(bitmap));
            }

            if (bitmap1 != null) {
                body.putString("portrait", convertMapToB64(bitmap1));
            }
        }

        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("successfulIdFrontScanEmittedEvent", body);
    }

    @Override
    public void scanImageFailed() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("scanImageFailed", "");
    }


    private String saveAsFile(Bitmap bitmap, String name) {
        File directory = getCurrentActivity().getFilesDir();
        File mrzPath = new File(directory, "mrzimages");
        mrzPath.mkdir();
        // Create imageDir
        File mypath = new File(mrzPath, name + ".png");

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
        return mypath.getAbsolutePath();
    }


    @Override
    public void permissionsWereDenied() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("permissionsWereDenied", "");
    }

    @Override
    public void scannerWasDismissed() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("scannerWasDismissedEmittedEvent", "");
    }

    private static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return size;
    }

}