package edu.abhirup.client.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

public class InputBoxWindow extends JDialog {
    private JTextField inputField;
    private JButton enterButton;
    private String userInput = "";

    public InputBoxWindow(Frame parent) {
        super(parent, "Enter Message", true);
        setSize(500, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("Enter Your Message");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        add(inputField, BorderLayout.CENTER);

        enterButton = new JButton("Submit");
        enterButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        enterButton.setBackground(new Color(0, 120, 215));
        enterButton.setForeground(Color.WHITE);
        enterButton.setFocusPainted(false);
        enterButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        enterButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                enterButton.setBackground(new Color(0, 100, 190));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                enterButton.setBackground(new Color(0, 120, 215));
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(enterButton);
        add(buttonPanel, BorderLayout.SOUTH);

        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userInput = inputField.getText().trim();
                dispose();
            }
        });

        setLocationRelativeTo(parent);
    }

    public String getUserInput() {
        return userInput;
    }

    public static String showInputDialog(Frame parent) {
        InputBoxWindow dialog = new InputBoxWindow(parent);
        dialog.setVisible(true);
        return dialog.getUserInput();
    }
}
