package com.github.sdankbar.swing_integration.test_recording;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.junit.Test;

public class TestRecorderTest {

	private static final String screenshotDir = "src/test/resources/TestRecorderTest";

	private static JFrame createAndShowGUI() {
		final JFrame jFrame = new JFrame("Hello World Swing Example");
		jFrame.setLayout(new FlowLayout());
		jFrame.setSize(500, 360);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JButton button = new JButton("Hello World Swing");
		final Border border = BorderFactory.createLineBorder(Color.BLACK);
		button.setBorder(border);
		button.setPreferredSize(new Dimension(150, 100));
		button.setText("Hello World Swing");
		button.setHorizontalAlignment(JLabel.CENTER);
		button.setVerticalAlignment(JLabel.CENTER);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				button.setText("Hello World Clicked");
			}
		});

		jFrame.add(button);

		final JTextField field = new JTextField(20);
		field.setPreferredSize(new Dimension(200, 100));
		button.setHorizontalAlignment(JLabel.CENTER);
		button.setVerticalAlignment(JLabel.CENTER);
		jFrame.add(field);

		jFrame.setVisible(true);

		return jFrame;
	}

	@Test
	public void test_run() throws AWTException {
		final TestRecorder t = new TestRecorder();
		final JFrame f = createAndShowGUI();

		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final TestRunner tools = new TestRunner(new File(screenshotDir));
		tools.compare("screenshot_1711204025.png");
		tools.mouseMove(274, 261);
		tools.delay(214);
		tools.delay(107);
		tools.mousePress(81, 81, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(409);
		tools.mouseRelease(81, 81, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(92);
		tools.delay(1);
		tools.compare("screenshot_1711204028.png");
		tools.mouseMove(87, 79);
		tools.delay(221);
		tools.delay(40);
		tools.delay(0);
		tools.mousePress(142, 63, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(290);
		tools.mouseRelease(142, 63, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(106);
		tools.delay(0);
		tools.keyPress(65);
		tools.delay(562);
		tools.keyRelease(65);
		tools.delay(78);
		tools.keyPress(66);
		tools.delay(191);
		tools.keyRelease(66);
		tools.delay(81);
		tools.keyPress(67);
		tools.delay(239);
		tools.keyRelease(67);
		tools.delay(64);
		tools.compare("screenshot_1711204030.png");
	}
}
