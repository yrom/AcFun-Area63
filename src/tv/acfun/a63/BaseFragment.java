package tv.acfun.a63;

import com.umeng.analytics.MobclickAgent;

import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getPageTitle());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getPageTitle());
    }

    protected String getPageTitle() {
        return this.getClass().getSimpleName();
    }
    
}
