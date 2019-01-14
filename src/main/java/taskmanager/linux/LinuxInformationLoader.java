package taskmanager.linux;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import oshi.util.FileUtil;
import taskmanager.InformationLoader;
import taskmanager.Process;
import taskmanager.SystemInformation;

public class LinuxInformationLoader extends InformationLoader {
	private static final String PROC_PATH = "/proc";

//  private long lastCpuTime;
//  private long currentCpuTime;

	private long nextProcessId;

	@Override
	public void init(SystemInformation systemInformation) {
		super.init(systemInformation);

		systemInformation.physicalMemoryTotalInstalled = systemInformation.physicalMemoryTotal;

//    systemInformation.reservedMemory = systemInformation.physicalMemoryTotalInstalled - systemInformation.physicalMemoryTotal;

		systemInformation.processes.add(new Process(nextProcessId++, 0));
	}


	@Override
	public void update(SystemInformation systemInformation) {
		super.update(systemInformation);

//    updateTotalCpuTime();
		updateProcesses(systemInformation);
//
//    PERFORMANCE_INFORMATION performanceInfo = fetchPerformanceInformation();
//    systemInformation.totalProcesses = performanceInfo.ProcessCount.intValue();
//    systemInformation.totalThreads = performanceInfo.ThreadCount.intValue();
//    systemInformation.totalHandles = performanceInfo.HandleCount.intValue();
//    
//    systemInformation.commitLimit = performanceInfo.CommitLimit.longValue() * systemInformation.pageSize;
//    systemInformation.commitUsed = performanceInfo.CommitTotal.longValue() * systemInformation.pageSize;
//    
//    systemInformation.kernelPaged = performanceInfo.KernelPaged.longValue() * systemInformation.pageSize;
//    systemInformation.kernelNonpaged = performanceInfo.KernelNonpaged.longValue() * systemInformation.pageSize;
//    
//    Memory memory = new Memory(new SYSTEM_MEMORY_LIST_INFORMATION().size());
//    int status = NtDllExt.INSTANCE.NtQuerySystemInformation(
//        SYSTEM_INFORMATION_CLASS.SystemMemoryListInformation.ordinal(), memory, (int)memory.size(), null);
//    if (status == 0) {
//      SYSTEM_MEMORY_LIST_INFORMATION memoryInfo = Structure.newInstance(NtDllExt.SYSTEM_MEMORY_LIST_INFORMATION.class, memory);
//      memoryInfo.read();
//      systemInformation.modifiedMemory = memoryInfo.ModifiedPageCount.longValue() * systemInformation.pageSize;
//      systemInformation.standbyMemory = 0;
//      systemInformation.freeMemory = (memoryInfo.FreePageCount.longValue() + memoryInfo.ZeroPageCount.longValue()) * systemInformation.pageSize;
//      for (int i = 0; i < memoryInfo.PageCountByPriority.length; i++)
//      {
//        systemInformation.standbyMemory += memoryInfo.PageCountByPriority[i].longValue() * systemInformation.pageSize;
//      }
//    } else {
//      System.out.println("update(): Failed to read SYSTEM_MEMORY_LIST_INFORMATION: " + Integer.toHexString(status));
//    }
	}

	//  private void updateTotalCpuTime() {
//    lastCpuTime = currentCpuTime;
//    FILETIME time = new FILETIME();
//    Kernel32Ext.INSTANCE.GetSystemTimeAsFileTime(time);
//    currentCpuTime = time.toTime();
//  }
//
	private void updateProcesses(SystemInformation systemInformation) {
		Set<Long> newProcessIds = fetchProcessIds();

		for (Long pid : newProcessIds) {
			Process process = findProcess(systemInformation.processes, pid);
			if (process == null) {
				process = new Process(nextProcessId++, pid);
				systemInformation.processes.add(process);
			}

			String processPath = PROC_PATH + "/" + pid;

			if (!process.hasReadOnce) {
				process.commandLine = readFileAsString(processPath + "/cmdline").replaceAll("" + (char) 0, " ").trim();
//        Map<String, String> status = FileUtil.getKeyValueMapFromFile(processPath + "/status", ":");
				String partialName = readFileAsString(processPath + "/comm");
				if (partialName.isEmpty()) {
					Map<String, String> status = FileUtil.getKeyValueMapFromFile(processPath + "/status", ":");
					System.out.println("Found no name: " + status.getOrDefault("Name", "Not found!"));
				} else {
					int start = process.commandLine.indexOf(partialName);
					if (start == -1) {
						System.out.println("Error? " + partialName + " vs. " + process.commandLine);
					} else {
						int end = process.commandLine.indexOf(' ', start + partialName.length());
						end = (end == -1) ? process.commandLine.length() : end;
						process.fileName = process.commandLine.substring(start, end);
						if (process.fileName.endsWith(":")) {
							process.fileName = process.fileName.substring(0, process.fileName.length() - 1);
						}
					}
				}

				Map<String, String> status = FileUtil.getKeyValueMapFromFile(processPath + "/status", ":");
				process.privateWorkingSet.addValue(Long.parseLong(removeUnit(status.getOrDefault("VmRSS", "0 kb"))) * 1024);
//        if (process.id == 0) {
//          process.fileName = "System Idle Process";
//          process.userName = "SYSTEM";
//        } else
//          process.fileName = newProcess.ImageName.Buffer.getWideString(0);
//      
//        if (readProcessCommandLine(process)) {
//          readFileDescription(process);
//        }
//        
//        if (process.description.isEmpty())
//          process.description = process.fileName;
			}
//      
//      process.privateWorkingSet.addValue(newProcess.WorkingSetPrivateSize);
//
//      // For some reason we need to extract the value and then put it back inside a new LONG_INTEGER instance before using
//      // it otherwise the FILETIME becomes corrupted. Does this have something to do with the memory the NT-call returns?
//      process.updateCpu(
//          new FILETIME(new LARGE_INTEGER(newProcess.KernelTime.getValue())).toTime(), 
//          new FILETIME(new LARGE_INTEGER(newProcess.UserTime.getValue())).toTime(), 
//          (currentCpuTime - lastCpuTime), systemInformation.logicalProcessorCount);
//      
//      process.hasReadOnce = true;
		}

		// Remove old processes
		ListIterator<Process> itr = systemInformation.processes.listIterator();
		while (itr.hasNext()) {
			Process process = itr.next();
			if (!newProcessIds.contains(process.id)) {
				process.isDead = true;
				process.deathTimestamp = System.currentTimeMillis();
				itr.remove();
				systemInformation.deadProcesses.add(process);
			}
		}
	}

	private String removeUnit(String vmRSS) {
		return vmRSS.substring(0, vmRSS.length()-3);
	}


	private String readFileAsString(String path) {
		try {
			List<String> lines = Files.readAllLines(new File(path).toPath());
			if (lines.isEmpty())
				return "";
			return lines.get(0);
		} catch (IOException e) {
			System.out.println("readFileAsString(): Failed to read file: " + path);
		}
		return "";
	}

	//
//  private boolean readProcessCommandLine(Process process) {
//    WinNT.HANDLE handle = Kernel32.INSTANCE.OpenProcess( // TODO Try again with only PROCESS_QUERY_LIMITED_INFORMATION, might give you the user name at least
//        WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ,
//        false,
//        (int)process.id);
//    if (handle == null) {
//      System.out.println("readProcessCommandLine(): Failed to open " + process.fileName + ": " + Integer.toHexString(Native.getLastError()));
//      return false;
//    }
//    
//    Memory mem = new Memory(new PROCESS_BASIC_INFORMATION().size());
//    int status = NtDllExt.INSTANCE.NtQueryInformationProcess(handle, 0, mem, (int)mem.size(), null);
//    if (status != 0) {
//      System.out.println("readProcessCommandLine(): Failed to read process information for " + process.fileName + ": " + Integer.toHexString(status));
//      Kernel32.INSTANCE.CloseHandle(handle);
//      return false;
//    }
//    
//    PROCESS_BASIC_INFORMATION processInfo = Structure.newInstance(NtDllExt.PROCESS_BASIC_INFORMATION.class, mem);
//    processInfo.read();
//    
//    mem = new Memory(new PEB().size());
//    if (!readProcessMemory(handle, mem, processInfo.PebBaseAddress, process, "PEB"))
//      return false;
//    
//    PEB peb = Structure.newInstance(NtDllExt.PEB.class, mem);
//    peb.read();
//    
//    mem = new Memory(new RTL_USER_PROCESS_PARAMETERS().size());
//    if (!readProcessMemory(handle, mem, peb.ProcessParameters, process, "RTL_USER_PROCESS_PARAMETERS"))
//      return false;
//    
//    RTL_USER_PROCESS_PARAMETERS parameters = Structure.newInstance(NtDllExt.RTL_USER_PROCESS_PARAMETERS.class, mem);
//    parameters.read();
//    
//    mem = new Memory(parameters.ImagePathName.Length + 2);
//    if (!readProcessMemory(handle, mem, parameters.ImagePathName.Buffer, process, "image path"))
//      return false;
//    
//    process.filePath = mem.getWideString(0);
//    
//    mem = new Memory(parameters.CommandLine.Length + 2);
//    if (!readProcessMemory(handle, mem, parameters.CommandLine.Buffer, process, "command line"))
//      return false;
//    
//    process.commandLine = mem.getWideString(0);
//    
//    HANDLEByReference tokenRef = new HANDLEByReference();
//    if (Advapi32.INSTANCE.OpenProcessToken(handle, WinNT.TOKEN_QUERY, tokenRef)) {
//      Account account = Advapi32Util.getTokenAccount(tokenRef.getValue());
//      process.userName = account.name;
//    } else {
//      Kernel32.INSTANCE.CloseHandle(handle);
//      return false;
//    }
//    
//    Kernel32.INSTANCE.CloseHandle(handle);
//    
//    return true;
//  }
//
//  private boolean readProcessMemory(WinNT.HANDLE handle, Memory mem, Pointer address, Process process, String targetStruct) {
//    boolean success = Kernel32.INSTANCE.ReadProcessMemory(handle, address, mem, (int)mem.size(), null);
//    if (!success) {
//      System.out.println("readProcessCommandLine(): Failed to read " + targetStruct + " information for " + process.fileName + ": " + Integer.toHexString(Native.getLastError()));
//      Kernel32.INSTANCE.CloseHandle(handle);
//    }
//    return success;
//  }
//
//  private boolean readFileDescription(Process process) {
//    IntByReference size = new IntByReference();
//    int versionInfoSize = Version.INSTANCE.GetFileVersionInfoSize(process.filePath, size);
//    if (versionInfoSize == 0) {
//      System.out.println("readFileDescription(): Failed to read FileVersionSize for " + process.filePath + ": " + Integer.toHexString(Native.getLastError()));
//      return false;
//    }
//    
//    Memory mem = new Memory(versionInfoSize);
//    if (!Version.INSTANCE.GetFileVersionInfo(process.filePath, 0, (int) mem.size(), mem)) {
//      System.out.println("readFileDescription(): Failed to read FileVersionInfo for " + process.filePath + ": " + Integer.toHexString(Native.getLastError()));
//      return false;
//    }
//    
//    PointerByReference pointerRef = new PointerByReference();
//    if (!Version.INSTANCE.VerQueryValue(mem, "\\VarFileInfo\\Translation", pointerRef, size)) {
//      System.out.println("readFileDescription(): Failed to read Translations for " + process.filePath + ": " + Integer.toHexString(Native.getLastError()));
//      return false;
//    }
//    
//    int nLangs = size.getValue()/new LANGANDCODEPAGE().size();
//    LANGANDCODEPAGE language = Structure.newInstance(VersionExt.LANGANDCODEPAGE.class, pointerRef.getValue());
//    language.read();
//    String query = "\\StringFileInfo\\" + String.format("%04x%04x", language.wLanguage.intValue(), language.wCodePage.intValue()).toUpperCase() + "\\FileDescription";
//    
////    System.out.println("Query: " + query);
//    if (!Version.INSTANCE.VerQueryValue(mem, query, pointerRef, size)) {
//      System.out.println("readFileDescription(): Failed to read FileDescription for " + process.filePath + ": " + Integer.toHexString(Native.getLastError()));
//      return false;
//    }
//    
//    process.description = pointerRef.getValue().getWideString(0).trim();
//    
//    return true;
//  }
//
	private Set<Long> fetchProcessIds() {
		Set<Long> processIds = new HashSet<>();
		File processDir = new File(PROC_PATH);
		for (File file : processDir.listFiles()) {
			String fileName = file.getName();
			if (file.isDirectory() && fileName.matches("[0-9]+")) {
				Long pid = Long.parseLong(fileName);
				processIds.add(pid);
			}
		}
		return processIds;
	}

	private Process findProcess(List<Process> processes, long processId) {
		for (Process process : processes) {
			if (process.id == processId) {
				return process;
			}
		}
		return null;
	}
}
