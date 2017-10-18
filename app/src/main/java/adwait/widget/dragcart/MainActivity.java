package adwait.widget.dragcart;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.Window;

import adwait.widget.dragcartlib.CircularRevealView;
import adwait.widget.dragcartlib.helper.DragActionListener;
import adwait.widget.dragcartlib.helper.OnStartDragListener;
import adwait.widget.dragcartlib.helper.SimpleItemTouchHelperCallback;
import adwait.widget.dragcartlib.utils.DimenUtils;

import static adwait.widget.dragcart.R.color.colorAccent;

public class MainActivity extends AppCompatActivity implements OnStartDragListener, DragActionListener {

    private CircularRevealView mCircularRevealView;
    private FloatingActionButton fab;
    private boolean expanded;
    private AttachObserver listener;
    private RecyclerView mRecyclerView;
    private ItemTouchHelper mItemTouchHelper;
    private double mFabRatio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mCircularRevealView = (CircularRevealView) findViewById(R.id.revealView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mCircularRevealView.setColor(getResources().getColor(colorAccent));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            fab.getViewTreeObserver().addOnGlobalLayoutListener(new AttachObserver(this, fab,mCircularRevealView));
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!expanded) {
                    mCircularRevealView.expand().start();
                    expanded = true;
                } else {
                    mCircularRevealView.contract().start();
                    expanded = false;
                }
            }
        });
        init(mRecyclerView);
    }

    private void init(RecyclerView recyclerView) {
        final RecyclerListAdapter adapter = new RecyclerListAdapter(this, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        final int spanCount = getResources().getInteger(R.integer.grid_columns);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter, this);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int[] screenLoc = new int[2];
        fab.getLocationOnScreen(screenLoc);
        Log.e("location","top = "+screenLoc[0]+", left = "+screenLoc[1]);
        int[] location = new int[2];
        int centerX = fab.getWidth()/2;//getRelativeLeft(mView)+mView.getWidth()/2;//mView.getX()+mView.getWidth()/2;
        int centerY = fab.getHeight()/2;//getRelativeTop(mView)+mView.getHeight()/2 - mView.getRootView().getTop();//getTitleBarHeight(mActivity) - getStatusBarHeight(mActivity);//mView.getY()+mView.getHeight()/2;
        mCircularRevealView.setCenter(centerX,centerY);
        int cx = mCircularRevealView.getWidth()/2;
        int cy = mCircularRevealView.getHeight()/2;
        float radius = (float) Math.sqrt(cx * cx + cy * cy);
        mFabRatio = DimenUtils.getRatio(radius,DimenUtils.dpTopx(this,fab.getHeight()));
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
        mCircularRevealView.expand(((float)mFabRatio),1f).start();
    }

    @Override
    public void onStopDrag(RecyclerView.ViewHolder viewHolder) {
        mCircularRevealView.contract(1f,((float)mFabRatio)).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class AttachObserver implements ViewTreeObserver.OnWindowAttachListener,ViewTreeObserver.OnGlobalLayoutListener {
        private final CircularRevealView mCircularRevealView;
        private final Activity mActivity;
        private View mView;

        public AttachObserver(Activity activity, View fab, CircularRevealView view) {
            mCircularRevealView = view;
            mView = fab;
            mActivity = activity;
        }

        @Override
        public void onWindowAttached() {

        }

        @Override
        public void onWindowDetached() {

        }

        @Override
        public void onGlobalLayout() {
            int[] location = new int[2];
            mView.getLocationOnScreen(location);
            int centerX = getRelativeLeft(mView)+mView.getWidth()/2;//mView.getX()+mView.getWidth()/2;
            int centerY = location[1] - 160;//getRelativeTop(mView)+mView.getHeight()/2 - mView.getRootView().getTop();//getTitleBarHeight(mActivity) - getStatusBarHeight(mActivity);//mView.getY()+mView.getHeight()/2;
            mCircularRevealView.setCenter(centerX,centerY);

        }
        private int getRelativeLeft(View myView) {
            if (myView.getParent() == myView.getRootView())
                return myView.getLeft();
            else
                return myView.getLeft() + getRelativeLeft((View) myView.getParent());
        }

        private int getRelativeTop(View myView) {
            if (myView.getParent() == myView.getRootView())
                return myView.getTop();
            else
                return myView.getTop() + getRelativeTop((View) myView.getParent());
        }

        private int getStatusBarHeight(Activity activity){
            Rect rectangle = new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int statusBarHeight = rectangle.top;

            return statusBarHeight;

        }

        private int getTitleBarHeight(Activity activity){
            Window window = activity.getWindow();
            int statusBarHeight = getStatusBarHeight(activity);

            int contentViewTop =
                    window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight= contentViewTop - statusBarHeight;
            return titleBarHeight;
        }
    }


}
