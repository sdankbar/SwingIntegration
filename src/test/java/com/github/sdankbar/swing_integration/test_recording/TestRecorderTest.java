package com.github.sdankbar.swing_integration.test_recording;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.junit.Test;

public class TestRecorderTest {
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

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
