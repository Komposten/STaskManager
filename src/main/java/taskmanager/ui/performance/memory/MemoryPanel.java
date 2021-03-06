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

package taskmanager.ui.performance.memory;

import taskmanager.Measurements;
import taskmanager.data.SystemInformation;
import taskmanager.platform.linux.LinuxExtraInformation;
import taskmanager.platform.win32.WindowsExtraInformation;
import taskmanager.ui.SimpleGridBagLayout;
import taskmanager.ui.TextUtils;
import taskmanager.ui.TextUtils.ValueType;
import taskmanager.ui.performance.GraphPanel;
import taskmanager.ui.performance.GraphType;
import taskmanager.ui.performance.GraphTypeButton;
import taskmanager.ui.performance.common.InformationItemPanel;
import taskmanager.ui.performance.RatioItemPanel;
import taskmanager.ui.performance.ShowProcessCallback;
import taskmanager.ui.performance.TimelineGraphPanel;
import taskmanager.ui.performance.TimelineGroup;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GridBagConstraints;

public class MemoryPanel extends JPanel {
	private final Measurements<Long> memoryAvailable;

	private final JLabel labelMaxMemory;

	private final GraphPanel memoryGraph;
	private final TimelineGraphPanel timelineGraph;
	private final MemoryCompositionPanel memoryComposition;

	private final InformationItemPanel inUsePanel;
	private final InformationItemPanel availablePanel;

	// Windows specific
	private final RatioItemPanel committedPanel;
	private final InformationItemPanel cachedPanel;
	private final InformationItemPanel pagedPoolPanel;
	private final InformationItemPanel nonpagedPoolPanel;

	// Linux specific
	private final InformationItemPanel sharedPanel;
	private final RatioItemPanel swapPanel;

	private GraphTypeButton connectedButton;


	public MemoryPanel(TimelineGroup timelineGroup, SystemInformation systemInformation, ShowProcessCallback showProcessCallback) {
		memoryAvailable = systemInformation.memoryUsed;

		JLabel labelHeader = new JLabel("Memory");
		labelHeader.setFont(labelHeader.getFont().deriveFont(24f));

		JLabel labelMemoryUsage = new JLabel("Memory usage");
		JLabel labelZero = new JLabel("0");
		JLabel labelMaxTime = new JLabel("Displaying 60 seconds");
		labelMaxMemory = new JLabel("XX GB");
		JLabel labelComposition = new JLabel("Memory composition");

		memoryGraph = new GraphPanel(GraphType.Memory, ValueType.Bytes);
		timelineGraph = new TimelineGraphPanel(GraphType.Memory, labelMaxTime);
		memoryComposition = new MemoryCompositionPanel(systemInformation);

		memoryGraph.addGraph(memoryAvailable, systemInformation.memoryUsedTopList);
		timelineGraph.connectGraphPanels(memoryGraph);
		timelineGraph.addGraph(memoryAvailable);
		timelineGroup.add(timelineGraph);


		JPanel realTimePanel = new JPanel();
		inUsePanel = new InformationItemPanel("In use", ValueType.Bytes);
		availablePanel = new InformationItemPanel("Available", ValueType.Bytes);
		committedPanel = new RatioItemPanel("Committed", ValueType.Bytes);
		cachedPanel = new InformationItemPanel("Cached", ValueType.Bytes);
		pagedPoolPanel = new InformationItemPanel("Paged pool", ValueType.Bytes);
		nonpagedPoolPanel = new InformationItemPanel("Non-paged pool", ValueType.Bytes);
		sharedPanel = new InformationItemPanel("Shared memory", ValueType.Bytes);
		swapPanel = new RatioItemPanel("Swap", ValueType.Bytes);

		Font dataFont = inUsePanel.getFont().deriveFont(Font.BOLD, inUsePanel.getFont().getSize() + 3f);
		inUsePanel.setFont(dataFont);
		availablePanel.setFont(dataFont);
		committedPanel.setFont(dataFont);
		cachedPanel.setFont(dataFont);
		pagedPoolPanel.setFont(dataFont);
		nonpagedPoolPanel.setFont(dataFont);

		SimpleGridBagLayout realTimeLayout = new SimpleGridBagLayout(realTimePanel);
		realTimeLayout.addToGrid(inUsePanel, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
		realTimeLayout.addToGrid(availablePanel, 1, 0, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
		if (systemInformation.extraInformation instanceof WindowsExtraInformation) {
			realTimeLayout.addToGrid(committedPanel, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
			realTimeLayout.addToGrid(cachedPanel, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
			realTimeLayout.addToGrid(pagedPoolPanel, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
			realTimeLayout.addToGrid(nonpagedPoolPanel, 1, 2, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
		} else if (systemInformation.extraInformation instanceof LinuxExtraInformation) {
			realTimeLayout.addToGrid(sharedPanel, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
			realTimeLayout.addToGrid(swapPanel, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, 1, 0);
		}

		SimpleGridBagLayout layout = new SimpleGridBagLayout(this);
		layout.addToGrid(labelHeader, 0, 0, 1, 1, GridBagConstraints.WEST);
		layout.setInsets(0, 5, 0, 5);
		layout.addToGrid(labelMemoryUsage, 0, 1, 1, 1, GridBagConstraints.WEST);
		layout.addToGrid(labelMaxMemory, 1, 1, 1, 1, GridBagConstraints.EAST);
		layout.setInsets(2, 5, 2, 5);
		layout.addToGrid(memoryGraph, 0, 2, 2, 1, GridBagConstraints.BOTH, 1, 1);
		layout.setInsets(0, 5, 0, 5);
		layout.addToGrid(labelMaxTime, 0, 3, 1, 1, GridBagConstraints.WEST);
		layout.addToGrid(labelZero, 1, 3, 1, 1, GridBagConstraints.EAST);
		layout.setInsets(5, 5, 5, 5);
		layout.addToGrid(timelineGraph, 0, 4, 2, 1, GridBagConstraints.HORIZONTAL, 1, 0);
		layout.setInsets(0, 5, 0, 5);
		layout.addToGrid(labelComposition, 0, 5, 1, 1, GridBagConstraints.WEST);
		layout.setInsets(5, 5, 5, 5);
		layout.addToGrid(memoryComposition, 0, 6, 2, 1, GridBagConstraints.HORIZONTAL, 1, 0);
		layout.addToGrid(realTimePanel, 0, 7, 2, 1, GridBagConstraints.WEST);

		MemoryContextMenu contextMenu = new MemoryContextMenu(systemInformation.memoryUsedTopList, showProcessCallback);
		setComponentPopupMenu(contextMenu);
		memoryGraph.setComponentPopupMenu(contextMenu);
		timelineGraph.setComponentPopupMenu(contextMenu);
	}


	public void update(SystemInformation systemInformation) {
		labelMaxMemory.setText(TextUtils.valueToString(systemInformation.physicalMemoryTotal, ValueType.Bytes));

		long memoryUsed = systemInformation.memoryUsed.newest();

		memoryGraph.setMaxDatapointValue(systemInformation.physicalMemoryTotal);
		timelineGraph.setMaxDatapointValue(systemInformation.physicalMemoryTotal);
		connectedButton.setMaxDatapointValue(systemInformation.physicalMemoryTotal);
		memoryGraph.newDatapoint();
		timelineGraph.newDatapoint();
		connectedButton.newDatapoint(memoryUsed);

		memoryComposition.update(systemInformation);

		// Labels
		inUsePanel.updateValue(memoryUsed);
		availablePanel.updateValue(systemInformation.physicalMemoryTotal - memoryUsed);

		if (systemInformation.extraInformation instanceof WindowsExtraInformation) {
			WindowsExtraInformation extraInformation = (WindowsExtraInformation) systemInformation.extraInformation;
			committedPanel.setMaximum(extraInformation.commitLimit);
			committedPanel.updateValue(extraInformation.commitUsed);
			cachedPanel.updateValue(extraInformation.standbyMemory + extraInformation.modifiedMemory);
			pagedPoolPanel.updateValue(extraInformation.kernelPaged);
			nonpagedPoolPanel.updateValue(extraInformation.kernelNonPaged);
		} else if (systemInformation.extraInformation instanceof LinuxExtraInformation) {
			LinuxExtraInformation extraInformation = (LinuxExtraInformation) systemInformation.extraInformation;
			sharedPanel.updateValue(extraInformation.sharedMemory);
			swapPanel.setMaximum(extraInformation.swapSize);
			swapPanel.updateValue(extraInformation.swapUsed);
		}
	}


	public GraphTypeButton createMemoryButton() {
		connectedButton = new GraphTypeButton(GraphType.Memory, ValueType.Bytes, "Memory");
		connectedButton.setIsLogarithmic(memoryGraph.isLogarithmic());
		connectedButton.addGraph(memoryAvailable);
		return connectedButton;
	}
}