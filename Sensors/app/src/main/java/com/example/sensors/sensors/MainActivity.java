package com.example.sensors.sensors;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements SensorEventListener, SeekBar.OnSeekBarChangeListener {



    private TextView x,y,z,g,rate;
    private SensorManager sm;
    private Sensor accel;
    private Sensor magnet;
    private SeekBar sb;
    private long update,time;
    private ImageView diagram;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x=(TextView)findViewById(R.id.tvX);
        y=(TextView)findViewById(R.id.tvY);
        z=(TextView)findViewById(R.id.tvZ);
        g=(TextView)findViewById(R.id.tvG);
        rate=(TextView)findViewById(R.id.tvRate);
        diagram=(ImageView)findViewById(R.id.diagram);
        sb=(SeekBar)findViewById(R.id.seekbar);
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        sb.setOnSeekBarChangeListener(this);

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onSensorChanged(SensorEvent event) {
        calcMag(event);
    }

    private void calcMag(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            long now=System.currentTimeMillis();
            if(now-time>update){
                x.setText("X: "+String.valueOf(event.values[0]));
                y.setText("Y: "+String.valueOf(event.values[1]));
                z.setText("Z: "+String.valueOf(event.values[2]));
                double gx,gy,gz,gt;
                gx=(double)event.values[0];
                gy=(double)event.values[1];
                gz=(double)event.values[2];

                //there is a code in http://developer.android.com/reference/android/hardware/SensorEvent.html
                //but I didn't know if that is the value we want!
                //I used this instead
                gt=gx*gx+gy*gy+gz*gz;
                g.setText("G: " + gt);
                //Drawing Part
                Bitmap b = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                Paint paint=new Paint();
                Canvas canvas=new Canvas(b);
                float scale= (float) (diagram.getWidth()/gt-0.2);
                paint.setColor(Color.RED);
                canvas.drawLine(0, 20, (float)gx*scale, 20, paint);
                paint.setColor(Color.GREEN);
                canvas.drawLine(0, 50, (float)gy*scale, 50, paint);
                paint.setColor(Color.BLUE);
                canvas.drawLine(0, 80, (float)gz*scale, 80, paint);
                paint.setColor(Color.WHITE);
                canvas.drawLine(0, 90, (float)gt*scale, 90, paint);
                diagram.setImageBitmap(b);
                time=now;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        update=progress;
        rate.setText("Rate(ms): "+String.valueOf(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void fftClick(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, fftVis.class);
        startActivity(intent);
    }
    public void recogClick(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, recognizer.class);
        startActivity(intent);
    }
}
