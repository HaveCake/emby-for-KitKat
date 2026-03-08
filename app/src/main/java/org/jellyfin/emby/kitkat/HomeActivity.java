package org.jellyfin.emby.kitkat;

import android.app.Activity;
import android.os.Bundle;

/**
 * Leanback 海报墙主界面 Activity。
 * <p>
 * 继承原生 {@link Activity}（非 AppCompatActivity），加载 {@link HomeFragment}
 * 以展示 Leanback BrowseFragment 风格的电视端海报墙。
 */
public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 动态加载 HomeFragment 到默认容器
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new HomeFragment())
                    .commit();
        }
    }
}
