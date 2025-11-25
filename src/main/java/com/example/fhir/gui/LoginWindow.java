package com.example.fhir.gui;

import com.example.fhir.model.User;
import com.example.fhir.storage.UserStorage;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de Login y Registro de usuarios
 */
public class LoginWindow {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField patientIdField;
    private JTabbedPane tabbedPane;
    private User authenticatedUser;
    private String authenticatedPassword;
    private boolean loginSuccessful = false;
    private JDialog dialog;
    
    private UserStorage userStorage;
    
    public LoginWindow() {
        this.userStorage = UserStorage.getInstance();
        initComponents();
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Panel de Login
        JPanel loginPanel = createLoginPanel();
        tabbedPane.addTab("Iniciar Sesión", loginPanel);
        
        // Panel de Registro
        JPanel signupPanel = createSignupPanel();
        tabbedPane.addTab("Registrarse", signupPanel);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Iniciar Sesión");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        panel.add(titleLabel, gbc);
        
        // Usuario
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);
        
        // Contraseña
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        // Botón Login
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.setPreferredSize(new Dimension(200, 30));
        loginButton.addActionListener(e -> performLogin());
        panel.add(loginButton, gbc);
        
        // Mensaje de error/éxito
        gbc.gridy = 4;
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        panel.add(messageLabel, gbc);
        
        // Enter key para login
        passwordField.addActionListener(e -> performLogin());
        
        return panel;
    }
    
    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Registrarse");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        panel.add(titleLabel, gbc);
        
        // Usuario
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField signupUsernameField = new JTextField(20);
        panel.add(signupUsernameField, gbc);
        
        // Contraseña
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPasswordField signupPasswordField = new JPasswordField(20);
        panel.add(signupPasswordField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Patient ID (opcional)
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Patient ID (opcional):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        patientIdField = new JTextField(20);
        panel.add(patientIdField, gbc);
        
        // Botón Registro
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        JButton signupButton = new JButton("Registrarse");
        signupButton.setPreferredSize(new Dimension(200, 30));
        signupButton.addActionListener(e -> performSignup(signupUsernameField, signupPasswordField));
        panel.add(signupButton, gbc);
        
        // Mensaje de error/éxito
        gbc.gridy = 6;
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        panel.add(messageLabel, gbc);
        
        return panel;
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Por favor complete todos los campos", Color.RED);
            return;
        }
        
        User user = userStorage.authenticate(username, password);
        if (user != null) {
            this.authenticatedUser = user;
            this.authenticatedPassword = password;
            this.loginSuccessful = true;
            if (dialog != null) {
                dialog.dispose();
            }
        } else {
            showMessage("Usuario o contraseña incorrectos", Color.RED);
            passwordField.setText("");
        }
    }
    
    private void performSignup(JTextField usernameField, JPasswordField passwordField) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String patientId = patientIdField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showMessage("Por favor complete los campos obligatorios", Color.RED);
            return;
        }
        
        if (userStorage.userExists(username)) {
            showMessage("El usuario ya existe", Color.RED);
            return;
        }
        
        if (userStorage.registerUser(username, password, email, patientId.isEmpty() ? null : patientId)) {
            showMessage("Usuario registrado exitosamente. Puede iniciar sesión ahora.", Color.GREEN);
            // Limpiar campos
            usernameField.setText("");
            passwordField.setText("");
            emailField.setText("");
            patientIdField.setText("");
        } else {
            showMessage("Error al registrar usuario", Color.RED);
        }
    }
    
    private void showMessage(String message, Color color) {
        Component selectedComponent = tabbedPane.getSelectedComponent();
        if (selectedComponent instanceof Container) {
            Container container = (Container) selectedComponent;
            Component[] components = container.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if (label.getText().equals(" ") || label.getForeground().equals(Color.RED) || label.getForeground().equals(Color.GREEN)) {
                        label.setText(message);
                        label.setForeground(color);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Mostrar la ventana de login de forma modal
     * @return Usuario autenticado o null si se canceló
     */
    public User showAndWait() {
        // Usar JDialog modal en lugar de JFrame para bloquear correctamente
        dialog = new JDialog((Frame) null, "Sistema FHIR - Inicio de Sesión", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        // Agregar el contenido al diálogo
        dialog.add(tabbedPane, BorderLayout.CENTER);
        
        // Resetear estado de login
        loginSuccessful = false;
        authenticatedUser = null;
        authenticatedPassword = null;
        
        // Mostrar el diálogo de forma modal (bloquea hasta que se cierre)
        dialog.setVisible(true);
        
        // El diálogo bloqueará hasta que se cierre
        // Cuando se cierre, verificamos si el login fue exitoso
        return loginSuccessful ? authenticatedUser : null;
    }
    
    /**
     * Obtener la contraseña del usuario autenticado
     * @return Contraseña del usuario autenticado
     */
    public String getAuthenticatedPassword() {
        return authenticatedPassword;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            LoginWindow loginWindow = new LoginWindow();
            User user = loginWindow.showAndWait();
            if (user != null) {
                System.out.println("Usuario autenticado: " + user.getUsername() + " - Rol: " + user.getRole());
            }
        });
    }
}

