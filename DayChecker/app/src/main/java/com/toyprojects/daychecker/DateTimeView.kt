package com.toyprojects.daychecker

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.view_date_time.view.*

class DateTimeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_date_time, this, true)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DateTimeView,
            0, 0).apply {

            try {
                vdtImage.setImageDrawable(getDrawable(R.styleable.DateTimeView_vdtImage))

                vdtTextView.text = getString(R.styleable.DateTimeView_vdtText)
                vdtTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimension(R.styleable.DateTimeView_textSize, 16F))
                vdtTextView.typeface = ResourcesCompat.getFont(context, getResourceId(R.styleable.DateTimeView_fontFamily, R.font.medium))
                vdtTextView.setTextColor(getColor(R.styleable.DateTimeView_textColor, 0))

                val vdtImageParams = vdtImage.layoutParams as LayoutParams
                vdtImageParams.topMargin = getDimensionPixelSize(R.styleable.DateTimeView_innerMargin, 0)
                vdtImageParams.bottomMargin = getDimensionPixelSize(R.styleable.DateTimeView_innerMargin, 0)
                vdtImage.layoutParams = vdtImageParams

            } finally {
                recycle()
            }
        }
    }

    fun setDTText(text: String) {
        vdtTextView.text = text
    }

    override fun setOnClickListener(l: OnClickListener?) {
        vdtTextView.setOnClickListener(l)
    }
}