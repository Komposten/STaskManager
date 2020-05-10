/*
 * Copyright (c) 2020. Sebastian Hjelm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * See LICENSE for further details.
 */

package taskmanager.ui.details;

import taskmanager.Process;

import javax.swing.JPopupMenu;
import java.awt.Component;

public class ProcessContextMenu extends JPopupMenu {
	private OpenProcessDialogMenuItem openDialogMenuItem;
	private DeleteProcessMenuItem deleteMenuItem;
	private OpenFileLocationMenuItem openLocationMenuItem;
	private CopyToClipboardMenuItem copyToClipboardMenuItem;

	public ProcessContextMenu(Component parent) {
		openDialogMenuItem = new OpenProcessDialogMenuItem(parent);
		deleteMenuItem = new DeleteProcessMenuItem(parent);
		openLocationMenuItem = new OpenFileLocationMenuItem(parent);
		copyToClipboardMenuItem = new CopyToClipboardMenuItem(this);

		add(openDialogMenuItem);
		addSeparator();
		add(openLocationMenuItem);
		add(deleteMenuItem);
		addSeparator();
		add(copyToClipboardMenuItem);
	}

	public void setProcess(Process process) {
		openDialogMenuItem.setProcess(process);
		deleteMenuItem.setProcess(process);
		openLocationMenuItem.setProcess(process);
	}

	public void setCellText(String column, String text) {
		copyToClipboardMenuItem.setCellText(column, text);
	}
}
