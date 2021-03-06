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

package taskmanager.filter;

import taskmanager.data.Process;

public abstract class TextContainsFilter implements Filter {
	private final String textToContain;

	public TextContainsFilter(String textToContain) {
		this.textToContain = textToContain;
	}

	@Override
	public boolean apply(Process process) {
		return textToFilter(process).contains(textToContain);
	}

	protected abstract String textToFilter(Process process);
}