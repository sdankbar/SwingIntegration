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
		jFrame.setName("MainWindow");
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

		final TestRunner tools = new TestRunner(new File(screenshotDir));
		tools.waitForWindow();
		tools.delay(561);
		tools.compare("screenshot_1711205090.png");
		tools.mouseMove(186, 249);
		tools.delay(57);
		tools.delay(47);
		tools.mousePress(129, 46, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(385);
		tools.mouseRelease(129, 46, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(92);
		tools.delay(0);
		tools.delay(482);
		tools.compare("screenshot_1711205091.png");
		tools.mouseMove(130, 46);
		tools.delay(226);
		tools.delay(74);
		tools.delay(8);
		tools.mousePress(258, 73, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(259);
		tools.mouseRelease(258, 73, InputEvent.BUTTON1_DOWN_MASK);
		tools.delay(106);
		tools.delay(0);
		tools.keyPress(65);
		tools.delay(494);
		tools.keyRelease(65);
		tools.delay(96);
		tools.keyPress(66);
		tools.delay(143);
		tools.keyRelease(66);
		tools.delay(75);
		tools.keyPress(67);
		tools.delay(135);
		tools.keyRelease(67);
		tools.delay(84);
		tools.delay(550);
		tools.compare("screenshot_1711205093.png");
	}
}
