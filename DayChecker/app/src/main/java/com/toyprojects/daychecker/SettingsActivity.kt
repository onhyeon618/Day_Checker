package com.toyprojects.daychecker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.toyprojects.daychecker.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private var currentNativeAd: NativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setTitle(R.string.settings)

        topAppBar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_transition, R.anim.slide_out_right)
        }

        loadNativeAd()
    }

    private fun loadNativeAd() {
        val adLoader = AdLoader.Builder(this, getString(R.string.sample_unit_id))
            .forNativeAd { nativeAd: NativeAd ->
                currentNativeAd = nativeAd

                val styles = NativeTemplateStyle.Builder().build()
                binding.nativeAdTemplate.setStyles(styles)
                binding.nativeAdTemplate.setNativeAd(nativeAd)
            }
            .withAdListener(object: AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    binding.nativeAdTemplate.visibility = View.GONE
                    super.onAdFailedToLoad(p0)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        binding.nativeAdTemplate.nativeAdView.destroy()
        binding.nativeAdTemplate.removeAllViews()
        super.onDestroy()
    }
}