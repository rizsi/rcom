package com.rizsi.rcom.webcam;

import java.util.Map;

import javax.swing.JOptionPane;

import com.rizsi.rcom.AbstractRcomArgs;

public class ListCams {
	public WebCamParameter queryUser(AbstractRcomArgs args) throws Exception
	{
		Map<String, WebCamParameter> ret=args.platform.getCameras(args);
		String[] choices = ret.keySet().toArray(new String[] {});
		String input = (String) JOptionPane.showInputDialog(null, "Select...", "WebCam input mode",
				JOptionPane.QUESTION_MESSAGE, null, // Use
													// default
													// icon
				choices, // Array of choices
				choices[1]); // Initial choice
		if(input==null)
		{
			return null;
		}
		return ret.get(input);
	}
}
