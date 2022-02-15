package com.reactlibrary;

interface ScannerActivityListener {
    void resumeScanning();

    void closeScanner();

    void setContinuousScanningEnabled(Boolean enabled);
}