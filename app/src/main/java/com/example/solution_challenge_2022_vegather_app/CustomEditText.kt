package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityJoinBinding

//clear 버튼이 있는 EditText 위젯
class CustomEditText : AppCompatEditText, OnTouchListener, OnFocusChangeListener, TextWatcher {
    private var clearDrawable: Drawable? = null
    @get:JvmName("getOnFocusChangeListener()") private var onFocusChangeListener: OnFocusChangeListener? = null
    private var onTouchListener: OnTouchListener? = null
    var email = findViewById<TextView>(R.id.email_ch_comment)


    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }
    constructor(context: Context, attrs:AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init() }

    override fun setOnFocusChangeListener(onFocusChangeListener: OnFocusChangeListener?) {
        this.onFocusChangeListener = onFocusChangeListener
    }

    override fun setOnTouchListener(onTouchListener: OnTouchListener?) {
        this.onTouchListener = onTouchListener
    }
//R.drawable.ic_menu_close_clear_cancel
    fun init(){
        val tempDrawable = ContextCompat.getDrawable(context,R.drawable.x_btn_gray) //X 아이콘 추후 수정
        clearDrawable = tempDrawable?.let { DrawableCompat.wrap(it) }
        clearDrawable?.let { DrawableCompat.setTintList(it, hintTextColors) }
        clearDrawable?.setBounds(
            0,
            0,
            clearDrawable!!.getIntrinsicWidth(),
            clearDrawable!!.getIntrinsicHeight()
        )

        setClearIconVisible(false)

        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        addTextChangedListener(this)
    }

    private fun setClearIconVisible(visible: Boolean) {
        clearDrawable?.setVisible(visible, false)
        if(visible){
            setCompoundDrawables(null,null, clearDrawable, null);
        }else{
            setCompoundDrawables(null,null, null, null);
        }
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null) {
            if (clearDrawable!!.isVisible && motionEvent.getX() > width - paddingRight - clearDrawable!!.intrinsicWidth) {
                if (motionEvent.getAction() === MotionEvent.ACTION_UP) {
                    error = null
                    setText(null)
                }
                return true
            }
        }

        return onTouchListener?.onTouch(view, motionEvent) ?: false
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible(getText()?.length ?: 0 > 0);
        } else {
            setClearIconVisible(false);
        }

        onFocusChangeListener?.onFocusChange(view, hasFocus)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (isFocused) {
            setClearIconVisible(s.length > 0)
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }

}