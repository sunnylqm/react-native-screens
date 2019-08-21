package com.swmansion.rnscreens;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.PointerEvents;
import com.facebook.react.uimanager.ReactPointerEventsView;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

public class Screen extends ViewGroup implements ReactPointerEventsView {

  public static class ScreenFragment extends Fragment {

    private Screen mScreenView;

    public ScreenFragment() {
      throw new IllegalStateException("Screen fragments should never be restored");
    }

    @SuppressLint("ValidFragment")
    public ScreenFragment(Screen screenView) {
      super();
      mScreenView = screenView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
      return mScreenView;
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      mScreenView.onDestroy();
    }

    @Override
    public void onDetach() {
      super.onDetach();
      mScreenView.onDetach();
    }
  }

  private final Fragment mFragment;
  private final EventDispatcher mEventDispatcher;
  private @Nullable ScreenContainer mContainer;
  private boolean mActive;
  private boolean mTransitioning;

  public Screen(ReactContext context) {
    super(context);
    mFragment = new ScreenFragment(this);
    mEventDispatcher = context.getNativeModule(UIManagerModule.class).getEventDispatcher();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int b, int r) {
    // no-op
  }

  private void onDetach() {
    clearDisappearingChildren();
  }

  private void onDestroy() {
    mEventDispatcher.dispatchEvent(new ScreenDismissedEvent(getId()));
  }

  /**
   * While transitioning this property allows to optimize rendering behavior on Android and provide
   * a correct blending options for the animated screen. It is turned on automatically by the container
   * when transitioning is detected and turned off immediately after
   */
  public void setTransitioning(boolean transitioning) {
    if (mTransitioning == transitioning) {
      return;
    }
    mTransitioning = transitioning;
    super.setLayerType(transitioning ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE, null);
  }

  @Override
  public PointerEvents getPointerEvents() {
    return mTransitioning ? PointerEvents.NONE : PointerEvents.AUTO;
  }

  @Override
  public void setLayerType(int layerType, @Nullable Paint paint) {
    // ignore - layer type is controlled by `transitioning` prop
  }

  protected void setContainer(@Nullable ScreenContainer mContainer) {
    this.mContainer = mContainer;
  }

  protected @Nullable ScreenContainer getContainer() {
    return mContainer;
  }

  protected Fragment getFragment() {
    return mFragment;
  }

  public void setActive(boolean active) {
    if (active == mActive) {
      return;
    }
    mActive = active;
    if (mContainer != null) {
      mContainer.notifyChildUpdate();
    }
  }

  public boolean isActive() {
    return mActive;
  }
}
