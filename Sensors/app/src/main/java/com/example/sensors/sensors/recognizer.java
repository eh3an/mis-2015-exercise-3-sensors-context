package com.example.sensors.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class recognizer extends ActionBarActivity implements SensorEventListener, SeekBar.OnSeekBarChangeListener {


    private TextView rate,state;
    private SensorManager sm;
    private Sensor accel;
    private Sensor magnet;
    private SeekBar sb;
    private ImageView diagram;
    private FFT fft;
    private ArrayList<Double> x=new ArrayList<Double>();
    private ArrayList<Double> y=new ArrayList<Double>();
    private int seekProgress=5;
    private double[] result;
    private long now,time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizer);
        state=(TextView)findViewById(R.id.tvState);
        rate=(TextView)findViewById(R.id.tvRate);
        diagram=(ImageView)findViewById(R.id.diagram);
        sb=(SeekBar)findViewById(R.id.seekbar);
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        sb.setOnSeekBarChangeListener(this);
        fft=new FFT((int)Math.pow(2,seekProgress));
        if(null==(accel=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))){
            finish();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        //Register Listener
        sm.registerListener(this,accel,SensorManager.SENSOR_DELAY_UI);
        time=System.currentTimeMillis();
    }

    @Override
    public void onPause(){
        sm.unregisterListener(this);
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognizer, menu);
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        rate.setText("Rate(ms): "+String.valueOf(progress+5));
        seekProgress=progress+5;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        fft=new FFT((int)Math.pow(2,seekProgress));
        x=new ArrayList<Double>();
        y=new ArrayList<Double>();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        calcMag(event);
    }
    private void calcMag(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double gx, gy, gz, gt;

            if (x.size() < fft.n) {
                gx = (double) event.values[0];
                gy = (double) event.values[1];
                gz = (double) event.values[2];

                //there is a code in http://developer.android.com/reference/android/hardware/SensorEvent.html
                //but I didn't know if that is the value we want!
                //I used this instead
                gt = gx * gx + gy * gy + gz * gz;
                x.add(gt);
                y.add(0.0);
            }
            if (x.size() == fft.n) {
                runFFT();
                calcState();
                x=new ArrayList<Double>();
                y=new ArrayList<Double>();
            }
        }
    }

    private void calcState() {
        double sum=0, average=0;
        for(int i=0;i<result.length;i++){
            sum+=result[i];
        }
        //using average for deciding the state
        average=sum/result.length;

        //we have tested different states and gathered these approximate values for running, walkin and lying
        // for a more accurate result we can analyze x,y,z acceleration and decide based on them

        if (average<150){
            state.setText("State: Lying ");
        }
        else if(average>350){
            state.setText("State: Running ");
        }
        else if(average>150 && average<350){
            state.setText("State: Walking ");
        }
    }


    private void runFFT() {
        double[] xreal,yimag;
        xreal=new double[fft.n];
        yimag=new double[fft.n];
        result=new double[fft.n];
        //build x
        for (int i=0;i<fft.n;i++){
            xreal[i]=x.get(i);
        }
        //build y
        for (int i=0;i<fft.n;i++){
            yimag[i]=y.get(i);
        }
        fft.fft(xreal,yimag);

        //result
        for (int i=0;i<fft.n;i++){
            result[i]=Math.sqrt(xreal[i]*xreal[i]+yimag[i]*yimag[i]);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
