package com.example.fhir.gui;

import com.example.fhir.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Interfaz gráfica para el cliente FHIR
 * Permite gestionar recursos Patient y Observation mediante una GUI
 */
public class FhirClientGUI extends JFrame {
    
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080/fhir/";
    
    private JTextField serverUrlField;
    private JLabel userInfoLabel;
    private JButton logoutButton;
    private JTextArea logArea;
    
    private PatientPanel patientPanel;
    private ObservationPanel observationPanel;
    
    private User currentUser;
    private String currentPassword;
    
    public FhirClientGUI(User user, String password) {
        this.currentUser = user;
        this.currentPassword = password;
        
        setTitle("Cliente FHIR - Gestor de Recursos HL7 - Usuario: " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        connectToServer();
    }
    
    private void initComponents() {
        // Panel de configuración
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Información del usuario
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        String userInfo = currentUser.getUsername() + " (" + currentUser.getRole() + ")";
        if (currentUser.getPatientId() != null) {
            userInfo += " - Patient ID: " + currentUser.getPatientId();
        }
        userInfoLabel = new JLabel(userInfo);
        userInfoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        configPanel.add(userInfoLabel, gbc);
        
        // URL del servidor
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("URL del Servidor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        serverUrlField = new JTextField(DEFAULT_SERVER_URL, 30);
        configPanel.add(serverUrlField, gbc);
        
        // Botón reconectar
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton reconnectButton = new JButton("Reconectar");
        reconnectButton.addActionListener(e -> connectToServer());
        configPanel.add(reconnectButton, gbc);
        
        // Botón cerrar sesión
        gbc.gridx = 3;
        logoutButton = new JButton("Cerrar Sesión");
        logoutButton.addActionListener(e -> logout());
        configPanel.add(logoutButton, gbc);
        
        // Panel de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Panel Patient
        patientPanel = new PatientPanel(this);
        tabbedPane.addTab("Patient", patientPanel);
        
        // Panel Observation
        observationPanel = new ObservationPanel(this);
        tabbedPane.addTab("Observation", observationPanel);
        
        // Área de log
        logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log de Operaciones"));
        
        // Layout principal
        setLayout(new BorderLayout());
        add(configPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
    }
    
    private void layoutComponents() {
        // Ya está todo en initComponents
    }
    
    private void connectToServer() {
        String serverUrl = serverUrlField.getText();
        
        log("Intentando conectar a: " + serverUrl);
        log("Usuario: " + currentUser.getUsername() + " | Rol: " + currentUser.getRole());
        
        // Actualizar configuración en los paneles
        patientPanel.setServerConfig(serverUrl, currentUser.getUsername(), currentPassword);
        observationPanel.setServerConfig(serverUrl, currentUser.getUsername(), currentPassword);
        
        // Probar conexión en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                if (patientPanel.testConnection()) {
                    SwingUtilities.invokeLater(() -> {
                        log("✓ Conexión exitosa al servidor FHIR");
                        updateUIForUserRole();
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("✗ Error de conexión: " + e.getMessage());
                    JOptionPane.showMessageDialog(FhirClientGUI.this, 
                        "Error al conectar: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void updateUIForUserRole() {
        // Deshabilitar botones de creación/actualización/eliminación para usuarios regulares
        if (!currentUser.isAdmin()) {
            patientPanel.disableWriteOperations();
            observationPanel.disableWriteOperations();
            log("Modo de solo lectura activado para usuario regular");
        } else {
            patientPanel.enableWriteOperations();
            observationPanel.enableWriteOperations();
            log("Modo completo activado para administrador");
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desea cerrar sesión?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Mostrar ventana de login nuevamente
            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow = new LoginWindow();
                User user = loginWindow.showAndWait();
                if (user != null) {
                    String password = loginWindow.getAuthenticatedPassword();
                    if (password != null && !password.isEmpty()) {
                        new FhirClientGUI(user, password).setVisible(true);
                    }
                }
            });
        }
    }
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        // Configurar Look and Feel en el hilo principal antes de crear componentes
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al configurar Look and Feel: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Ejecutar en EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Mostrar ventana de login primero
                LoginWindow loginWindow = new LoginWindow();
                User user = loginWindow.showAndWait();
                
                if (user != null) {
                    String password = loginWindow.getAuthenticatedPassword();
                    if (password != null && !password.isEmpty()) {
                        FhirClientGUI gui = new FhirClientGUI(user, password);
                        gui.setVisible(true);
                    } else {
                        System.err.println("Error: No se pudo obtener la contraseña del usuario");
                        System.exit(1);
                    }
                } else {
                    System.out.println("Login cancelado por el usuario");
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("Error al iniciar la aplicación: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar la aplicación: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

