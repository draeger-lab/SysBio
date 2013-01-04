/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.io.fileformat;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Read {@link metadata.FormatDescription} objects from a semicolon-separated text file.
 * @author Marco Schmidt
 * @version $Rev$
 * @since 1.0
 */
public class FormatDescriptionReader
{
	private BufferedReader in;

	public FormatDescriptionReader(Reader reader)
	{
		in = new BufferedReader(reader);
	}

	public FormatDescription read() throws IOException
	{
		String line;
		do
		{
			line = in.readLine();
			if (line == null)
			{
				return null;
			}
		}
		while (line.length() < 1 || line.charAt(0) == '#');
		String[] items = line.split(";");
		if (items == null || items.length < 8)
		{
			throw new IOException("Could not interpret line: " +
				line);
		}
		FormatDescription desc = new FormatDescription();
		desc.setGroup(items[0]);
		desc.setShortName(items[1]);
		desc.setLongName(items[2]);
		desc.addMimeTypes(items[3]);
		desc.addFileExtensions(items[4]);
		desc.setOffset(new Integer(items[5]));
		desc.setMagicBytes(items[6]);
		desc.setMinimumSize(new Integer(items[7]));
		return desc;
	}
}
