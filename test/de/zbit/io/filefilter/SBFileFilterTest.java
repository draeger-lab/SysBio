/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.io.filefilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class SBFileFilterTest {
  //
  //  /**
  //   * @throws java.lang.Exception
  //   */
  //  @Before
  //  public void setUp() throws Exception {
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#accept(java.io.File)}.
  //   */
  //  @Test
  //  public void testAcceptFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#checkExtension(java.io.File, java.lang.String)}.
  //   */
  //  @Test
  //  public void testCheckExtension() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createAllFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateAllFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createASSOCFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateASSOCFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createBEDFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateBEDFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createBIMFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateBIMFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createBioPAXFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateBioPAXFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createBioPAXFileFilterL2()}.
  //   */
  //  @Test
  //  public void testCreateBioPAXFileFilterL2() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createBioPAXFileFilterL3()}.
  //   */
  //  @Test
  //  public void testCreateBioPAXFileFilterL3() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createCDTFileFilterList()}.
  //   */
  //  @Test
  //  public void testCreateCDTFileFilterList() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createCSVFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateCSVFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createDirectoryFilter()}.
  //   */
  //  @Test
  //  public void testCreateDirectoryFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createFAMFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateFAMFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createGIFFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateGIFFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createGMLFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateGMLFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createGraphMLFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateGraphMLFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createHTMLFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateHTMLFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createHWEFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateHWEFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createImageFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateImageFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createJPEGFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateJPEGFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createJSONFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateJSONFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createKGMLFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateKGMLFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createLogFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateLogFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createMAPFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateMAPFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createMATFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateMATFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createOWLFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateOWLFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createPDFFileFilter()}.
  //   */
  //  @Test
  //  public void testCreatePDFFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createPEDFileFilter()}.
  //   */
  //  @Test
  //  public void testCreatePEDFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createPlinkGenotypeFileFilter()}.
  //   */
  //  @Test
  //  public void testCreatePlinkGenotypeFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createPNGFileFilter()}.
  //   */
  //  @Test
  //  public void testCreatePNGFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBGNFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateSBGNFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL1V1()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL1V1() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL1V2()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL1V2() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL2V1()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL2V1() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL2V2()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL2V2() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL2V3()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL2V3() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL2V4()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL2V4() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL2V5()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL2V5() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL3V1()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL3V1() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterL3V2()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterL3V2() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSBMLFileFilterList()}.
  //   */
  //  @Test
  //  public void testCreateSBMLFileFilterList() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSIFFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateSIFFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createSVGFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateSVGFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createTeXFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateTeXFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createTextFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateTextFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createTGFFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateTGFFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createTSVFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateTSVFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#createYGFFileFilter()}.
  //   */
  //  @Test
  //  public void testCreateYGFFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#getExtension(java.io.File)}.
  //   */
  //  @Test
  //  public void testGetExtensionFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#getExtension(java.lang.String)}.
  //   */
  //  @Test
  //  public void testGetExtensionString() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#hasFileType(java.io.File, de.zbit.io.filefilter.SBFileFilter.FileType)}.
  //   */
  //  @Test
  //  public void testHasFileTypeFileFileType() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#hasFileType(java.io.File, de.zbit.io.filefilter.SBFileFilter.FileType, boolean)}.
  //   */
  //  @Test
  //  public void testHasFileTypeFileFileTypeBoolean() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isCSVFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsCSVFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isHTMLFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsHTMLFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isJPEGFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsJPEGFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isJSONFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsJSONFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isKGML(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsKGML() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isLogFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsLogFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isOWLFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsOWLFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isPDFFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsPDFFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isPNGFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsPNGFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isSBGNFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsSBGNFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isSBMLFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsSBMLFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isTeXFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsTeXFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isTextFile(java.io.File)}.
  //   */
  //  @Test
  //  public void testIsTextFile() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsBioPAXFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsBioPAXFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsCDTFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsCDTFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsCSVFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsCSVFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsJPEGFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsJPEGFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsOWLFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsOWLFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsPNGFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsPNGFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsSBGNFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsSBGNFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsSBMLFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsSBMLFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsSVGFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsSVGFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsTeXFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsTeXFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsTextFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsTextFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#acceptsTSVFiles()}.
  //   */
  //  @Test
  //  public void testAcceptsTSVFiles() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#filter(java.io.File[])}.
  //   */
  //  @Test
  //  public void testFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#getDescription()}.
  //   */
  //  @Test
  //  public void testGetDescription() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#getDescription(boolean)}.
  //   */
  //  @Test
  //  public void testGetDescriptionBoolean() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#getExtension()}.
  //   */
  //  @Test
  //  public void testGetExtension() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#getExtensions()}.
  //   */
  //  @Test
  //  public void testGetExtensions() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#isSetFileType()}.
  //   */
  //  @Test
  //  public void testIsSetFileType() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.SBFileFilter#separate(java.io.File[])}.
  //   */
  //  @Test
  //  public void testSeparate() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.GeneralFileFilter#isSetFileFilter()}.
  //   */
  //  @Test
  //  public void testIsSetFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  //  /**
  //   * Test method for {@link de.zbit.io.filefilter.GeneralFileFilter#setFileFilter(java.io.FileFilter)}.
  //   */
  //  @Test
  //  public void testSetFileFilter() {
  //    fail("Not yet implemented"); // TODO
  //  }
  //
  
  /**
   * Test method for {@link de.zbit.io.filefilter.SBFileFilter.FileType#getLinePattern()}
   */
  @Test
  public void testGetLinePattern() {
    Pattern pattern = SBFileFilter.FileType.SBGN_FILES.getLinePattern();
    
    assertTrue(pattern.matcher("<sbgn xmlns=\"http://sbgn.org/libsbgn/0.2\">").matches());
    assertTrue(pattern.matcher("<sbgn xmlns=\"http://sbgn.org/libsbgn/pd/0.1\">").matches());
    
    assertFalse(pattern.matcher("<sbgn xmlns=\"http://sbgn.org/libsbgn/0,2\">").matches());
    assertFalse(pattern.matcher("<sbgn xmlns=\"http://sbgn.org/libsbgn/pd0.1\">").matches());
  }
  
}
