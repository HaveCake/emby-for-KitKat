package org.jellyfin.emby.kitkat.network;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Enables TLS 1.2 on Android 4.4 (KitKat) devices where it is supported
 * by the platform but not enabled by default.
 * <p>
 * On API 16-19 the platform SSLSocketFactory negotiates TLS 1.0 only.
 * This wrapper forces TLS 1.2 (and 1.1) so that modern HTTPS servers
 * that have dropped TLS 1.0 remain reachable.
 * <p>
 * Usage: pass the return value of {@link #createSocketFactory()} to
 * {@code OkHttpClient.Builder#sslSocketFactory}.
 */
public final class Tls12SocketFactory extends SSLSocketFactory {

    private static final String[] TLS_SUPPORTED_PROTOCOLS = {"TLSv1.2", "TLSv1.1"};

    private final SSLSocketFactory delegate;

    public Tls12SocketFactory(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a TLS 1.2-capable {@link SSLSocketFactory}.
     * On API &ge; 20 this simply returns the platform default.
     */
    public static SSLSocketFactory createSocketFactory()
            throws NoSuchAlgorithmException, KeyManagementException {
        if (Build.VERSION.SDK_INT >= 20) {
            return (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, null, null);
        return new Tls12SocketFactory(sc.getSocketFactory());
    }

    // --- delegate every factory method, patching the enabled protocols ------

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        return patch(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return patch(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddress, int localPort) throws IOException {
        return patch(delegate.createSocket(address, port, localAddress, localPort));
    }

    private static Socket patch(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(TLS_SUPPORTED_PROTOCOLS);
        }
        return socket;
    }
}
