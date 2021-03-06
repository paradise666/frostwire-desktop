/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.limewire.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Returns system information, where supported, for Windows and OSX. Most methods
 * in <code>SystemUtils</code> rely on native code and fail gracefully if the 
 * native code library isn't found. <code>SystemUtils</code> uses 
 * SystemUtilities.dll for Window environments and libSystemUtilities.jnilib 
 * for OSX.
 */
public class SystemUtils {
    
    private static final Log LOG = LogFactory.getLog(SystemUtils.class);
    
    /**
     * Whether or not the native libraries could be loaded.
     */
    private static boolean isLoaded;
    
	static {
		boolean canLoad = false;
		try {
			if ((OSUtils.isWindows() && OSUtils.isGoodWindows()) || ( OSUtils.isMacOSX() ) ) {
				System.loadLibrary("SystemUtilities");
				canLoad = true;
			}
		} catch (UnsatisfiedLinkError noGo) {
			System.out.println("ERROR: " + noGo.getMessage());
			canLoad = false;
		}
		isLoaded = canLoad;
	}
    
    private SystemUtils() {}
    
    
    /**
     * Retrieves the amount of time the system has been idle, where
     * idle means the user has not pressed a key, mouse button, or moved
     * the mouse.  The time returned is in milliseconds.
     */
    public static long getIdleTime() {
    	if(supportsIdleTime()) 
            return idleTime();

        return 0;
    }
    
    /**
     * Returns whether or not the idle time function is supported on this
     * operating system.
     * 
     * @return <tt>true</tt> if we're able to determine the idle time on this
     *  operating system, otherwise <tt>false</tt>
     */
    public static boolean supportsIdleTime() {
        if(isLoaded) {
            if(OSUtils.isGoodWindows())
                return true;
            else if(OSUtils.isMacOSX())
                return true;
        }
            
        return false;
    }
    
    /**
     * Sets the number of open files, if supported.
     */
    public static long setOpenFileLimit(int max) {
        if(isLoaded && OSUtils.isMacOSX())
            return setOpenFileLimit0(max);
        else
            return -1;
    }
    
    /**
     * Sets a file to be writeable.  Package-access so FileUtils can delegate
     * the filename given should ideally be a canonicalized filename.
     */
    static void setWriteable(String fileName) {
        if(isLoaded && (OSUtils.isWindows() || OSUtils.isMacOSX()))
            setFileWriteable(fileName);
    }

    private static final native int setOpenFileLimit0(int max);

	/**
	 * Gets the path to the Windows launcher .exe file that is us running right now.
	 * 
	 * @return A String like "c:\Program Files\LimeWire\LimeWire.exe".
	 *         null on error.
	 */
    public static final String getRunningPath() {
        try {
            if (OSUtils.isWindows() && isLoaded) {
                String path = getRunningPathNative();
                if (path.equals(""))
                    return null;
                else
                    return path;
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }
    
    /** A list of places that getSpecialPath uses. */
    public static enum SpecialLocations {
        DOCUMENTS("Documents"),
        DOWNLOADS("Downloads"),
        APPLICATION_DATA("ApplicationData"),
        DESKTOP("Desktop"),
        START_MENU("StartMenu"),
        START_MENU_PROGRAMS("StartMenuPrograms"),
        START_MENU_STARTUP("StartMenuStartup");
        
        private final String name;
        SpecialLocations(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    /**
	 * Gets the complete path to a special folder in the platform operating system shell.
	 * 
	 * The returned path is specific to the current user, and current to how the user has customized it.
	 * Here are the given special folder names and example return paths this method currently supports on Windows:
	 * 
	 * <pre>
	 * Documents         C:\Documents and Settings\UserName\My Documents
	 * ApplicationData   C:\Documents and Settings\UserName\Application Data
	 * Desktop           C:\Documents and Settings\UserName\Desktop
	 * StartMenu         C:\Documents and Settings\UserName\Start Menu
	 * StartMenuPrograms C:\Documents and Settings\UserName\Start Menu\Programs
	 * StartMenuStartup  C:\Documents and Settings\UserName\Start Menu\Programs\Startup
	 * </pre>
	 * 
	 * @param name The name of a special folder
	 * @return     The path to that folder, or null on error
	 */
    public static final String getSpecialPath(SpecialLocations location) {
    	if (OSUtils.isWindows() && isLoaded) {
            try {
        		String path = getSpecialPathNative(location.getName());
        		if(!path.equals(""))
                    return path;
            } catch(UnsatisfiedLinkError error) {
                // Must catch the error because earlier versions of the dll didn't
                // include this method, and installs that happen to have not
                // updated this dll for whatever reason will receive the error
                // otherwise.
                LOG.error("Unable to use getSpecialPath!", error);
            }
    	}
    	return null;
    }    

    /**
     * Changes the icon of a window.
     * Puts the given icon in the title bar, task bar, and Alt+Tab box.
     * Replaces the Swing icon with a real Windows .ico icon that supports multiple sizes, full color, and partially transparent pixels.
     * 
     * @param frame The AWT Component, like a JFrame, that is backed by a native window
     * @param icon  The path to a .exe or .ico file on the disk
     * @return      False on error
     */
    public static final boolean setWindowIcon(Component frame, File icon) {
    	if (OSUtils.isWindows() && isLoaded) {
    		String result = setWindowIconNative(frame, System.getProperty("sun.boot.library.path"), icon.getPath());
    	    return result.equals(""); // Returns blank on success, or information about an error
    	}
        
    	return false;
    }
    
    /**
     * Sets a Component to be topmost.
     */
    public static final boolean setWindowTopMost(Component frame) {
        if(isLoaded && OSUtils.isWindows()) {
            String result = setWindowTopMostNative(frame, System.getProperty("sun.boot.library.path"));
            return result.equals("");
        }
        
        return false;
    }
    
    public static final boolean toggleFullScreen(long hwnd) {
        if(isLoaded && (OSUtils.isWindows() || OSUtils.isLinux()) ) {
            return toggleFullScreenNative(hwnd);
        }
        
        return false;
    }
    
    /**
     * Flushes the icon cache on the OS, forcing any icons to be redrawn
     * with the current-most icon.
     */
    public static final boolean flushIconCache() {
        if(isLoaded && OSUtils.isWindows()) {
            return flushIconCacheNative();
        }
        
        return false;
    }

    /**
     * Reads a numerical value stored in the Windows Registry.
     * 
     * @param root The name of the root registry key, like "HKEY_LOCAL_MACHINE"
     * @param path The path to the registry key with backslashes as separators, like "Software\\Microsoft\\Windows"
     * @param name The name of the variable within that key, or blank to access the key's default value
     * @return     The number value stored there, or 0 on error
     */
    public static final int registryReadNumber(String root, String path, String name) throws IOException {
    	if (OSUtils.isWindows() && isLoaded)
    		return registryReadNumberNative(root, path, name);
    	throw new IOException(" not supported ");
    }

    /**
     * Reads a text value stored in the Windows Registry.
     * 
     * @param root The name of the root registry key, like "HKEY_LOCAL_MACHINE"
     * @param path The path to the registry key with backslashes as separators, like "Software\\Microsoft\\Windows"
     * @param name The name of the variable within that key, or blank to access the key's default value
     * @return     The text value stored there or blank on error
     */
    public static final String registryReadText(String root, String path, String name) throws IOException {
    	if (OSUtils.isWindows() && isLoaded)
    		return registryReadTextNative(root, path, name);
    	throw new IOException(" not supported ");
    }

    /**
     * Sets a numerical value in the Windows Registry.
     * 
     * @param root  The name of the root registry key, like "HKEY_LOCAL_MACHINE"
     * @param path  The path to the registry key with backslashes as separators, like "Software\\Microsoft\\Windows"
     * @param name  The name of the variable within that key, or blank to access the key's default value
     * @param value The number value to set there
     * @return      False on error
     */
    public static final boolean registryWriteNumber(String root, String path, String name, int value) {
    	if (OSUtils.isWindows() && isLoaded)
    		return registryWriteNumberNative(root, path, name, value);
    	else
    		return false;
    }

    /**
     * Sets a text value in the Windows Registry.
     * 
     * @param root  The name of the root registry key, like "HKEY_LOCAL_MACHINE"
     * @param path  The path to the registry key with backslashes as separators, like "Software\\Microsoft\\Windows"
     * @param name  The name of the variable within that key, or blank to access the key's default value
     * @param value The text value to set there
     * @return      False on error
     */
    public static final boolean registryWriteText(String root, String path, String name, String value) {
    	if (OSUtils.isWindows() && isLoaded)
    		return registryWriteTextNative(root, path, name, value);
    	else
    		return false;
    }

    /**
     * Deletes a key in the Windows Registry.
     * 
     * @param root The name of the root registry key, like "HKEY_LOCAL_MACHINE"
     * @param path The path to the registry key with backslashes as separators, like "Software\\Microsoft\\Windows"
     * @return     False on error
     */
    public static final boolean registryDelete(String root, String path) {
    	if (OSUtils.isWindows() && isLoaded)
    		return registryDeleteNative(root, path);
    	else
    		return false;
    }

    /**
     * Determine if this Windows computer has Windows Firewall on it.
     * 
     * @return True if it does, false if it does not or there was an error
     */
    public static final boolean isFirewallPresent() {
    	if (OSUtils.isWindows() && isLoaded)
    		return firewallPresentNative();
    	return false;
    }

    /**
     * Determine if the Windows Firewall is enabled.
     * 
     * @return True if the setting on the "General" tab is "On (recommended)".
     *         False if the setting on the "General" tab is "Off (not recommended)".
     *         False on error.
     */
    public static final boolean isFirewallEnabled() {
    	if (OSUtils.isWindows() && isLoaded)
    	    return firewallEnabledNative();
    	return false;
    }

    /**
     * Determine if the Windows Firewall is on with no exceptions.
     * 
     * @return True if the box on the "General" tab "Don't allow exceptions" is checked.
     *         False if the box is not checked.
     *         False on error.
     */
    public static final boolean isFirewallExceptionsNotAllowed() {
    	if (OSUtils.isWindows() && isLoaded)
    		return firewallExceptionsNotAllowedNative();
    	return false;
    }

    /**
     * Determine if a program is listed on the Windows Firewall exceptions list.
     * 
     * @param path The path to the program, like "C:\Program Files\LimeWire\LimeWire.exe"
     * @return     True if it has a listing on the Exceptions list, false if not or on error
     */
    public static final boolean isProgramListedOnFirewall(String path) {
    	if (OSUtils.isWindows() && isLoaded)
    		return firewallIsProgramListedNative(path);
    	return false;
    }

    /**
     * Determine if a program's listing on the Windows Firewall exceptions list has a check box making it enabled.
     * 
     * @param path The path to the program, like "C:\Program Files\LimeWire\LimeWire.exe"
     * @return     True if it's listing's check box is checked, false if not or on error
     */
    public static final boolean isProgramEnabledOnFirewall(String path) {
    	if (OSUtils.isWindows() && isLoaded)
    		return firewallIsProgramEnabledNative(path);
    	return false;
    }

    /**
     * Add a program to the Windows Firewall exceptions list.
     * 
     * @param path The path to the program, like "C:\Program Files\LimeWire\LimeWire.exe"
     * @param name The name of the program, like "LimeWire", this is the text that will identify the item on the list
     * @return     False if error
     */
    public static final boolean addProgramToFirewall(String path, String name) {
    	if (OSUtils.isWindows() && isLoaded)
    		return firewallAddNative(path, name);
    	return false;
    }

    /**
     * Remove a program from the Windows Firewall exceptions list.
     * 
     * @param path The path to the program, like "C:\Program Files\LimeWire\LimeWire.exe"
     * @return     False if error.
     */
    public static final boolean removeProgramFromFirewall(String path) {
    	if (OSUtils.isWindows() && isLoaded)
    		return firewallRemoveNative(path);
    	return false;
    }

    /**
     * Opens a Web address using the default browser on the native platform.
     * 
     * This method returns immediately, not later after the browser exits.
     * On Windows, this method does the same thing as Start, Run.
     * 
     * @param url The Web address to open, like "http://www.frostwire.com/"
     * @return    0, in place of the process exit code
     */
    public static int openURL(String url) throws IOException {
        if(OSUtils.isWindows() && isLoaded) {
            openURLNative(url);
            return 0; // program's still running, no way of getting an exit code.
        }
        
        throw new IOException("native code not linked");
    }

    /**
     * Runs a path using the default program on the native platform.
     * 
     * Given a path to a program, runs that program.
     * Given a path to a document, opens it in the default program for that kind of document.
     * Given a path to a folder, opens it in the shell.
     * 
     * This method returns immediately, not later after the program exits.
     * On Windows, this method does the same thing as Start, Run.
     * 
     * @param path The complete path to run, like "C:\folder\file.ext"
     * @return     0, in place of the process exit code
     */
    public static int openFile(String path) throws IOException {
        if(OSUtils.isWindows() && isLoaded) {
            openFileNative(path);
            return 0; // program's running, no way to get exit code.
        }
        
        throw new IOException("native code not linked");
    }

    /**
     * Runs a path using the default program on the native platform.
     * 
     * Given a path to a program, runs that program.
     * Given a path to a document, opens it in the default program for that kind of document.
     * Given a path to a folder, opens it in the shell.
     * 
     * Note: this method accepts a parameter list thus should
     *        be generally used with executable files 
     * 
     * This method returns immediately, not later after the program exits.
     * On Windows, this method does the same thing as Start, Run.
     * 
     * @param path The complete path to run, like "C:\folder\file.ext"
     * @param path The list of parameters to pass to the file 
     * @return     0, in place of the process exit code
     */
    public static int openFile(String path, String params) throws IOException {
        if(OSUtils.isWindows() && isLoaded) {
            openFileParamsNative(path, params);
            return 0; // program's running, no way to get exit code.
        }
        
        throw new IOException("native code not linked");
    }
    
    public static String getShortFileName(String fileName) {
        	if(OSUtils.isWindows() && isLoaded) {
        		return getShortFileNameNative(fileName);
        	} else {
        		return fileName;
        	}
    }
    
    /**
     * Moves a file to the platform-specific trash can or recycle bin.
     * 
     * @param file The file to trash
     * @return     True on success
     */
    public static boolean recycle(File file) {
    	if (OSUtils.isWindows() && isLoaded) {

    		// Get the path to the file
    		String path = null;
			try {
				path = file.getCanonicalPath();
			} catch (IOException err) {
				LOG.error("IOException", err);
				path = file.getAbsolutePath();
			}

			// Use native code to move the file at that path to the recycle bin
			return recycleNative(path);

    	} else {
    		return false;
    	}
    }
    
    /**
     * @return the default String that the shell will execute to open
     * a file with the provided extention.
     * Only supported on windows.
     */
    public static String getDefaultExtentionHandler(String extention) {
    	if (!OSUtils.isWindows() || !isLoaded)
    		return null;

    	if (!extention.startsWith("."))
    		extention = "."+extention;
    	try {
    		String progId = registryReadText("HKEY_CLASSES_ROOT", extention,"");
    		if ("".equals(progId))
    			return "";
    		return registryReadText("HKEY_CLASSES_ROOT",
    				progId+"\\shell\\open\\command","");
    	} catch (IOException iox) {
    		return null;
    	}
    }
    
    /**
     * @return the default String that the shell will execute to open
     * content with the provided mime type.
     * Only supported on windows.
     */
    public static String getDefaultMimeHandler(String mimeType) {
    	if (!OSUtils.isWindows() || !isLoaded)
    		return null;
    	String extention = "";
    	try {
    		extention = registryReadText("HKEY_CLASSES_ROOT", 
    				"MIME\\Database\\Content Type\\"+mimeType, 
    				"Extension");
    	} catch (IOException iox) {
    		return null;
    	}
    	
    	if ("".equals(extention))
    		return "";
    	return getDefaultExtentionHandler(extention);
    }

    /*
     * The following methods are implemented in C++ code in SystemUtilities.dll.
     * In addition, setFileWritable(String) and idleTime() may be implemeted in FrostWire's native library for another platform, like Mac or Linux.
     * The idea is that the Windows, Mac, and Linux libraries have methods with the same names.
     * Call a method, and it will run platform-specific code to complete the task in the appropriate platform-specific way.
     */

    private static final native String getRunningPathNative();
    private static final native String getSpecialPathNative(String name);
    private static final native String getShortFileNameNative(String fileName);
    private static final native void openURLNative(String url);
    private static final native void openFileNative(String path);
    private static final native void openFileParamsNative(String path, String params);
    private static final native boolean recycleNative(String path);
    private static final native int setFileWriteable(String path);
    private static final native long idleTime();
    private static final native String setWindowIconNative(Component frame, String bin, String icon);
    private static final native String setWindowTopMostNative(Component frame, String bin);
    private static final native boolean flushIconCacheNative();
    private static final native boolean toggleFullScreenNative(long hwnd);
    
    private static final native int registryReadNumberNative(String root, String path, String name) throws IOException ;
    private static final native String registryReadTextNative(String root, String path, String name) throws IOException;
    private static final native boolean registryWriteNumberNative(String root, String path, String name, int value);
    private static final native boolean registryWriteTextNative(String root, String path, String name, String value);
    private static final native boolean registryDeleteNative(String root, String path);

    private static final native boolean firewallPresentNative();
    private static final native boolean firewallEnabledNative();
    private static final native boolean firewallExceptionsNotAllowedNative();
    private static final native boolean firewallIsProgramListedNative(String path);
    private static final native boolean firewallIsProgramEnabledNative(String path);
    private static final native boolean firewallAddNative(String path, String name);
    private static final native boolean firewallRemoveNative(String path);
}
