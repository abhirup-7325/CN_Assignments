package edu.abhirup.client.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputBoxWindow extends JDialog {
    private JTextField inputField;
    private JButton enterButton;
    private String userInput = "";

    public InputBoxWindow(Frame parent) {
        super(parent, "Enter Message", true); // true = modal dialog
        setSize(500, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 245, 245));

        // ====== Title Label ======
        JLabel titleLabel = new JLabel("Enter Your Message");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ====== Input Field ======
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        add(inputField, BorderLayout.CENTER);

        // ====== Enter Button ======
        enterButton = new JButton("Submit");
        enterButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        enterButton.setBackground(new Color(0, 120, 215));
        enterButton.setForeground(Color.WHITE);
        enterButton.setFocusPainted(false);
        enterButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        enterButton.addMouseListener(new java.awt.event.MouseAdapter() {
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

        // ====== Button Action ======
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userInput = inputField.getText().trim();
                dispose(); // close the dialog
            }
        });

        // ====== Center Screen ======
        setLocationRelativeTo(parent);
    }

    public String getUserInput() {
        return userInput;
    }

    public static String showInputDialog(Frame parent) {
        InputBoxWindow dialog = new InputBoxWindow(parent);
        dialog.setVisible(true); // blocks until user closes
        return dialog.getUserInput();
    }
}
