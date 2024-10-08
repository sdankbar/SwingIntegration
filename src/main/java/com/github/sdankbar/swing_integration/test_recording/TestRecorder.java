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

import java.awt.AWTEvent;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;
import javax.swing.FocusManager;
import javax.swing.SwingUtilities;

public class TestRecorder {

	private static class ActiveWindow {
		private final String title;
		private final Point location;

		public ActiveWindow(final String title, final Point location) {
			this.title = title;
			this.location = location;
		}

	}

	private static class RecordedEvent {
		private final AWTEvent event;
		private final Instant eventTime;
		private final File screenshotFile;
		private final boolean motionEvent;

		private final ActiveWindow window;

		public RecordedEvent(final AWTEvent event, final Instant eventTime, final ActiveWindow w) {
			this.event = event;
			this.eventTime = eventTime;
			screenshotFile = null;
			motionEvent = false;
			window = w;
		}

		public RecordedEvent(final AWTEvent event, final Instant eventTime, final boolean isMotion,
				final ActiveWindow w) {
			this.event = event;
			this.eventTime = eventTime;
			screenshotFile = null;
			motionEvent = isMotion;
			window = w;
		}

		public RecordedEvent(final Instant eventTime, final File screenshotFile) {
			event = null;
			this.eventTime = eventTime;
			this.screenshotFile = screenshotFile;
			motionEvent = false;
			window = null;
		}

		public int getRelativeX(final int x) {
			if (window != null) {
				return x - window.location.x;
			} else {
				return x;
			}
		}

		public int getRelativeY(final int y) {
			if (window != null) {
				return y - window.location.y;
			} else {
				return y;
			}
		}
	}

	public static BufferedImage takeScreenshot() {
		final Window w = FocusManager.getCurrentManager().getActiveWindow();
		final FutureTask<BufferedImage> task = new FutureTask<>(() -> {
			final BufferedImage i = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = i.createGraphics();
			w.paint(g);
			g.dispose();
			return i;
		});
		if (SwingUtilities.isEventDispatchThread()) {
			task.run();
		} else {
			SwingUtilities.invokeLater(task);
		}
		try {
			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public enum RecordingMode {
		ABSOLUTE, RELATIVE
	}

	private static final int MOVE_SAMPLE_RATE = 250;

	private final List<RecordedEvent> recordedEvents = new ArrayList<>();

	private boolean isRecording = false;
	private Instant startTime = Instant.EPOCH;
	private File recordingDir = new File(".");
	private Instant lastMouseMoveTime = Instant.EPOCH;
	private final RecordingMode mode;
	private final boolean autoRaise;

	private int toggleRecordingHotKey = KeyEvent.VK_F1;
	private int screenshotHotKey = KeyEvent.VK_F2;

	public TestRecorder() {
		this(RecordingMode.RELATIVE, false);
	}

	public TestRecorder(final RecordingMode mode, final boolean autoRaise) {
		this.mode = Objects.requireNonNull(mode, "mode is null");
		this.autoRaise = autoRaise;
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			handleKeyEvent(event);
		}, AWTEvent.KEY_EVENT_MASK);

		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			handleMouseMotionEvent(event);
		}, AWTEvent.MOUSE_MOTION_EVENT_MASK);

		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			handleMouseEvent(event);
		}, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}

	private void addEvent(final RecordedEvent e) {
		if (!recordedEvents.isEmpty()) {
			final RecordedEvent last = recordedEvents.get(recordedEvents.size() - 1);
			if (last.event != e.event || e.event == null) {
				recordedEvents.add(e);
			}
		} else {
			recordedEvents.add(e);
		}
	}

	/**
	 * Change the keyboard keys used to start/stop recording and take screenshots.
	 *
	 * @param toggleRecording Key code for toggling recording. Use a KeyEvent
	 *                        constant, like KeyEvent.VK_F1.
	 * @param screenshot      Key code for toggling recording. Use a KeyEvent
	 *                        constant, like KeyEvent.VK_F2.
	 */
	public void setHotkeys(final int toggleRecording, final int screenshot) {
		toggleRecordingHotKey = toggleRecording;
		screenshotHotKey = screenshot;
	}

	private ActiveWindow getActiveWindow() {
		final Window w = FocusManager.getCurrentManager().getActiveWindow();
		if (w == null) {
			return null;
		}
		return new ActiveWindow(w.getName(), w.getLocationOnScreen());
	}

	private void handleMouseMotionEvent(final AWTEvent event) {
		final Instant now = Instant.now();
		final long milli = now.toEpochMilli();
		if ((milli - lastMouseMoveTime.toEpochMilli()) > MOVE_SAMPLE_RATE && isRecording) {
			final RecordedEvent rec = new RecordedEvent(event, now, true, getActiveWindow());
			lastMouseMoveTime = now;
			addEvent(rec);
		}
	}

	private void handleMouseEvent(final AWTEvent event) {
		if (isRecording) {
			final RecordedEvent rec = new RecordedEvent(event, Instant.now(), getActiveWindow());
			addEvent(rec);
		}
	}

	private void handleKeyEvent(final AWTEvent event) {
		final KeyEvent key = (KeyEvent) event;

		if (key.getID() == KeyEvent.KEY_PRESSED) {
			if (key.getKeyCode() == toggleRecordingHotKey) {
				// Ignore
			} else if (key.getKeyCode() == screenshotHotKey) {
				// Ignore
			} else if (isRecording) {
				final RecordedEvent rec = new RecordedEvent(event, Instant.now(), getActiveWindow());
				addEvent(rec);
			}
			lastMouseMoveTime = Instant.EPOCH;
		} else if (key.getID() == KeyEvent.KEY_RELEASED) {
			if (key.getKeyCode() == toggleRecordingHotKey) {
				if (isRecording) {
					saveTestSteps();
				} else {
					System.out.println("Start recording");
					startTime = Instant.now();
					recordedEvents.clear();

					recordingDir = new File("recording_" + startTime.toEpochMilli());
					recordingDir.mkdir();
				}
				isRecording = !isRecording;

				lastMouseMoveTime = Instant.EPOCH;
			} else if (key.getKeyCode() == screenshotHotKey && isRecording) {

				final BufferedImage windowImage = takeScreenshot();
				if (windowImage != null) {
					final String now = Long.toString(Instant.now().getEpochSecond());
					final String fileName = "screenshot_" + now + ".png";
					final File filePath = new File(recordingDir, fileName);
					try {
						ImageIO.write(windowImage, "PNG", filePath);
						final RecordedEvent rec = new RecordedEvent(Instant.now(), filePath);
						addEvent(rec);
					} catch (final IOException e) {
						e.printStackTrace();// TODO
					}
				}
				lastMouseMoveTime = Instant.EPOCH;
			} else if (isRecording) {
				final RecordedEvent rec = new RecordedEvent(event, Instant.now(), getActiveWindow());
				addEvent(rec);
			}
			lastMouseMoveTime = Instant.EPOCH;
		}
	}

	private String buttonToEnum(final int mouseButton) {
		switch (mouseButton) {
		case MouseEvent.BUTTON1:
			return "InputEvent.BUTTON1_DOWN_MASK";
		case MouseEvent.BUTTON2:
			return "InputEvent.BUTTON2_DOWN_MASK";
		case MouseEvent.BUTTON3:
			return "InputEvent.BUTTON3_DOWN_MASK";
		default:
			return "InputEvent.BUTTON1_DOWN_MASK";
		}
	}

	private String getAutoRaiseString(final RecordedEvent e) {
		if (autoRaise && mode == RecordingMode.RELATIVE && e.window != null) {
			return "\"" + e.window.title + "\", ";
		} else {
			return "";
		}
	}

	private void saveTestSteps() {
		final File output = new File(recordingDir, "TemplateTest.java");
		try (BufferedWriter w = new BufferedWriter(new FileWriter(output))) {

			w.write("import java.io.File;\n");
			w.write("import java.time.Duration;\n");
			w.write("import org.junit.After;\n");
			w.write("import org.junit.Test;\n");
			w.write("import java.awt.Robot;\n");
			w.write("import java.awt.AWTException;\n");
			w.write("import org.junit.rules.ErrorCollector;\n");
			w.write("import org.junit.Rule;\n");
			w.write("\n");
			w.write("public class IntegrationTest {\n");
			w.write("\n");
			w.write("\tprivate final String screenshotDir = \"TODO\";\n");
			w.write("\n");
			w.write("\t@Rule\n");
			w.write("\tpublic ErrorCollector collector= new ErrorCollector();\n");
			w.write("\n");
			w.write("\t@Before\n");
			w.write("\tpublic void setup() {\n");
			w.write("\t\t// TODO run test setup\n");
			w.write("\t}\n");
			w.write("\n");
			w.write("\t@After\n");
			w.write("\tpublic void finish() {\n");
			w.write("\t\t// TODO test cleanup\n");
			w.write("\t}\n");
			w.write("\n");
			w.write("\t@Test\n");
			w.write("\tpublic void test_run() throws AWTException {\n");
			w.write("\t\tTestRunner tools = new TestRunner(new File(screenshotDir), collector);\n");
			w.write("\t\ttools.waitForWindow();\n");

			Instant workingTime = startTime;
			for (final RecordedEvent e : recordedEvents) {
				if (e.event != null) {
					final long milli = Duration.between(workingTime, e.eventTime).toMillis();

					if (e.event instanceof KeyEvent) {
						w.write("\t\ttools.delay(" + milli + ");\n");

						final KeyEvent key = (KeyEvent) e.event;
						if (key.getID() == KeyEvent.KEY_PRESSED) {
							w.write("\t\ttools.keyPress(" + getAutoRaiseString(e) + key.getKeyCode() + ");// "
									+ KeyEvent.getKeyText(key.getKeyCode()) + "\n");
						} else {
							w.write("\t\ttools.keyRelease(" + getAutoRaiseString(e) + key.getKeyCode() + ");// "
									+ KeyEvent.getKeyText(key.getKeyCode()) + "\n");
						}
						workingTime = e.eventTime;
					} else if (e.event instanceof MouseWheelEvent) {
						w.write("\t\ttools.delay(" + milli + ");\n");

						final MouseWheelEvent mouse = (MouseWheelEvent) e.event;
						w.write("\t\ttools.mouseWheel(" + getAutoRaiseString(e) + mouse.getWheelRotation() + ");\n");

						workingTime = e.eventTime;
					} else if (e.event instanceof MouseEvent) {
						final MouseEvent mouse = (MouseEvent) e.event;
						if (e.motionEvent) {
							w.write("\t\ttools.delay(" + milli + ");\n");

							if (mode == RecordingMode.ABSOLUTE) {
								w.write("\t\ttools.mouseMove(" + mouse.getXOnScreen() + ", " + mouse.getYOnScreen()
										+ ");\n");
							} else {
								w.write("\t\ttools.mouseMoveRelative(" + getAutoRaiseString(e)
										+ e.getRelativeX(mouse.getXOnScreen()) + ", "
										+ e.getRelativeY(mouse.getYOnScreen()) + ");\n");
							}

							workingTime = e.eventTime;
						} else if (mouse.getID() == MouseEvent.MOUSE_PRESSED) {
							w.write("\t\ttools.delay(" + milli + ");\n");

							if (mode == RecordingMode.ABSOLUTE) {
								w.write("\t\ttools.mousePress(" + mouse.getXOnScreen() + ", " + mouse.getYOnScreen()
										+ "," + buttonToEnum(mouse.getButton()) + ");\n");
							} else {
								w.write("\t\ttools.mousePressRelative(" + getAutoRaiseString(e)
										+ e.getRelativeX(mouse.getXOnScreen()) + ", "
										+ e.getRelativeY(mouse.getYOnScreen()) + "," + buttonToEnum(mouse.getButton())
										+ ");\n");
							}

							workingTime = e.eventTime;
						} else if (mouse.getID() == MouseEvent.MOUSE_RELEASED) {
							w.write("\t\ttools.delay(" + milli + ");\n");

							if (mode == RecordingMode.ABSOLUTE) {
								w.write("\t\ttools.mouseRelease(" + mouse.getXOnScreen() + ", " + mouse.getYOnScreen()
										+ "," + buttonToEnum(mouse.getButton()) + ");\n");
							} else {
								w.write("\t\ttools.mouseReleaseRelative(" + getAutoRaiseString(e)
										+ e.getRelativeX(mouse.getXOnScreen()) + ", "
										+ e.getRelativeY(mouse.getYOnScreen()) + "," + buttonToEnum(mouse.getButton())
										+ ");\n");
							}

							workingTime = e.eventTime;
						}
					}
				} else {
					final long milli = Duration.between(workingTime, e.eventTime).toMillis();
					w.write("\t\ttools.delay(" + milli + ");\n");
					w.write("\t\ttools.compare(\"" + e.screenshotFile.getName() + "\");\n");
					workingTime = e.eventTime;
				}
			}

			w.write("\t}\n");
			w.write("\n");
			w.write("}\n");

			w.close();
			System.out.println("Done writing test");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
