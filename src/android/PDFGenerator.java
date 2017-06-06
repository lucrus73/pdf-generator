package com.pdf.generator;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class echoes a string called from JavaScript.
 */
public class PDFGenerator extends CordovaPlugin {

    private final static String APPNAME = "PDFGenerator";
    private WebView offscreenWebview = null;

    public WebView getOffscreenWebkitInstance(Context ctx) {
        LOG.i(APPNAME, "Mounting offscreen webview");
        if (this.offscreenWebview == null)
            return this.offscreenWebview = new WebView(ctx);
        else
            return this.offscreenWebview;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("htmlToPDF")) {
            this.pdfPrinter(args, callbackContext);

            return true;
        }
        return false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

    private void pdfPrinter(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final Context ctx = this.cordova.getActivity().getApplicationContext();
        final CordovaInterface _cordova = this.cordova;
        final CallbackContext cordovaCallback = callbackContext;

        _cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    WebView webview = getOffscreenWebkitInstance(ctx);
                    webview.getSettings().setJavaScriptEnabled(true);
                    PrintManager printManager = (PrintManager) cordova.getActivity()
                            .getSystemService(Context.PRINT_SERVICE);

                    boolean outputBase64 = args.getString(4) != null && args.getString(4).equals("base64");
                    PDFPrinterWebView printerWebView = new PDFPrinterWebView(printManager, ctx, outputBase64);

                    String fileNameArg = args.getString(5);
                    if (fileNameArg != null) {
                        printerWebView.setFileName(fileNameArg);
                    }

                    printerWebView.setCordovaCallback(cordovaCallback);
                    webview.setWebViewClient(printerWebView);

                    if (args.getString(0) != null && !args.getString(0).equals("null"))
                        webview.loadUrl(args.getString(0));

                    if (args.getString(1) != null && !args.getString(1).equals("null"))
                        webview.loadData(args.getString(1), "text/html; charset=UTF-8", null);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(APPNAME, e.getMessage());
                    cordovaCallback.error("Native pasing arguments: " + e.getMessage());
                }
            }
        });
    }

}
