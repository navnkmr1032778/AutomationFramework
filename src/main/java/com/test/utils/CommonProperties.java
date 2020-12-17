package com.test.utils;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class CommonProperties extends Properties {

	/**
	 * logging object
	 */
	protected static Logger log = Logger.getLogger(CommonProperties.class);

	/**
	 * Indicator if the properties have been successfully loaded. This variable is
	 * set to <code>true</code> if a properties file load has occured successfully.
	 */
	private boolean hasbeenSuccessfullyLoaded = false;

	/**
	 * Reference to this object
	 */
	private static CommonProperties instance = new CommonProperties();

	/**
	 * Default Constructor
	 */
	protected CommonProperties() {
	}

	/**
	 * Return the instance of this object.
	 * 
	 * @return The instance of this object
	 */
	public static CommonProperties getInstance() {
		return instance;
	}

	/**
	 * Indicator if the properties have been successfully loaded.
	 * 
	 * @return
	 */
	public boolean hasbeenSuccessfullyLoaded() {
		return hasbeenSuccessfullyLoaded;
	}

	/**
	 * Get a properties object given a filename.
	 * 
	 * @param filename The path to the file to use (relative or absolute path)
	 * 
	 * @throws Exception
	 */
	public void load(String filename) throws Exception {

		try {

			super.load(new FileInputStream(filename));

			hasbeenSuccessfullyLoaded = true;

		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Get a property value for a given <code>key</code>.
	 * 
	 * @param key
	 * 
	 * @return A property value for a given key
	 */
	public String get(String key) {
		return getProperty(key);
	}

}