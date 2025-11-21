package com.example.fhir.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Interfaz gráfica para el cliente FHIR
 * Permite gestionar recursos Patient y Observation mediante una GUI
 */
public class FhirClientGUI extends JFrame {
    
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080/fhir/";
    
    private JTextField serverUrlField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JTextArea logArea;
    
    private PatientPanel patientPanel;
    private ObservationPanel observationPanel;
    
    public FhirClientGUI() {
        setTitle("Cliente FHIR - Gestor de Recursos HL7");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        // Panel de configuración
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // URL del servidor
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("URL del Servidor:"), gbc);
        gbc.gridx = 1;
        serverUrlField = new JTextField(DEFAULT_SERVER_URL, 30);
        configPanel.add(serverUrlField, gbc);
        
        // Usuario
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField("admin", 15);
        configPanel.add(usernameField, gbc);
        
        // Contraseña
        gbc.gridx = 2;
        configPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 3;
        passwordField = new JPasswordField("admin123", 15);
        configPanel.add(passwordField, gbc);
        
        // Rol
        gbc.gridx = 4;
        configPanel.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 5;
        roleComboBox = new JComboBox<>(new String[]{"admin", "user"});
        roleComboBox.setSelectedIndex(0);
        configPanel.add(roleComboBox, gbc);
        
        // Botón conectar
        gbc.gridx = 6;
        JButton connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> connectToServer());
        configPanel.add(connectButton, gbc);
        
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
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();
        
        log("Intentando conectar a: " + serverUrl);
        log("Usuario: " + username + " | Rol: " + role);
        
        // Actualizar configuración en los paneles
        patientPanel.setServerConfig(serverUrl, username, password);
        observationPanel.setServerConfig(serverUrl, username, password);
        
        // Probar conexión
        try {
            if (patientPanel.testConnection()) {
                log("✓ Conexión exitosa al servidor FHIR");
                JOptionPane.showMessageDialog(this, 
                    "Conexión exitosa al servidor FHIR", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            log("✗ Error de conexión: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al conectar: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
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
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new FhirClientGUI().setVisible(true);
        });
    }
}

