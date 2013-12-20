package cz.hnutiduha.bioadresar.util;

import android.widget.TextView;

public class StringOperations {

    public static String getStringFromEditBox(TextView text)
    {
    	if (text == null) return null;
    	
    	return text.getText().toString().trim();
    }

}
