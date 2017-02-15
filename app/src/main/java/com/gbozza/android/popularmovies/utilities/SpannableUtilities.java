package com.gbozza.android.popularmovies.utilities;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;

public class SpannableUtilities {
    /**
     *
     * @param string the text to be styled in bold
     * @return the SpannabelString containing the bold text
     */
    public static  SpannableString makeBold(String string) {
        SpannableString boldText = new SpannableString(string);
        boldText.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), 0);
        return boldText;
    }
}
