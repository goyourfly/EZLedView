package com.goyourfly.ezledview.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.AttributeSet;
import android.widget.TextView;

public class EmojiTextView extends TextView {
	private Context context;
	
	public EmojiTextView(Context context) {
		super(context);
		this.context = context;
	}
	
	public EmojiTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public EmojiTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}
	
	public void setEmojiText(String text) {
		text = EmojiUtils.convertTag(text);
		CharSequence spanned = Html.fromHtml(text, emojiGetter, null);
		setText(spanned);
	}
	
	private ImageGetter emojiGetter = new ImageGetter()
	{
        public Drawable getDrawable(String source){
            // �摜�̃��\�[�XID���擾
            int id = getResources().getIdentifier(source, "drawable", context.getPackageName());
            
            Drawable emoji = getResources().getDrawable(id);
            int w = (int)(emoji.getIntrinsicWidth() * 1.25);
            int h = (int)(emoji.getIntrinsicHeight() * 1.25);
            emoji.setBounds(0, 0, w, h);
            return emoji;
        }
    };		
}
