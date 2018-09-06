package de.rheingold.utils;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
