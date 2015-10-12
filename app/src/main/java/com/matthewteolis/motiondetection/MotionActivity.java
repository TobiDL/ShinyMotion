package com.matthewteolis.motiondetection;

import android.app.Activity;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MotionActivity extends Activity {

    class FaceCatcher implements Camera.FaceDetectionListener {
        private boolean light = false;
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            SmartThingsResponds st = new SmartThingsResponds();

            if(faces != null && faces.length > 0) {
                for (Camera.Face face : faces) {
                    System.out.println(face.score);
                }
                if (!this.light) {
                    this.light = true;
                    System.out.println("Detected!");
                    st.execute(true);
                } else {
                    System.out.println("The light is already on");
                    System.out.println(faces.toString());
                }
            } else {
                if(this.light) {
                    st.execute(false);
                    this.light = false;
                } else {
                    System.out.println("The light is off...");
                }
            }
        }
    }

    private class SmartThingsResponds extends AsyncTask<Boolean, Void, String> {

        InputStream inputStream = null;
        String mResult = "";

        @Override
        protected String doInBackground(Boolean... params) {
            mResult = turn(params[0]);
            return mResult;
        } // protected String doInBackground(String... params)

        public String turn(boolean turnStatus) {
            HttpURLConnection c = null;
            String event = (turnStatus) ? "on" : "off";
            try {
                URL u = new URL("https://maker.ifttt.com/trigger/" + event + "/with/key/bKp8jzO7rkA7xJ5ReifJis");
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(1000);
                c.setReadTimeout(1000);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        return sb.toString();
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);
        FaceCatcher f = new FaceCatcher();
        Camera camera = getCameraInstance();
        camera.setFaceDetectionListener(f);

        CameraPreview preview = new CameraPreview(this, camera);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(preview);
        camera.startFaceDetection();
    }

    /** A safe way to get an instance of the Came` object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_motion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
