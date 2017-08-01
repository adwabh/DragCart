package adwait.widget.dragcart;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import adwait.widget.dragcartlib.CirculerRevealView;

import static adwait.widget.dragcart.R.color.colorAccent;

public class MainActivity extends AppCompatActivity {

    private CirculerRevealView mCirculerRevealView;
    private FloatingActionButton fab;
    private boolean expanded;
    private AttachObserver listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mCirculerRevealView = (CirculerRevealView) findViewById(R.id.revealView);
        mCirculerRevealView.setColor(colorAccent);
        int[] location = new int[2];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            listener = new AttachObserver(fab,location);
            fab.getViewTreeObserver().addOnWindowAttachListener(listener);
        }
        mCirculerRevealView.setCenter(location[0],location[1]);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!expanded) {
                    mCirculerRevealView.expand().start();
                    expanded = true;
                } else {
                    mCirculerRevealView.contract().start();
                    expanded = false;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                fab.getViewTreeObserver().removeOnWindowAttachListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class AttachObserver implements ViewTreeObserver.OnWindowAttachListener {
        private int[] mLocation;
        private View mView;

        public AttachObserver(View fab, int[] location) {
            mLocation = location;
            mView = fab;
        }

        @Override
        public void onWindowAttached() {
            mView.getLocationInWindow(mLocation);
        }

        @Override
        public void onWindowDetached() {

        }
    }
}
