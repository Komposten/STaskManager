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

import com.sun.jna.Platform;
import taskmanager.data.Process;
import taskmanager.data.Status;
import taskmanager.platform.linux.LinuxProcess;
import taskmanager.platform.win32.WindowsProcess;
import taskmanager.ui.AbstractMenuItem;

import javax.swing.JOptionPane;
import java.awt.Component;

public class SuspendResumeProcessMenuItem extends AbstractMenuItem {
	private final Component parent;
	private Process process;

	public SuspendResumeProcessMenuItem(Component parent) {
		super("Suspend process");
		this.parent = parent;
	}

	@Override
	protected void doAction() {
		String action = "suspend";
		if (process.status == Status.Suspended) {
			action = "resume";
		}
		int result = JOptionPane.showConfirmDialog(parent, "<html>Do you want to " + action + " \"" + process.fileName + "\"?</html>", "Killing process", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			boolean succeeded;
			if (Platform.isWindows()) {
				succeeded = (process.status == Status.Suspended) ? WindowsProcess.resume(process.id) : WindowsProcess.suspend(process.id);
			} else if (Platform.isLinux()) {
				succeeded = (process.status == Status.Suspended) ? LinuxProcess.resume(process.id) : LinuxProcess.suspend(process.id);
			} else {
				throw new UnsupportedOperationException("You are running an unsupported operating system!");
			}

			if (!succeeded) {
				JOptionPane.showMessageDialog(parent, "Failed to terminate the process!", "Termination failed", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void setProcess(Process process) {
		this.process = process;
		setEnabled(process.status != Status.Dead);
		if (process.status == Status.Suspended) {
			setText("Resume process");
		} else {
			setText("Suspend process");
		}
	}
}
