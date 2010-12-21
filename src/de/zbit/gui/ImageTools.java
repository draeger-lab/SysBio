/**
 *
 * @author wrzodek
 */
package de.zbit.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import de.zbit.io.SBFileFilter;

/**
 * This class contains tools for java.awt.image, java.awt.image.BufferedImage,
 * etc.
 * 
 * @author wrzodek
 * @author Andreas Dr&auml;ger
 */
public class ImageTools {

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
		try {
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
						System.err.printf("Could not load image %s.\n", file);
					}
				}
			}
		} catch (URISyntaxException e) {
			System.err.println(String.format(
					"Could not load icons from directory %s.", directory));
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
		BufferedImage bmage = new BufferedImage(img.getWidth(null), img
				.getHeight(null), BufferedImage.TYPE_INT_ARGB);
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
	 * Blur ("unsharpen") the given image.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public BufferedImage blur(BufferedImage bufferedImage) {
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
