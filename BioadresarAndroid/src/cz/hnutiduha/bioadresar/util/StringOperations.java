package cz.hnutiduha.bioadresar.util;

import android.widget.EditText;


public class StringOperations {

    public static String getStringFromEditBox(EditText text)
    {
    	if (text == null) return null;
    	
    	return text.getText().toString().trim();
    }

}
