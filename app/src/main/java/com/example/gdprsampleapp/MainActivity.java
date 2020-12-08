package com.example.gdprsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String IS_PERSONALIZED = "isPersonalized";
    AdView adView;
    InterstitialAd interstitialAd;
    Button btnLoadInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoadInterstitial = findViewById(R.id.btn_loadinterstitial);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                getConsentStatus();
            }
        });

        btnLoadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(interstitialAd!=null && interstitialAd.isLoaded())
                {
                    interstitialAd.show();
                }
            }
        });


    }

    private void loadInterstitial() {
        boolean isPersonalized = SharedPrefUtil.getBoolean(MainActivity.this, IS_PERSONALIZED, false);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        AdRequest adRequest;
        if (isPersonalized) {
            adRequest = new AdRequest.Builder().build();
        } else {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        }
        interstitialAd.loadAd(adRequest);
    }

    private void loadBannerAd() {
        boolean isPersonalized = SharedPrefUtil.getBoolean(MainActivity.this, IS_PERSONALIZED, false);
        adView = findViewById(R.id.adView);

        // this is the part you need to add/modify on your code
        AdRequest adRequest;
        if (isPersonalized) {
            adRequest = new AdRequest.Builder().build();
        } else {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        }
        adView.loadAd(adRequest);
    }

    private void getConsentStatus() {
        ConsentInformation consentInformation = ConsentInformation.getInstance(MainActivity.this);
        String[] publisherIds = {"pub-123456789"};// Sign in to Admob and get your publisher ID and replace
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                if (ConsentInformation.getInstance(getBaseContext()).isRequestLocationInEeaOrUnknown()) {
                    //Inside EU get Consent
                    switch (consentStatus) {
                        case UNKNOWN:
                            displayConsentForm();
                            break;
                        case PERSONALIZED:
                            //Load Personalized Ads as we have got the consent for it.
                            loadBannerAd();
                            loadInterstitial();
                            break;
                        case NON_PERSONALIZED:
                            //User declined Consent for Personalized ads hence show non Personalized Ads.
                            loadBannerAd();
                            loadInterstitial();
                            break;
                    }
                } else {
                    //Not in EU display Normal Ads
                    loadBannerAd();
                    loadInterstitial();
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });

        ConsentInformation.getInstance(MainActivity.this).addTestDevice("6F95960A3684F4D12D8453CC9187C6D4");
        ConsentInformation.getInstance(MainActivity.this).
                setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
    }
    ConsentForm form;
    private void displayConsentForm() {
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL("https://www.your.com/privacyurl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        form.show();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        // Consent form was closed.
                        switch (consentStatus) {
                            case PERSONALIZED:
                                SharedPrefUtil.putBoolean(MainActivity.this, IS_PERSONALIZED, true);
                                loadBannerAd(); // true means show Personalized ads
                                loadInterstitial();
                                break;
                            case NON_PERSONALIZED:
                                SharedPrefUtil.putBoolean(MainActivity.this, IS_PERSONALIZED, true);
                                loadBannerAd();
                                loadInterstitial();
                                break;
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build();

        form.load();
    }



}