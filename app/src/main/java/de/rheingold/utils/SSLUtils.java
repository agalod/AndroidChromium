package de.rheingold.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import static org.chromium.base.ContextUtils.getApplicationContext;

public class SSLUtils
{
//    protected static final String TAG = "NukeSSLCerts";

    public static void nuke() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    public static void setCertificate(Context context)
    {
        try
        {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt

            AssetManager assManager = getApplicationContext().getAssets();
            InputStream is = null;
            try {
                is = assManager.open("rheingold-salon.crt");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            InputStream caInput = new BufferedInputStream(is);
//            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("rheingold-salon.crt");
//            FileDescriptor fileDescriptor = assetFileDescriptor.getFileDescriptor();
//            InputStream caInput = new BufferedInputStream(new FileInputStream(fileDescriptor));
//            InputStream caInput = new BufferedInputStream(is);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
//                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (CertificateException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (KeyManagementException e)
        {
            e.printStackTrace();
        } catch (NoSuchProviderException e)
        {
            e.printStackTrace();
        }
    }


//    public void createTrustManager()
//    {
//        try
//        {
//
//            /* To understand the concept about Self-Signed Certificates, Custom TrustManagers, etc.
//            CertificateFactory:  https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateFactory.html
//            Certificate:         https://docs.oracle.com/javase/7/docs/api/java/security/cert/Certificate.html
//            KeyStore:            https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html
//            KeyManagerFactory:   https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/KeyManagerFactory.html
//            TrustManagerFactory: http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/TrustManagerFactory.html
//            SSLContext:          https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html
//            SSLSocketFactory:    https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLSocketFactory.html
//            */
//
//            Context myContext = getApplicationContext();
//            InputStream inStream = myContext.getResources().openRawResource(R.raw.tdmssl);
//            // Use the .crt file obtained via this script: https://gist.github.com/ivanlmj/a6a93dd142fb623d01262303d5bd8074
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            Certificate ca = cf.generateCertificate(inStream);
//            inStream.close();
//
//            String keyStoreType = KeyStore.getDefaultType();
//            KeyStore ks = KeyStore.getInstance(keyStoreType);
//            ks.load(null, null);
//            ks.setCertificateEntry("ca", ca);
//
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//            tmf.init(ks);
//
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, tmf.getTrustManagers(), null);
//
//            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            hurlStack = new HurlStack(null, sslSocketFactory);
//
//        } catch (Exception e)
//        {
//            Log.d("my_app", "LoginScreen.java - SSL Exception: " + e.toString());
//            e.printStackTrace();
//        }
//
//    }
}
