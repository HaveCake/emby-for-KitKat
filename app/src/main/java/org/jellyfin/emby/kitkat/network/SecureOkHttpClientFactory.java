package org.jellyfin.emby.kitkat.network;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Creates a hardened {@link OkHttpClient} that mitigates two issues on
 * Android 4.4 (KitKat):
 * <ol>
 *   <li><b>TLS 1.2 not enabled by default</b> &ndash; solved by
 *       {@link Tls12SocketFactory}.</li>
 *   <li><b>OkHttp &lt; 4.9.2 hostname-verification vulnerability
 *       (GHSA-w33c-445m-f8w7)</b> &ndash; OkHttp 3.12.x's internal
 *       {@code OkHostnameVerifier} may accept a certificate whose
 *       Subject Alternative Name does not match the requested host.
 *       We mitigate this by installing the <em>platform-default</em>
 *       {@link HostnameVerifier} obtained from
 *       {@link HttpsURLConnection#getDefaultHostnameVerifier()}, which
 *       is <strong>not</strong> affected by the OkHttp bug.</li>
 * </ol>
 *
 * <p>Every network layer in the app (Retrofit, Glide, raw OkHttp calls)
 * should obtain its {@code OkHttpClient} from {@link #create()} to
 * ensure both patches are active.</p>
 */
public final class SecureOkHttpClientFactory {

    private static final String TAG = "SecureOkHttp";

    private SecureOkHttpClientFactory() { /* utility class */ }

    /**
     * Builds a new {@link OkHttpClient} with:
     * <ul>
     *   <li>TLS 1.2 enabled (KitKat patch)</li>
     *   <li>Platform-default {@link HostnameVerifier} to bypass the
     *       vulnerable OkHttp internal verifier</li>
     * </ul>
     */
    public static OkHttpClient create() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // --- Mitigation: override OkHttp's hostname verifier ----------------
        // The platform verifier (Android's own implementation) is not affected
        // by GHSA-w33c-445m-f8w7 and performs strict RFC-2818 / RFC-6125
        // hostname matching.
        builder.hostnameVerifier(new HostnameVerifier() {
            private final HostnameVerifier platformVerifier =
                    HttpsURLConnection.getDefaultHostnameVerifier();

            @Override
            public boolean verify(String hostname, SSLSession session) {
                return platformVerifier.verify(hostname, session);
            }
        });

        // --- TLS 1.2 patch for KitKat ---------------------------------------
        try {
            SSLSocketFactory sslSocketFactory = Tls12SocketFactory.createSocketFactory();

            // OkHttp 3.x requires a X509TrustManager alongside the factory.
            // We use the platform default trust manager.
            javax.net.ssl.TrustManagerFactory tmf =
                    javax.net.ssl.TrustManagerFactory.getInstance(
                            javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((java.security.KeyStore) null);
            javax.net.ssl.TrustManager[] trustManagers = tmf.getTrustManagers();

            if (trustManagers.length > 0
                    && trustManagers[0] instanceof X509TrustManager) {
                builder.sslSocketFactory(sslSocketFactory,
                        (X509TrustManager) trustManagers[0]);
            }
        } catch (NoSuchAlgorithmException | KeyManagementException
                | java.security.KeyStoreException e) {
            Log.w(TAG, "Unable to install TLS 1.2 socket factory", e);
        }

        // --- Logging (debug builds only) ------------------------------------
        if (org.jellyfin.emby.kitkat.BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(logging);
        }

        return builder.build();
    }
}
