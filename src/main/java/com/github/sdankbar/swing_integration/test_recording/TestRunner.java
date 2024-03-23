/**
 * The MIT License
 * Copyright © 2024 Stephen Dankbar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.sdankbar.swing_integration.test_recording;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.FocusManager;

import org.junit.rules.ErrorCollector;

public class TestRunner {

	private final File imagePath;
	private final Robot robot;
	private final ErrorCollector collector;

	public TestRunner(final File imagePath, final ErrorCollector collector) {
		this.imagePath = Objects.requireNonNull(imagePath, "imagePath is null");
		this.collector = Objects.requireNonNull(collector, "collector is null");
		try {
			robot = new Robot();
		} catch (final AWTException e) {
			throw new RuntimeException(e);
		}
	}

	public void waitForWindow() {
		Window w;
		while ((w = FocusManager.getCurrentManager().getActiveWindow()) == null) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {

			}
		}
	}

	private BufferedImage generateDelta(final BufferedImage source, final BufferedImage target) {
		if (source.getWidth() != target.getWidth()) {
			return null;
		} else if (source.getHeight() != target.getHeight()) {
			return null;
		} else {
			final boolean whiteEquals = ("1".equals(System.getenv("WHITE_EQUALS")));
			final BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(),
					BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < source.getWidth(); ++x) {
				for (int y = 0; y < source.getHeight(); ++y) {
					final int sColor = source.getRGB(x, y);
					final int tColor = target.getRGB(x, y);
					final int deltaR = Math.abs(red(sColor) - red(tColor));
					final int deltaG = Math.abs(green(sColor) - green(tColor));
					final int deltaB = Math.abs(blue(sColor) - blue(tColor));
					if (whiteEquals && deltaR == 0 && deltaG == 0 && deltaB == 0) {
						output.setRGB(x, y, new Color(255, 255, 255).getRGB());
					} else {
						output.setRGB(x, y, new Color(deltaR, deltaG, deltaB).getRGB());
					}
				}
			}

			return output;
		}
	}

	public void compare(final String fileName) {
		compare(fileName, 75);
	}

	public void compare(final String fileName, final int minimumScore) {
		try {
			final File fullPath = new File(imagePath, fileName);
			final BufferedImage target = ImageIO.read(fullPath);

			BufferedImage source = null;
			for (int i = 0; i < 10; ++i) {
				source = TestRecorder.takeScreenshot();

				if (fuzzyEquals(source, target, 75)) {
					return;
				}
				Thread.sleep(100);
			}

			if ("1".equals(System.getenv("RECAPTURE_CONDITIONALLY"))
					&& !"".equals(System.getenv("RECAPTURE_LOWER_BOUND"))) {
				final double ratio = getPeakSignalToNoiseRatio(source, target);
				final double lowerBound = Double.parseDouble(System.getenv("RECAPTURE_LOWER_BOUND"));
				if (ratio >= lowerBound) {
					final BufferedImage delta = generateDelta(source, target);
					if (delta != null) {
						final String diffFile = fileName.replace(".png", ".delta.png");
						ImageIO.write(delta, "PNG", new File(imagePath, diffFile));
					}
					ImageIO.write(source, "PNG", new File(imagePath, fileName));
				} else {
					final BufferedImage delta = generateDelta(source, target);
					if (delta != null) {
						final String diffFile = fileName.replace(".png", ".delta.png");
						ImageIO.write(delta, "PNG", new File(imagePath, diffFile));
						collector.addError(new RuntimeException("Image does not match " + fileName
								+ " and not eligible for recapture. See " + diffFile));
					} else {
						collector.addError(new RuntimeException(
								"Image does not match " + fileName + " and not eligible for recapture."));
					}
				}
			} else if ("1".equals(System.getenv("RECAPTURE"))) {
				final BufferedImage delta = generateDelta(source, target);
				if (delta != null) {
					final String diffFile = fileName.replace(".png", ".delta.png");
					ImageIO.write(delta, "PNG", new File(imagePath, diffFile));
				}
				ImageIO.write(source, "PNG", new File(imagePath, fileName));
			} else {
				final BufferedImage delta = generateDelta(source, target);
				if (delta != null) {
					final String diffFile = fileName.replace(".png", ".delta.png");
					ImageIO.write(delta, "PNG", new File(imagePath, diffFile));
					collector.addError(new RuntimeException("Image does not match " + fileName + ". See " + diffFile));
				} else {
					collector.addError(new RuntimeException("Image does not match " + fileName + "."));
				}
			}

		} catch (final IOException e) {
			throw new RuntimeException(e);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int red(final int c) {
		return (c >> 16) & 0xFF;
	}

	private int green(final int c) {
		return (c >> 8) & 0xFF;
	}

	private int blue(final int c) {
		return (c >> 0) & 0xFF;
	}

	private double getPeakSignalToNoiseRatio(final BufferedImage source, final BufferedImage target) {
		if (source == null || target == null) {
			return 0;
		} else if (source.getWidth() != target.getWidth()) {
			return 0;
		} else if (source.getHeight() != target.getHeight()) {
			return 0;
		} else {
			long sqSum = 0;
			final int pixelCount = source.getWidth() * source.getHeight();
			for (int x = 0; x < source.getWidth(); ++x) {
				for (int y = 0; y < source.getHeight(); ++y) {
					final int sColor = source.getRGB(x, y);
					final int tColor = target.getRGB(x, y);
					final int deltaR = red(sColor) - red(tColor);
					final int deltaG = green(sColor) - green(tColor);
					final int deltaB = blue(sColor) - blue(tColor);
					sqSum += (deltaR * deltaR) + (deltaG * deltaG) + (deltaB * deltaB);
				}
			}

			final double meanSquareError = sqSum / (3.0 * pixelCount);
			if (meanSquareError == 0) {
				// Avoid division by 0.
				return 1000;
			} else {
				final double peakSignalToNoiseRatio = 10 * Math.log10((255 * 255) / meanSquareError);
				return Math.min(peakSignalToNoiseRatio, 1000.0);
			}
		}
	}

	private boolean fuzzyEquals(final BufferedImage source, final BufferedImage target, final double ratiodB) {
		final double peakSignalToNoiseRatio = getPeakSignalToNoiseRatio(source, target);
		return (peakSignalToNoiseRatio > ratiodB);
	}

	public void keyPress(final int keyCode) {
		robot.keyPress(keyCode);
	}

	public void keyRelease(final int keyCode) {
		robot.keyRelease(keyCode);
	}

	public void mouseWheel(final int x, final int y, final int wheelClickCount) {
		robot.mouseMove(x, y);
		robot.mouseWheel(wheelClickCount);
	}

	public void mouseMove(final int x, final int y) {
		robot.mouseMove(x, y);
	}

	public void mousePress(final int x, final int y, final int buttons) {
		robot.mouseMove(x, y);
		robot.mousePress(buttons);
	}

	public void mouseRelease(final int x, final int y, final int buttons) {
		robot.mouseMove(x, y);
		robot.mouseRelease(buttons);
	}

	public void delay(final int milli) {
		try {
			Thread.sleep(milli);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
