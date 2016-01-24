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
package de.zbit.locales;

import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;

/**
 * Interface for holding the localization IDs as constants. The strings still
 * need to be manually kept in sync with the string the actual resource file
 * (Labels.xml), but it is easier to refactor in the program this way.
 * 
 * @author Florian Mittag
 */
public interface Labels {
  
  public static ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
  
  /*
   * You can use this regular expression to quickly convert the content of an
   * XML file to the constants here:
   * 
   * Find:         <entry key="([^"]+)">.*</entry>
   * Replace with: public static final String \1 = "\1";
   * 
   * Find:         <!-- (.*) -->
   * Replace with: \/\* \1 \*\/
   */
  
  
  /* BaseFrame */
  
  public static final String ABOUT_THE_PROGRAM = "ABOUT_THE_PROGRAM";
  public static final String DEFAULT_TOOL_BAR_TITLE = "DEFAULT_TOOL_BAR_TITLE";
  public static final String LICENSE_OF_THE_PROGRAM = "LICENSE_OF_THE_PROGRAM";
  public static final String ONLINE_HELP_FOR_THE_PROGRAM = "ONLINE_HELP_FOR_THE_PROGRAM";
  public static final String STATUS_BAR = "STATUS_BAR";
  public static final String VIEW = "VIEW";
  
  /* CSVImporter */
  
  public static final String COULD_NOT_IDENTIFY_COLUMNS = "COULD_NOT_IDENTIFY_COLUMNS";
  public static final String COLUMN_ASSIGNMENT = "COLUMN_ASSIGNMENT";
  
  /* CSVImporterV2 */
  
  public static final String CSV_IMPORT_TITLE = "CSV_IMPORT_TITLE";
  
  /* CSVOptions */
  
  public static final String CSVOptions = "CSVOptions";
  public static final String CSV_FILE = "CSV_FILE";
  public static final String CSV_FILE_TOOLTIP = "CSV_FILE_TOOLTIP";
  public static final String CSV_FILES_OPEN_DIR = "CSV_FILES_OPEN_DIR";
  public static final String CSV_FILES_OPEN_DIR_TOOLTIP = "CSV_FILES_OPEN_DIR_TOOLTIP";
  public static final String CSV_FILES_QUOTE_CHAR = "CSV_FILES_QUOTE_CHAR";
  public static final String CSV_FILES_QUOTE_CHAR_TOOLTIP = "CSV_FILES_QUOTE_CHAR_TOOLTIP";
  public static final String CSV_FILES_SAVE_DIR = "CSV_FILES_SAVE_DIR";
  public static final String CSV_FILES_SAVE_DIR_TOOLTIP = "CSV_FILES_SAVE_DIR_TOOLTIP";
  public static final String CSV_FILES_SEPARATOR_CHAR = "CSV_FILES_SEPARATOR_CHAR";
  public static final String CSV_FILES_SEPARATOR_CHAR_TOOLTIP = "CSV_FILES_SEPARATOR_CHAR_TOOLTIP";
  public static final String CSV_FILE_SELECTION = "CSV_FILE_SELECTION";
  public static final String CSV_FILE_SELECTION_TOOLTIP = "CSV_FILE_SELECTION_TOOLTIP";
  public static final String CSV_FILE_CHARACTERS = "CSV_FILE_CHARACTERS";
  public static final String CSV_FILE_CHARACTERS_TOOLTIP = "CSV_FILE_CHARACTERS_TOOLTIP";
  
  /* CSVReaderColumnChooser */
  
  public static final String REQUIRED_COLUMNS = "REQUIRED_COLUMNS";
  public static final String OPTIONAL_COLUMNS = "OPTIONAL_COLUMNS";
  public static final String ASSIGN_THESE_COLUMNS = "ASSIGN_THESE_COLUMNS";
  
  /* CSVReaderOptionPanel */
  
  public static final String COLUMN = "COLUMN";
  public static final String CSV_OPTIONS = "CSV_OPTIONS";
  public static final String FILE_PREVIEW = "FILE_PREVIEW";
  public static final String FILE_CONTAINS_HEADERS = "FILE_CONTAINS_HEADERS";
  public static final String INDICATE_IF_COLUMN_DESCRIPTIONS_ARE_GIVEN = "INDICATE_IF_COLUMN_DESCRIPTIONS_ARE_GIVEN";
  public static final String SEPARATOR_CHAR = "SEPARATOR_CHAR";
  public static final String SEPARATOR_CHAR_TOOLTIP = "SEPARATOR_CHAR_TOOLTIP";
  public static final String TREAT_CONSECUTIVE_SEPARATORS_AS_ONE = "TREAT_CONSECUTIVE_SEPARATORS_AS_ONE";
  public static final String TREAT_CONSECUTIVE_SEPARATORS_AS_ONE_TOOLTIP = "TREAT_CONSECUTIVE_SEPARATORS_AS_ONE_TOOLTIP";
  public static final String SKIP_THIS_NUMBER_OF_LEADING_LINES = "SKIP_THIS_NUMBER_OF_LEADING_LINES";
  public static final String SKIP_LINES = "SKIP_LINES";
  
  /* ColorEditor */
  
  public static final String PICK_A_COLOR = "PICK_A_COLOR";
  
  /* FileHistory */
  
  public static final String LAST_OPENED = "LAST_OPENED";
  
  /* FileSelector */
  
  public static final String FILE = "FILE";
  public static final String DIRECTORY = "DIRECTORY";
  public static final String OPEN = "OPEN";
  public static final String SAVE = "SAVE";
  public static final String SELECT_TARGET_FILE = "SELECT_TARGET_FILE";
  public static final String TO_BE_OPENED = "TO_BE_OPENED";
  public static final String TO_BE_SAVED = "TO_BE_SAVED";
  public static final String SELECT_TARGET_DIRECTORY = "SELECT_TARGET_DIRECTORY";
  
  /* GUIOptions */
  
  public static final String GUIOptions = "GUIOptions";
  public static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";
  public static final String GUI = "GUI";
  public static final String OPEN_DIR = "OPEN_DIR";
  public static final String SAVE_DIR = "SAVE_DIR";
  public static final String DEFAULT_DIRECTORIES = "DEFAULT_DIRECTORIES";
  public static final String DEFAULT_DIRECTORIES_TOOLTIP = "DEFAULT_DIRECTORIES_TOOLTIP";
  public static final String WINDOW_WIDTH = "WINDOW_WIDTH";
  public static final String WINDOW_HEIGHT = "WINDOW_HEIGHT";
  
  /* GUITools */
  
  public static final String NO_READ_ACCESS_MESSAGE = "NO_READ_ACCESS_MESSAGE"; /* first %s will be replaced by THE_FILE or THE_DIRECTORY and the second %s by the absolute path */
  public static final String NO_READ_ACCESS_TITLE = "NO_READ_ACCESS_TITLE";
  public static final String NO_WRITE_ACCESS_MESSAGE = "NO_WRITE_ACCESS_MESSAGE"; /* first %s will be replaced by THE_FILE or THE_DIRECTORY and the second %s by the absolute path */
  public static final String NO_WRITE_ACCESS_TITLE = "NO_WRITE_ACCESS_TITLE";
  public static final String OVERRIDE_EXISTING_FILE_QUESTION = "OVERRIDE_EXISTING_FILE_QUESTION"; /* first %s will be replaced by THE_FILE or THE_DIRECTORY and the second %s by the absolute path */
  public static final String OVERRIDE_EXISTING_FILE_TITLE = "OVERRIDE_EXISTING_FILE_TITLE";
  public static final String THE_DIRECTORY = "THE_DIRECTORY";
  public static final String THE_FILE = "THE_FILE";
  
  /* JColumnChooser */
  
  public static final String EG = "EG";
  public static final String NOT_AVAILABLE = "NOT_AVAILABLE";
  public static final String INVALID_COLUMN = "INVALID_COLUMN";
  
  /* JDatePanel */
  
  public static final String CALENDAR = "CALENDAR";
  public static final String DATE = "DATE";
  public static final String DATE_TOOLTIP = "DATE_TOOLTIP";
  public static final String DATE_FORMAT = "DATE_FORMAT";
  
  /* JHelpBrowser */
  
  public static final String ONLINE_HELP = "ONLINE_HELP";
  public static final String ONLINE_HELP_TOOLTIP = "ONLINE_HELP_TOOLTIP";
  public static final String COMMAND_LINE_ARGUMENTS = "COMMAND_LINE_ARGUMENTS";
  public static final String LAST_PAGE = "LAST_PAGE";
  public static final String NEXT = "NEXT";
  public static final String NEXT_PAGE = "NEXT_PAGE";
  // public static final String SAVE = "SAVE";
  public static final String SAVE_TOOLTIP = "SAVE_TOOLTIP";
  public static final String OPEN_IN_BROWSER = "OPEN_IN_BROWSER";
  public static final String OPEN_IN_BROWSER_TOOLTIP = "OPEN_IN_BROWSER_TOOLTIP";
  
  /* KeyProvider */
  
  public static final String ADDITIONAL_OPTIONS = "ADDITIONAL_OPTIONS";
  public static final String ALL_POSSIBLE_VALUES_FOR_TYPE = "ALL_POSSIBLE_VALUES_FOR_TYPE";
  public static final String ARE = "ARE";
  public static final String AND = "AND";
  public static final String OR = "OR";
  public static final String NOT = "NOT";
  public static final String REGULAR_EXPRESSION = "REGULAR_EXPRESSION";
  public static final String ARGS_MUST_FIT_INTO_RANGE = "ARGS_MUST_FIT_INTO_RANGE";
  public static final String COMMAND_LINE_HELP = "COMMAND_LINE_HELP"; /* COMMAND_LINE_ARGUMENTS */
  public static final String DEFAULT_VALUE = "DEFAULT_VALUE";
  public static final String PROGRAM_USAGE = "PROGRAM_USAGE";
  public static final String STARTS_PROGRAM = "STARTS_PROGRAM"; /* Program name */
  
  /* MultiplePreferencesPanel */
  
  public static final String USER_PREFERENCES = "USER_PREFERENCES";
  
  /* Option */
  
  public static final String ACCEPTS = "ACCEPTS";
    
  /* PreferencesDialog */
  
  public static final String APPLY = "APPLY";
  public static final String BROWSE = "BROWSE";
  public static final String CANCEL = "CANCEL";
  public static final String DEFAULTS = "DEFAULTS";
  public static final String OK = "OK";
  public static final String PREFERENCES = "PREFERENCES";
  
  /* ProgressBarSwing */
  
  public static final String REMAINING_TIME = "REMAINING_TIME";
  
  /* SBPreferences */
  
  public static final String OPTIONS = "OPTIONS";
  
  /* SBFileFilter */
  
  public static final String ASSOC_FILES = "ASSOC_FILES";
  public static final String BED_FILES = "BED_FILES";
  public static final String BIM_FILES = "BIM_FILES";
  public static final String CSV_FILES = "CSV_FILES";
  public static final String DIRECTORIES_ONLY = "DIRECTORIES_ONLY";
  public static final String FAM_FILES = "FAM_FILES";
  public static final String GIF_FILES = "GIF_FILES";
  public static final String GML_FILES = "GML_FILES";
  public static final String GRAPHML_FILES = "GRAPHML_FILES";
  public static final String HTML_FILES = "HTML_FILES";
  public static final String IMAGE_FILES = "IMAGE_FILES";
  public static final String JPEG_FILES = "JPEG_FILES";
  public static final String KGML_FILES = "KGML_FILES";
  public static final String MAP_FILES = "MAP_FILES";
  public static final String PDF_FILES = "PDF_FILES";
  public static final String PED_FILES = "PED_FILES";
  public static final String PLINK_GENOTYPE_FILES = "PLINK_GENOTYPE_FILES";
  public static final String PNG_FILES = "PNG_FILES";
  public static final String SBGN_FILES = "SBGN_FILES";
  public static final String SBML_FILES = "SBML_FILES";
  public static final String SBML_FILES_L1V1 = "SBML_FILES_L1V1";
  public static final String SBML_FILES_L1V2 = "SBML_FILES_L1V2";
  public static final String SBML_FILES_L2V1 = "SBML_FILES_L2V1";
  public static final String SBML_FILES_L2V2 = "SBML_FILES_L2V2";
  public static final String SBML_FILES_L2V3 = "SBML_FILES_L2V3";
  public static final String SBML_FILES_L2V4 = "SBML_FILES_L2V4";
  public static final String SBML_FILES_L3V1 = "SBML_FILES_L3V1";
  public static final String SVG_FILES = "SVG_FILES";
  public static final String TeX_FILES = "TeX_FILES";
  public static final String TEXT_FILES = "TEXT_FILES";
  public static final String TGF_FILES = "TGF_FILES";
  public static final String TSV_FILES = "TSV_FILES";
  public static final String YGF_FILES = "YGF_FILES";
  
  public static final String OWL_FILES = "OWL_FILES";
  public static final String BioPAX_FILES = "BioPAX_FILES";
  public static final String BioPAX_FILES_L2 = "BioPAX_FILES_L2";
  public static final String BioPAX_FILES_L3 = "BioPAX_FILES_L3";
  public static final String SIF_FILES = "SIF_FILES";
  
  /* SBAcceptAllFileFilter */
  
  public static final String ACCEPT_ALL_FILES = "ACCEPT_ALL_FILES";
  
  /* StatusBar */
  
  public static final String READY = "READY";
  
  /* UpdateMessage */
  
  public static final String SHOW_RELEASE_NOTES = "SHOW_RELEASE_NOTES";
  public static final String HIDE_RELEASE_NOTES = "HIDE_RELEASE_NOTES";
  public static final String UPDATE_IS_AVAILABLE = "UPDATE_IS_AVAILABLE";
  public static final String COMMAND_LINE_UPDATE_MESSAGE = "COMMAND_LINE_UPDATE_MESSAGE";
  public static final String NO_UPDATE_AVAILABLE_FOR_CURRENT_VERSION_MESSAGE = "NO_UPDATE_AVAILABLE_FOR_CURRENT_VERSION_MESSAGE";
  public static final String NO_UPDATE_AVAILABLE_FOR_CURRENT_VERSION_TITLE = "NO_UPDATE_AVAILABLE_FOR_CURRENT_VERSION_TITLE"; 

  /* Wizard */
  
  public static final String WIZARD_NEXT = "WIZARD_NEXT";
  public static final String WIZARD_BACK = "WIZARD_BACK";
  public static final String WIZARD_CANCEL = "WIZARD_CANCEL";
  public static final String WIZARD_FINISH = "WIZARD_FINISH";
  public static final String WIZARD_HELP = "WIZARD_HELP";

}
