package adwait.widget.dragcartlib.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by adwait on 19/09/17.
 */

public class DimenUtils {
    /***
     * method returns double value of the ratio @Param dimen1:@Param dimen2 as a number between 0 and 1.0
     */
    public static double getRatio(float from,float to){
        return to/from;
    }

    public static float dpTopx(Context context, float dp){
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
    }
}
