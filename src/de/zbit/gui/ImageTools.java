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
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import de.zbit.io.SBFileFilter;
import de.zbit.util.ResourceManager;

/**
 * This class contains tools for java.awt.image, java.awt.image.BufferedImage,
 * etc.
 * 
 * @author wrzodek
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class ImageTools {
  /**
   * This bundle is initialized in the static constructor
   */
	private static ResourceBundle bundle = null;
	
	static {
	  // Allow using these tools, without localized warnings!
	  try {
	    bundle = ResourceManager.getBundle("de.zbit.locales.Warnings");
	  } catch (Throwable t) {
	    System.err.println("WARNING: Could not load bundle with warning locales in ImageTools.");
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
						// TODO: Logging
						System.err.printf(bundle.getString("COULD_NOT_LOAD_IMAGE"), file);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO: Logging
			System.err.println(String.format(bundle
					.getString("COULD_NOT_LOAD_ICONS_FROM_DIR"), directory));
		} catch (URISyntaxException e) {
			// TODO: Logging
			System.err.println(String.format(bundle
					.getString("COULD_NOT_LOAD_ICONS_FROM_DIR"), directory));
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
	 * @param drawBorder
	 *            - set to true, if you want to draw a black border
	 * @return BufferedImage
	 */
	public static BufferedImage image2BufferedImage(Image img,
			boolean drawBorder) {
		BufferedImage bmage = new BufferedImage(img.getWidth(null), 
		  img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bmage.getGraphics();
		g.drawImage(img, 0, 0, null);

		// Border (upper-, right and left corner)
		if (drawBorder) {
			g.setColor(new Color(0, 0, 0));
			g.drawRect(0, 0, img.getWidth(null) - 1, img.getHeight(null));
		}

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
				BufferedImage.TYPE_INT_ARGB);
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
   * Brightens an image by the given percentage.
   * @param image - image to brighten
   * @param percent a value between 0 and 1 (0 to 100 percent).
   * @return brightened image
   */
	public static BufferedImage brightenImage(BufferedImage image, float percent) {
		if ((percent < 0) || (percent > 1)) {
    	// TODO: Logging
      System.err.printf(bundle.getString("WRONG_PERCENTAGE_IN_IMAGE"), "brightenImage", percent);
      return image;
    }
  	// Brighten the image by given percentage
	  float scaleFactor = 1.0f + percent;
	  RescaleOp op = new RescaleOp(scaleFactor, 0, null);
	  BufferedImage bufferedImage = op.filter(image, null);
	  
	  return bufferedImage;
	}
	
	/**
	 * Darkens an image by the given percentage.
	 * @param image - image to darken
	 * @param percent a value between 0 and 1 (0 to 100 percent).
	 * @return darkened image
	 */
  public static BufferedImage darkenImage(BufferedImage image, float percent) {
		if ((percent < 0) || (percent > 1)) {
			// TODO: Logging
			System.err.printf(bundle.getString("WRONG_PERCENTAGE_IN_IMAGE"),
				"darkenImage", percent);
			return image;
		}
    
    // Darken the image by given percentage
    float scaleFactor = 1.0f - percent;
    RescaleOp op = new RescaleOp(scaleFactor, 0, null);
    BufferedImage bufferedImage = op.filter(image, null);
    
    return bufferedImage;
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

}
