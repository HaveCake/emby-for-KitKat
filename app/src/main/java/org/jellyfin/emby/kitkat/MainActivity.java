package org.jellyfin.emby.kitkat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;

import org.jellyfin.emby.kitkat.model.LoginRequest;
import org.jellyfin.emby.kitkat.model.LoginResponse;
import org.jellyfin.emby.kitkat.network.NetworkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 极简登录测试界面。
 * <p>
 * 用于在 Android 4.4 电视盒子上实机验证网络层是否正常工作：
 * <ol>
 *   <li>输入 Emby 服务器地址、用户名、密码</li>
 *   <li>点击「测试登录」按钮</li>
 *   <li>在下方日志区域查看请求结果（AccessToken 或错误信息）</li>
 * </ol>
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUrl;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        etUrl = findViewById(R.id.etUrl);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvResult = findViewById(R.id.tvResult);

        // 让 TextView 支持滚动（备选方案，ScrollView 已包裹）
        tvResult.setMovementMethod(new ScrollingMovementMethod());

        // 设置登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    /**
     * 执行登录操作：校验输入 → 初始化网络 → 发起请求 → 显示结果。
     */
    private void performLogin() {
        // 1. 获取输入
        String url = etUrl.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 2. 基本校验
        if (TextUtils.isEmpty(url)) {
            appendLog("❌ 错误：请输入服务器 URL");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            appendLog("❌ 错误：请输入用户名");
            return;
        }

        // 3. 确保 URL 格式正确（自动补全 http://）
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
            etUrl.setText(url);
        }
        // 确保 baseUrl 以 "/" 结尾（Retrofit 要求）
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        appendLog("🔗 服务器: " + url);
        appendLog("👤 用户名: " + username);
        appendLog("⏳ 正在连接...");

        // 4. 禁用按钮，防止重复点击
        btnLogin.setEnabled(false);

        // 5. 初始化（或重新初始化）网络管理器
        NetworkManager.init(url);

        // 6. 发起异步登录请求
        LoginRequest request = new LoginRequest(username, password);
        NetworkManager.getInstance().getEmbyApiService().login(request)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call,
                                           Response<LoginResponse> response) {
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse body = response.body();
                            String token = body.getAccessToken();

                            appendLog("✅ 登录成功！");
                            appendLog("🔑 AccessToken: " + token);

                            if (body.getSessionInfo() != null) {
                                appendLog("🆔 UserId: "
                                        + body.getSessionInfo().getUserId());
                            }

                            // 将 Token 保存到拦截器，后续请求自动携带
                            NetworkManager.getInstance()
                                    .getAuthInterceptor()
                                    .setAccessToken(token);

                            // 跳转到 Leanback 海报墙主界面
                            Intent intent = new Intent(MainActivity.this,
                                    HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            appendLog("❌ 登录失败: HTTP " + response.code());
                            try {
                                if (response.errorBody() != null) {
                                    appendLog("📄 响应: "
                                            + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                appendLog("⚠️ 读取错误响应失败: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        appendLog("❌ 网络请求失败!");
                        appendLog("💥 异常类型: " + t.getClass().getSimpleName());
                        appendLog("💬 错误信息: " + t.getMessage());
                    }
                });
    }

    /**
     * 在日志区域追加一行带时间戳的文本，并自动滚动到底部。
     *
     * @param message 要追加的日志内容
     */
    private void appendLog(final String message) {
        // 确保在主线程执行（onFailure 可能在非主线程回调）
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String timestamp = new SimpleDateFormat("HH:mm:ss",
                        Locale.getDefault()).format(new Date());
                tvResult.append("\n[" + timestamp + "] " + message);
            }
        });
    }
}
