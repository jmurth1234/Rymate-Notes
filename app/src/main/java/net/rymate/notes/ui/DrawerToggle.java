package net.rymate.notes.ui;


import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.rymate.notes.R;

import java.lang.reflect.Method;

/**
 * This class provides a handy way to tie together the functionality of
 * {@link DrawerLayout} and the framework <code>ActionBar</code> to implement the recommended
 * design for navigation drawers.
 *
 * <p>To use <code>ActionBarDrawerToggle</code>, create one in your Activity and call through
 * to the following methods corresponding to your Activity callbacks:</p>
 *
 * <ul>
 * <li>{@link Activity#onConfigurationChanged(android.content.res.Configuration) onConfigurationChanged}</li>
 * <li>{@link Activity#onOptionsItemSelected(android.view.MenuItem) onOptionsItemSelected}</li>
 * </ul>
 *
 * <p>Call {@link #syncState()} from your <code>Activity</code>'s
 * {@link Activity#onPostCreate(android.os.Bundle) onPostCreate} to synchronize the indicator
 * with the state of the linked DrawerLayout after <code>onRestoreInstanceState</code>
 * has occurred.</p>
 *
 * <p><code>ActionBarDrawerToggle</code> can be used directly as a
 * {@link DrawerLayout.DrawerListener}, or if you are already providing your own listener,
 * call through to each of the listener methods from your own.</p>
 */

public class DrawerToggle implements DrawerLayout.DrawerListener {

    /**
     * Allows an implementing Activity to return an {@link DrawerToggle.Delegate} to use
     * with ActionBarDrawerToggle.
     */
    public interface DelegateProvider {

        /**
         * @return Delegate to use for ActionBarDrawableToggles, or null if the Activity
         *         does not wish to override the default behavior.
         */
        Delegate getDrawerToggleDelegate();
    }

    public interface Delegate {
        /**
         * @return Up indicator drawable as defined in the Activity's theme, or null if one is not
         *         defined.
         */
        Drawable getThemeUpIndicator();

        /**
         * Set the Action Bar's up indicator drawable and content description.
         *
         * @param upDrawable     - Drawable to set as up indicator
         * @param contentDescRes - Content description to set
         */
        void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes);

        /**
         * Set the Action Bar's up indicator content description.
         *
         * @param contentDescRes - Content description to set
         */
        void setActionBarDescription(int contentDescRes);
    }

    private static class ActionBarDrawerToggleImpl {
        private static final int[] THEME_ATTRS = new int[] {
                android.R.attr.homeAsUpIndicator
        };
        private static final String TAG = "ActionBarDrawerToggleHoneycomb";

        public Drawable getThemeUpIndicator(Activity activity) {
            final TypedArray a = activity.obtainStyledAttributes(THEME_ATTRS);
            final Drawable result = a.getDrawable(0);
            a.recycle();
            return result;
        }

        public Object setActionBarUpIndicator(Object info, Activity activity,
                                              Drawable themeImage, int contentDescRes) {
            final Activity actionBarActivity = (Activity) activity;
            if (info == null) {
                info = new SetIndicatorInfo(activity);
            }
            final SetIndicatorInfo sii = (SetIndicatorInfo) info;
            if (sii.setHomeAsUpIndicator != null) {
                try {
                    final ActionBar actionBar = actionBarActivity.getActionBar();
                    sii.setHomeAsUpIndicator.invoke(actionBar, themeImage);
                    sii.setHomeActionContentDescription.invoke(actionBar, contentDescRes);
                } catch (Exception e) {
                    Log.w(TAG, "Couldn't set home-as-up indicator via JB-MR2 API", e);
                }
            } else if (sii.upIndicatorView != null) {
                sii.upIndicatorView.setImageDrawable(themeImage);
            } else {
                Log.w(TAG, "Couldn't set home-as-up indicator");
            }
            return info;
        }

        public Object setActionBarDescription(Object info, Activity activity, int contentDescRes) {
            final Activity actionBarActivity = (Activity) activity;
            if (info == null) {
                info = new SetIndicatorInfo(activity);
            }
            final SetIndicatorInfo sii = (SetIndicatorInfo) info;
            if (sii.setHomeAsUpIndicator != null) {
                try {
                    final ActionBar actionBar = actionBarActivity.getActionBar();
                    sii.setHomeActionContentDescription.invoke(actionBar, contentDescRes);
                } catch (Exception e) {
                    Log.w(TAG, "Couldn't set content description via JB-MR2 API", e);
                }
            }
            return info;
        }
    }

    private static class SetIndicatorInfo {
        public Method setHomeAsUpIndicator;
        public Method setHomeActionContentDescription;
        public ImageView upIndicatorView;

        SetIndicatorInfo(Activity activity) {
            try {
                setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod("setHomeAsUpIndicator",
                        Drawable.class);
                setHomeActionContentDescription = ActionBar.class.getDeclaredMethod(
                        "setHomeActionContentDescription", Integer.TYPE);

                // If we got the method we won't need the stuff below.
                return;
            } catch (NoSuchMethodException e) {
                // Oh well. We'll use the other mechanism below instead.
            }

            final int upId = android.R.id.home;

            final View home = activity.findViewById(upId);
            if (home == null) {
                // Action bar doesn't have a known configuration, an OEM messed with things.
                return;
            }

            final ViewGroup parent = (ViewGroup) home.getParent();
            final int childCount = parent.getChildCount();
            if (childCount != 2) {
                // No idea which one will be the right one, an OEM messed with things.
                return;
            }

            final View first = parent.getChildAt(0);
            final View second = parent.getChildAt(1);
            final View up = first.getId() == upId ? second : first;

            if (up instanceof ImageView) {
                // Jackpot! (Probably...)
                upIndicatorView = (ImageView) up;
            }
        }
    }

    private static final ActionBarDrawerToggleImpl IMPL;

    static {IMPL = new ActionBarDrawerToggleImpl();
    }

    // android.R.id.home as defined by public API in v11
    private static final int ID_HOME = 0x0102002c;

    private final Activity mActivity;
    private final Delegate mActivityImpl;
    private final DrawerLayout mDrawerLayout;
    private boolean mDrawerIndicatorEnabled = true;

    private Drawable mThemeImage;
    private Drawable mDrawerImage;
    private SlideDrawable mSlider;
    private final int mDrawerImageResource;
    private final int mOpenDrawerContentDescRes;
    private final int mCloseDrawerContentDescRes;

    private Object mSetIndicatorInfo;

    /**
     * Construct a new ActionBarDrawerToggle.
     *
     * <p>The given {@link Activity} will be linked to the specified {@link DrawerLayout}.
     * The provided drawer indicator drawable will animate slightly off-screen as the drawer
     * is opened, indicating that in the open state the drawer will move off-screen when pressed
     * and in the closed state the drawer will move on-screen when pressed.</p>
     *
     * <p>String resources must be provided to describe the open/close drawer actions for
     * accessibility services.</p>
     *
     * @param activity The Activity hosting the drawer
     * @param drawerLayout The DrawerLayout to link to the given Activity's ActionBar
     * @param drawerImageRes A Drawable resource to use as the drawer indicator
     * @param openDrawerContentDescRes A String resource to describe the "open drawer" action
     *                                 for accessibility
     * @param closeDrawerContentDescRes A String resource to describe the "close drawer" action
     *                                  for accessibility
     */
    public DrawerToggle(Activity activity, DrawerLayout drawerLayout,
                        int drawerImageRes, int openDrawerContentDescRes,
                        int closeDrawerContentDescRes) {
        mActivity = activity;
        mDrawerLayout = drawerLayout;
        mDrawerImageResource = drawerImageRes;
        mOpenDrawerContentDescRes = openDrawerContentDescRes;
        mCloseDrawerContentDescRes = closeDrawerContentDescRes;

        mThemeImage = getThemeUpIndicator();
        mDrawerImage = activity.getResources().getDrawable(drawerImageRes);
        mSlider = new SlideDrawable(mDrawerImage);
        mSlider.setOffsetBy(1.f / 3);

        // Allow the Activity to provide an impl
        if (activity instanceof DelegateProvider) {
            mActivityImpl = ((DelegateProvider) activity).getDrawerToggleDelegate();
        } else {
            mActivityImpl = null;
        }
    }

    /**
     * Synchronize the state of the drawer indicator/affordance with the linked DrawerLayout.
     *
     * <p>This should be called from your <code>Activity</code>'s
     * {@link Activity#onPostCreate(android.os.Bundle) onPostCreate} method to synchronize after
     * the DrawerLayout's instance state has been restored, and any other time when the state
     * may have diverged in such a way that the ActionBarDrawerToggle was not notified.
     * (For example, if you stop forwarding appropriate drawer events for a period of time.)</p>
     */
    /**
     * Synchronize the state of the drawer indicator/affordance with the linked DrawerLayout.
     *
     * <p>This should be called from your <code>Activity</code>'s
     * {@link Activity#onPostCreate(android.os.Bundle) onPostCreate} method to synchronize after
     * the DrawerLayout's instance state has been restored, and any other time when the state
     * may have diverged in such a way that the ActionBarDrawerToggle was not notified.
     * (For example, if you stop forwarding appropriate drawer events for a period of time.)</p>
     */
    public void syncState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mSlider.setOffset(1.f);
        } else {
            mSlider.setOffset(0.f);
        }

        if (mDrawerIndicatorEnabled) {
            setActionBarUpIndicator(mSlider, mDrawerLayout.isDrawerOpen(GravityCompat.START) ?
                    mOpenDrawerContentDescRes : mCloseDrawerContentDescRes);
        }
    }

    /**
     * Enable or disable the drawer indicator. The indicator defaults to enabled.
     *
     * <p>When the indicator is disabled, the <code>ActionBar</code> will revert to displaying
     * the home-as-up indicator provided by the <code>Activity</code>'s theme in the
     * <code>android.R.attr.homeAsUpIndicator</code> attribute instead of the animated
     * drawer glyph.</p>
     *
     * @param enable true to enable, false to disable
     */
    public void setDrawerIndicatorEnabled(boolean enable) {
        if (enable != mDrawerIndicatorEnabled) {
            if (enable) {
                setActionBarUpIndicator(mSlider, mDrawerLayout.isDrawerOpen(GravityCompat.START) ?
                        mOpenDrawerContentDescRes : mCloseDrawerContentDescRes);
            } else {
                setActionBarUpIndicator(mThemeImage, 0);
            }
            mDrawerIndicatorEnabled = enable;
        }
    }

    /**
     * @return true if the enhanced drawer indicator is enabled, false otherwise
     * @see #setDrawerIndicatorEnabled(boolean)
     */
    public boolean isDrawerIndicatorEnabled() {
        return mDrawerIndicatorEnabled;
    }

    /**
     * This method should always be called by your <code>Activity</code>'s
     * {@link Activity#onConfigurationChanged(android.content.res.Configuration) onConfigurationChanged}
     * method.
     *
     * @param newConfig The new configuration
     */
    public void onConfigurationChanged(Configuration newConfig) {
        // Reload drawables that can change with configuration
        mThemeImage = getThemeUpIndicator();
        mDrawerImage = mActivity.getResources().getDrawable(mDrawerImageResource);
        syncState();
    }

    /**
     * This method should be called by your <code>Activity</code>'s
     * {@link Activity#onOptionsItemSelected(android.view.MenuItem) onOptionsItemSelected} method.
     * If it returns true, your <code>onOptionsItemSelected</code> method should return true and
     * skip further processing.
     *
     * @param item the MenuItem instance representing the selected menu item
     * @return true if the event was handled and further processing should not occur
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == ID_HOME && mDrawerIndicatorEnabled) {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return false;
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param drawerView The child view that was moved
     * @param slideOffset The new offset of this drawer within its range, from 0-1
     */
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        float glyphOffset = mSlider.getOffset();
        if (slideOffset > 0.5f) {
            glyphOffset = Math.max(glyphOffset, Math.max(0.f, slideOffset - 0.5f) * 2);
        } else {
            glyphOffset = Math.min(glyphOffset, slideOffset * 2);
        }
        mSlider.setOffset(glyphOffset);
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param drawerView Drawer view that is now open
     */
    @Override
    public void onDrawerOpened(View drawerView) {
        mSlider.setOffset(1.f);
        if (mDrawerIndicatorEnabled) {
            setActionBarDescription(mOpenDrawerContentDescRes);
        }
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param drawerView Drawer view that is now closed
     */
    @Override
    public void onDrawerClosed(View drawerView) {
        mSlider.setOffset(0.f);
        if (mDrawerIndicatorEnabled) {
            setActionBarDescription(mCloseDrawerContentDescRes);
        }
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param newState The new drawer motion state
     */
    @Override
    public void onDrawerStateChanged(int newState) {
    }

    Drawable getThemeUpIndicator() {
        if (mActivityImpl != null) {
            return mActivityImpl.getThemeUpIndicator();
        }
        return IMPL.getThemeUpIndicator(mActivity);
    }

    void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
        if (mActivityImpl != null) {
            mActivityImpl.setActionBarUpIndicator(upDrawable, contentDescRes);
            return;
        }
        mSetIndicatorInfo = IMPL
                .setActionBarUpIndicator(mSetIndicatorInfo, mActivity, upDrawable, contentDescRes);
    }

    void setActionBarDescription(int contentDescRes) {
        if (mActivityImpl != null) {
            mActivityImpl.setActionBarDescription(contentDescRes);
            return;
        }
        mSetIndicatorInfo = IMPL
                .setActionBarDescription(mSetIndicatorInfo, mActivity, contentDescRes);
    }

    private static class SlideDrawable extends Drawable implements Drawable.Callback {
        private Drawable mWrapped;
        private float mOffset;
        private float mOffsetBy;

        private final Rect mTmpRect = new Rect();

        public SlideDrawable(Drawable wrapped) {
            mWrapped = wrapped;
        }

        public void setOffset(float offset) {
            mOffset = offset;
            invalidateSelf();
        }

        public float getOffset() {
            return mOffset;
        }

        public void setOffsetBy(float offsetBy) {
            mOffsetBy = offsetBy;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            mWrapped.copyBounds(mTmpRect);
            canvas.save();
            canvas.translate(mOffsetBy * mTmpRect.width() * -mOffset, 0);
            mWrapped.draw(canvas);
            canvas.restore();
        }

        @Override
        public void setChangingConfigurations(int configs) {
            mWrapped.setChangingConfigurations(configs);
        }

        @Override
        public int getChangingConfigurations() {
            return mWrapped.getChangingConfigurations();
        }

        @Override
        public void setDither(boolean dither) {
            mWrapped.setDither(dither);
        }

        @Override
        public void setFilterBitmap(boolean filter) {
            mWrapped.setFilterBitmap(filter);
        }

        @Override
        public void setAlpha(int alpha) {
            mWrapped.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mWrapped.setColorFilter(cf);
        }

        @Override
        public void setColorFilter(int color, PorterDuff.Mode mode) {
            mWrapped.setColorFilter(color, mode);
        }

        @Override
        public void clearColorFilter() {
            mWrapped.clearColorFilter();
        }

        @Override
        public boolean isStateful() {
            return mWrapped.isStateful();
        }

        @Override
        public boolean setState(int[] stateSet) {
            return mWrapped.setState(stateSet);
        }

        @Override
        public int[] getState() {
            return mWrapped.getState();
        }

        @Override
        public Drawable getCurrent() {
            return mWrapped.getCurrent();
        }

        @Override
        public boolean setVisible(boolean visible, boolean restart) {
            return super.setVisible(visible, restart);
        }

        @Override
        public int getOpacity() {
            return mWrapped.getOpacity();
        }

        @Override
        public Region getTransparentRegion() {
            return mWrapped.getTransparentRegion();
        }

        @Override
        protected boolean onStateChange(int[] state) {
            mWrapped.setState(state);
            return super.onStateChange(state);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mWrapped.setBounds(bounds);
        }

        @Override
        public int getIntrinsicWidth() {
            return mWrapped.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mWrapped.getIntrinsicHeight();
        }

        @Override
        public int getMinimumWidth() {
            return mWrapped.getMinimumWidth();
        }

        @Override
        public int getMinimumHeight() {
            return mWrapped.getMinimumHeight();
        }

        @Override
        public boolean getPadding(Rect padding) {
            return mWrapped.getPadding(padding);
        }

        @Override
        public ConstantState getConstantState() {
            return super.getConstantState();
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            if (who == mWrapped) {
                invalidateSelf();
            }
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            if (who == mWrapped) {
                scheduleSelf(what, when);
            }
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            if (who == mWrapped) {
                unscheduleSelf(what);
            }
        }
    }
}