/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import de.zbit.io.SBFileFilter;
import de.zbit.util.ResourceManager;

/**
 * This class contains tools for java.awt.image, java.awt.image.BufferedImage,
 * etc.
 * 
 * @author Clemens Wrzodek
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class ImageTools {
  
  public static final Logger log = Logger.getLogger(ImageTools.class
      .getName());
  
  /**
   * This bundle is initialized in the static constructor
   */
	private static ResourceBundle bundle = null;
	
	static {
	  // Allow using these tools, without localized warnings!
	  try {
	    bundle = ResourceManager.getBundle("de.zbit.locales.Warnings");
	  } catch (Throwable t) {
	    log.log(Level.WARNING, "Could not load bundle with warning locales in ImageTools.");
	  }
	}
	
	
	/**
	 * Blur ("unsharpen") the given image.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static BufferedImage blur(BufferedImage bufferedImage) {
		if (bufferedImage == null)
			return bufferedImage;
		if (bufferedImage.getType() == BufferedImage.TYPE_CUSTOM) {
			// System.err.println("This function does not work with custom type images. Please use image2BufferedImage().");
			// return bufferedImage;
			bufferedImage = image2BufferedImage(bufferedImage);
		}

		Kernel kernel = new Kernel(3, 3, new float[] { 1f / 9f, 1f / 9f,
				1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f });
		BufferedImageOp op = new ConvolveOp(kernel);
		bufferedImage = op.filter(bufferedImage, null);

		/*
		 * float[] blurKernel = { 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 /
		 * 9f, 1 / 9f, 1 / 9f, 1 / 9f }; BufferedImageOp blur = new
		 * ConvolveOp(new Kernel(3, 3, blurKernel)); bufferedImage =
		 * blur.filter(bufferedImage, null);
		 */

		return bufferedImage;
	}

	private static int brightenColorPart (int colorPart, double percentage, boolean brighten) {
	   if (brighten) {
	     return (int)(colorPart+((255-colorPart)/100.0*percentage));
	   } else {
	     return (int)(((colorPart)/100.0*percentage));
	   }
	 }
	
	
 /**
 * Brightens an image by the given percentage.
 * @param image - image to brighten
 * @param percent a value between 0 and 1 (0 to 100 percent).
 * @return brightened image
 */
public static BufferedImage brightenImage(BufferedImage image, float percent) {
	if ((percent < 0) ) { //|| (percent > 1)
    log.log(Level.SEVERE, MessageFormat.format(bundle.getString("WRONG_PERCENTAGE_IN_IMAGE"), "brightenImage", percent));
    return image;
  }
	// Brighten the image by given percentage
  float scaleFactor = 1.0f + percent;
  RescaleOp op = new RescaleOp(scaleFactor, 0, null);
  image = op.filter(image, null);
  
  return image;
}
  
  
	/**
	 * Custom (and IMHO better) implementation of brighten image.
	 * @param img image
	 * @param percentage 0 to 100.
	 */
	 public static void brightenImageCustom(BufferedImage img, double percentage) {
	   if (percentage<0||percentage>100) {
	     log.log(Level.SEVERE, "Invalid percentage given: " + percentage);
	     return;
	   }
	    Graphics g2 = img.getGraphics();
	    int x, y, clr,red,green,blue;

	    for (x = 0; x < img.getWidth(); x++) {
	      for (y = 0; y < img.getHeight(); y++) {

	        // For each pixel in the image
	        // get the red, green and blue value
	        clr = img.getRGB(x, y);

	        red = (clr & 0x00ff0000) >> 16;
	        green = (clr & 0x0000ff00) >> 8;
	        blue = clr & 0x000000ff;
	        
	        
	        g2.setColor(new Color(brightenColorPart(red, percentage, true),
	          brightenColorPart(green, percentage, true),
	          brightenColorPart(blue, percentage, true)));
	        g2.fillRect(x, y, 1, 1);
	        
	      }
	    }
	  }

	/**
	 * Convert an bufferedImage to an image.
	 * 
	 * @param bufferedImage
	 * @return Image
	 */
	public static Image bufferedImageToImage(BufferedImage bufferedImage) {
		return Toolkit.getDefaultToolkit().createImage(
				bufferedImage.getSource());
	}

	/**
	   * Crops an image.
		 * @param img - image to resize
		 * @param x - new x
		 * @param y - new y
		 * @param w - desired with
		 * @param h - desired height
		 * @return cropped image
		 */
	  public static Image cropImage(Image img, int x, int y, int w, int h) {
	    Image image = Toolkit.getDefaultToolkit().createImage(
	      new FilteredImageSource(img.getSource(),
	      new CropImageFilter(x, y, w, h)));
	    
	    return image;
	  }

	/**
	 * Darkens an image by the given percentage.
	 * @param image - image to darken
	 * @param percent a value between 0 and 1 (0 to 100 percent).
	 * @return darkened image
	 */
  public static BufferedImage darkenImage(BufferedImage image, float percent) {    
		if ((percent < 0) || (percent > 1)) {
			log.log(Level.SEVERE, MessageFormat.format(bundle.getString("WRONG_PERCENTAGE_IN_IMAGE"), "darkenImage", percent));
			return image;
		}
    
    // Darken the image by given percentage
    float scaleFactor = 1.0f - percent;
    RescaleOp op = new RescaleOp(scaleFactor, 0, null);
    image = op.filter(image, null);
    
    return image;
  }

  /**
   * @param imagePath
   * @return 
   * @throws IOException 
   */
  public static BufferedImage getImage(URL imagePath) throws IOException {
    return ImageIO.read(imagePath);
  }
	
	/**
   * 
   * @param listOfImages
   * @param obs allows <code>null</code>
   * @return
   */
	public static Image getImageOfHighestResolution(List<Image> listOfImages, ImageObserver obs) {
		Image image = null;
		int resolution = Integer.MIN_VALUE;
		for (Image i : listOfImages) {
			int res = i.getWidth(obs) * i.getHeight(obs);
			if (res>resolution) {
				image = i;
				resolution = res;
			}
		}
		return image;
	}
	
	/**
	 * Get the color of a specific pixel.
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @return java.awt.color of the given pixel.
	 */
	public static Color getPixelColor(BufferedImage image, int x, int y) {
		int c = image.getRGB(x, y);
		int red = (c & 0x00ff0000) >> 16;
		int green = (c & 0x0000ff00) >> 8;
		int blue = c & 0x000000ff;

		// and the Java Color is ...
		return new Color(red, green, blue);
	}
	 
	 
	 /**
	 * Convert an Image to an BufferedImage.
	 * 
	 * @param img
	 * @return BufferedImage
	 */
	public static BufferedImage image2BufferedImage(Image img) {
		return image2BufferedImage(img, false);
	}
	
	/**
	 * Convert an Image to an BufferedImage.
	 * 
	 * @param img
	 * @param drawBorder set to true, if you want to draw a black border
	 * @return BufferedImage
	 */
	public static BufferedImage image2BufferedImage(Image img,
			boolean drawBorder) {
		BufferedImage bmage = new BufferedImage(img.getWidth(null), 
		  img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = bmage.getGraphics();
		g.drawImage(img, 0, 0, null);

		// Border (upper-, right and left corner)
		if (drawBorder) {
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, img.getWidth(null) - 1, img.getHeight(null));
		}
		g.dispose();

		return bmage;
	}

	
	/**
	 * Centers the image within the given coordinates (does NOT scratch or other
	 * things).
	 * 
	 * @param img
	 *            - image to paing
	 * @param targetWith
	 * @param targetHeight
	 * @param autoSetBackgroundColor
	 * @param drawBorder
	 * @return BufferedImage
	 */
	public static BufferedImage image2BufferedImage(Image img, int targetWith,
			int targetHeight, boolean autoSetBackgroundColor, boolean drawBorder) {
		BufferedImage bmage = new BufferedImage(targetWith, targetHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = bmage.getGraphics();

		// Backgroundcolor as in Pixel (0,0)
		if (autoSetBackgroundColor) {
			Color bg = getPixelColor(image2BufferedImage(img, drawBorder), 1, 1);
			g.setColor(bg);
			g.fillRect(0, 0, targetWith, targetHeight);
		}

		// Draw the actual picture centered
		int x = targetWith / 2 - img.getWidth(null) / 2;
		int y = targetHeight / 2 - img.getHeight(null) / 2;
		g.drawImage(img, x, y, null);

		// Border (upper-, right and left corner)
		if (drawBorder) {
			g.setColor(new Color(0, 0, 0));
			g.drawRect(0, 0, targetWith - 1, targetHeight);
		}

		return bmage;
	}

	/**
	 * Loads a defined list of icons into the {@link UIManager}.
	 */
	public static void initImages() {
		String iconPaths[] = {
        "ICON_ARROW_LEFT_16.png",
        "ICON_ARROW_LEFT_32.png",
        "ICON_ARROW_RIGHT_16.png",
        "ICON_ARROW_RIGHT_32.png",
        "ICON_DOCUMENT_16.png",
        "ICON_DOCUMENT_32.png",
        "ICON_DOCUMENT_48.png",
        "ICON_DOCUMENT_64.png",
        "ICON_EXIT_16.png",
        "ICON_EXIT_32.png",
        "ICON_GEAR_16.png",
        "ICON_GEAR_64.png",
        "ICON_GLOBE_16.png",
        "ICON_GLOBE_64.png",
        "ICON_HELP_16.png",
        "ICON_HELP_48.png",
        "ICON_HELP_64.png",
        "ICON_INFO_16.png",
        "ICON_INFO_64.png",
        "ICON_LICENSE_16.png",
        "ICON_LICENSE_64.png",
        "ICON_LICENSE_48.png",
        "ICON_MINUS_16.png",
        "ICON_OPEN_16.png",
        "ICON_PENCIL_16.png",
        "ICON_PENCIL_32.png",
        "ICON_PENCIL_48.png",
        "ICON_PENCIL_64.png",
        "ICON_PLUS_16.png",
        "ICON_PREFS_16.png",
        "ICON_REFRESH_16.png",
        "ICON_REFRESH_32.png",
        "ICON_REFRESH_48.png",
        "ICON_REFRESH_64.png",        
        "ICON_SAVE_16.png",
        "ICON_TICK_16.png",
        "ICON_TRASH_16.png",
        "ICON_SEARCH_16.png",
        "ICON_WARNING_16.png",
        "ICON_WARNING_32.png",
        "ICON_WARNING_48.png",
        "ICON_WARNING_64.png",
        "UT_BM_Rot_RGB_tr_36x64.png",
        "UT_WBMS_Rot_RGB_tr_64x62.png",
        "UT_WBMW_mathnat_4C_380x45.png"
    };
    for (String path : iconPaths) {
      URL u = ImageTools.class.getResource("img/" + path);
      if (u!=null) {
        UIManager.put(path.substring(0, path.lastIndexOf('.')), new ImageIcon(u));
      }
    }
	}

  /**
	 * Loads all image files that can be found in the directory referenced by
	 * the given {@link URL} into the dedicated hash table in {@link UIManager}.
	 * The key to access these image files is then the file name, i.e., the
	 * substring of the file name ending at the last dot symbol, for instance,
	 * myFile.png will result in the key "myFile".
	 * 
	 * @param directory
	 *            A {@link URL} representing a directory with image files.
	 * @throws URISyntaxException
	 */
	public static void initImages(URL directory) {
	  /*
	   * You can not create File objects or list directories from places
	   * inside a jar. Using this code will make it impossible to ever release
	   * this class + images inside a jar file.
	   * TODO: Change the code to use getResourceAsStream().
	   */
		try {
		  // The following lines throws an IllegalArgumentException if called
		  // from inside a jar
			File dir = new File(directory.toURI());
			if (dir.canRead() && dir.isDirectory()) {
				String key;
				for (File file : dir.listFiles(SBFileFilter.createImageFileFilter())) {
					key = file.getName().substring(0,
							file.getName().lastIndexOf('.'));
					try {
						if (UIManager.getIcon(key) == null) {
							UIManager.put(key,
									new ImageIcon(ImageIO.read(file)));
						}
					} catch (Exception exc) {
					  log.log(Level.SEVERE, MessageFormat.format(bundle.getString("COULD_NOT_LOAD_IMAGE"), file));						
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.log(Level.SEVERE, MessageFormat.format(bundle.getString("COULD_NOT_LOAD_ICONS_FROM_DIR"), directory));
		} catch (URISyntaxException e) {
			log.log(Level.SEVERE, MessageFormat.format(bundle.getString("COULD_NOT_LOAD_ICONS_FROM_DIR"), directory));
		}
	}

  /**
	 * Replace a color with another.
	 * @param img
	 * @param source source {@link Color}
	 * @param replacement
	 */
	public static void replaceColor(BufferedImage img, Color source, Color replacement) {
	  Graphics g2 = img.getGraphics();
	  g2.setColor(replacement);
	  int x, y, clr,red,green,blue;

	  for (x = 0; x < img.getWidth(); x++) {
	    for (y = 0; y < img.getHeight(); y++) {

	      // For each pixel in the image
	      // get the red, green and blue value
	      clr = img.getRGB(x, y);

	      red = (clr & 0x00ff0000) >> 16;
	      green = (clr & 0x0000ff00) >> 8;
	      blue = clr & 0x000000ff;
	      
	      if (red==source.getRed() &&  green == source.getGreen() && blue == source.getBlue()) {
          g2.fillRect(x, y, 1, 1);
	      }
	      
	    }
	  }
	}

	/**
	 * Resize/rescale an image.
	 * 
	 * @param bimg
	 *            - image to resize
	 * @param w
	 *            - desired with
	 * @param h
	 *            - desired height
	 * @param scaleImageProportional
	 *            - if set to true, width and height will be set to the closest
	 *            of the given value, such that either with=w or height=h and
	 *            the image proportions are being preserved.
	 * @return resized BufferedImage
	 */
	public static BufferedImage resizeImage(BufferedImage bimg, int w, int h,
			boolean scaleImageProportional) { // -1 = proportional
		if (!scaleImageProportional) {
			Image img = bimg.getScaledInstance(w, h, Image.SCALE_SMOOTH); // width,
			// height,
			// hints(Fast/Smooth)
			return image2BufferedImage(img, false);
		} else {
			// resize proportional (ausgehend von max), und zentriert in
			// buffered image mit gegebenen koords setzen).
			int diffX = Math.abs(bimg.getWidth() - w);
			int diffY = Math.abs(bimg.getHeight() - h);

			int h2 = h, w2 = w;
			if (diffX > diffY)
				h2 = -1;
			else
				w2 = -1;
			Image img = bimg.getScaledInstance(w2, h2, Image.SCALE_SMOOTH); // width,
			// height,
			// hints

			// forceSize allow drawing with- or without border (=> given size)
			// if (forceSize)
			// return image2BufferedImage(img, w, h);
			// else
			return image2BufferedImage(img, false);
		}
	}

}
