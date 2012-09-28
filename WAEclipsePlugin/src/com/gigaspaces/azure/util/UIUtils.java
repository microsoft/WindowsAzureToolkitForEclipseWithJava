package com.gigaspaces.azure.util;

import org.eclipse.swt.widgets.Combo;

public class UIUtils {
	
	public static int findSelectionByText(String txt, Combo combo) {
		if (txt == null || txt.isEmpty()) return 0;
		for (int i = 0 ; i < combo.getItemCount() ; i++) {
			String itemText = combo.getItem(i);
			if (itemText.equals(txt)) return i;
		}
		return 0;
	}

}
