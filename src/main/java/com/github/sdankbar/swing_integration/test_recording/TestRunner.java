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
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.FocusManager;

import org.junit.Assert;

public class TestRunner {

	private final File imagePath;
	private final Robot robot;

	public TestRunner(final File imagePath) {
		this.imagePath = Objects.requireNonNull(imagePath, "imagePath is null");
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

	public void compare(final String fileName) {
		try {
			final BufferedImage target = ImageIO.read(new File(imagePath, fileName));
			final BufferedImage source = TestRecorder.takeScreenshot();

			if (!fuzzyEquals(source, target, 75)) {
				Assert.fail("Images not equal");
				// TODO generate delta and other options
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
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
