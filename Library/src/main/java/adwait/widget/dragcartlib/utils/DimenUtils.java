package adwait.widget.dragcartlib.utils;

/**
 * Created by adwait on 19/09/17.
 */

public class DimenUtils {
    /***
     * method returns double value of the ratio @Param dimen1:@Param dimen2 as a number between 0 and 1.0
     */
    public static double getRatio(float dimen1,float dimen2){
        return (dimen1/dimen2)<1?dimen1/dimen2:1;
    }
}
