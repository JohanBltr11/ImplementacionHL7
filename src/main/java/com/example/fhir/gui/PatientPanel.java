package com.example.fhir.gui;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel para gestionar recursos Patient
 */
public class PatientPanel extends JPanel {
    
    private FhirClientGUI parent;
    private IGenericClient client;
    private FhirContext fhirContext;
    private BasicAuthInterceptor authInterceptor; // Guardar referencia al interceptor
    
    private JTextField idField;
    private JTextField familyNameField;
    private JTextField givenNameField;
    private JComboBox<String> genderComboBox;
    private JTextField birthDateField;
    private JTextArea resultArea;
    
    private JButton createButton;
    private JButton readButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton searchButton;
    private JButton clearButton;
    
    private String serverUrl;
    private String username;
    private String password;
    
    public PatientPanel(FhirClientGUI parent) {
        this.parent = parent;
        this.fhirContext = FhirContext.forR4();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Panel de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        idField = new JTextField(20);
        inputPanel.add(idField, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        familyNameField = new JTextField(20);
        inputPanel.add(familyNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        givenNameField = new JTextField(20);
        inputPanel.add(givenNameField, gbc);
        
        // Género
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Género:"), gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"male", "female", "other", "unknown"});
        inputPanel.add(genderComboBox, gbc);
        
        // Fecha de nacimiento
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Fecha Nacimiento (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        birthDateField = new JTextField(20);
        inputPanel.add(birthDateField, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        createButton = new JButton("Crear");
        createButton.addActionListener(e -> createPatient());
        buttonPanel.add(createButton);
        
        readButton = new JButton("Leer");
        readButton.addActionListener(e -> readPatient());
        buttonPanel.add(readButton);
        
        updateButton = new JButton("Actualizar");
        updateButton.addActionListener(e -> updatePatient());
        buttonPanel.add(updateButton);
        
        deleteButton = new JButton("Eliminar");
        deleteButton.addActionListener(e -> deletePatient());
        buttonPanel.add(deleteButton);
        
        searchButton = new JButton("Buscar Todos");
        searchButton.addActionListener(e -> searchPatients());
        buttonPanel.add(searchButton);
        
        clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);
        
        // Área de resultados
        resultArea = new JTextArea(15, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Resultados"));
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(resultScrollPane, BorderLayout.CENTER);
    }
    
    public void setServerConfig(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        
        // Crear cliente nuevo (esto limpia interceptores anteriores)
        client = fhirContext.newRestfulGenericClient(serverUrl);
        
        // Limpiar interceptores anteriores si existen
        client.getInterceptorService().unregisterAllInterceptors();
        
        // Agregar autenticación (debe ser el primero)
        // Guardar referencia para verificar después
        this.authInterceptor = new BasicAuthInterceptor(username, password);
        client.registerInterceptor(this.authInterceptor);
        
        System.out.println("PatientPanel: Cliente configurado con usuario: " + username);
        System.out.println("PatientPanel: Interceptores registrados: " + 
            client.getInterceptorService().getAllRegisteredInterceptors().size());
        
        // Agregar logging del cliente (diferente del del servidor)
        ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor clientLoggingInterceptor = 
            new ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor();
        clientLoggingInterceptor.setLogRequestSummary(true);
        clientLoggingInterceptor.setLogRequestBody(true);
        client.registerInterceptor(clientLoggingInterceptor);
    }
    
    public boolean testConnection() {
        if (client == null) {
            setServerConfig(serverUrl != null ? serverUrl : "http://localhost:8080/fhir/", 
                          username != null ? username : "admin", 
                          password != null ? password : "admin123");
        }
        
        try {
            // Intentar leer el metadata (esto también requiere autenticación)
            client.capabilities();
            parent.log("Conexión exitosa - credenciales verificadas");
            return true;
        } catch (Exception e) {
            parent.log("Error de conexión: " + e.getMessage());
            // Si falla por autenticación, reconfigurar el cliente
            if (e.getMessage() != null && (e.getMessage().contains("401") || e.getMessage().contains("403") || e.getMessage().contains("autenticación"))) {
                parent.log("Error de autenticación - reconfigurando cliente...");
                setServerConfig(serverUrl, username, password);
            }
            return false;
        }
    }
    
    private void createPatient() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Asegurar que el cliente tenga las credenciales configuradas
        if (username != null && password != null) {
            // Reconfigurar para asegurar que las credenciales estén presentes
            setServerConfig(serverUrl, username, password);
        }
        
        // Verificar que el cliente tenga los interceptores antes de crear
        int interceptorCount = client.getInterceptorService().getAllRegisteredInterceptors().size();
        parent.log("PatientPanel: Interceptores registrados en cliente: " + interceptorCount);
        if (interceptorCount == 0) {
            parent.log("ERROR: No hay interceptores registrados! Reconfigurando...");
            setServerConfig(serverUrl, username, password);
        }
        
        try {
            Patient patient = new Patient();
            
            // Nombre
            patient.addName()
                .setFamily(familyNameField.getText())
                .addGiven(givenNameField.getText());
            
            // Género
            String gender = (String) genderComboBox.getSelectedItem();
            patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.fromCode(gender));
            
            // Fecha de nacimiento
            if (!birthDateField.getText().isEmpty()) {
                patient.setBirthDateElement(new org.hl7.fhir.r4.model.DateType(birthDateField.getText()));
            }
            
            // Establecer ID si el usuario lo ingresó
            String userProvidedId = idField.getText().trim();
            if (!userProvidedId.isEmpty()) {
                patient.setId("Patient/" + userProvidedId);
                parent.log("PatientPanel: Usando ID proporcionado por usuario: " + userProvidedId);
            }
            
            parent.log("PatientPanel: Enviando petición CREATE con usuario: " + username);
            
            // Crear en servidor
            Patient created = (Patient) client.create().resource(patient).execute().getResource();
            
            resultArea.setText("✓ Patient creado exitosamente:\n\n");
            resultArea.append(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(created));
            
            idField.setText(created.getIdElement().getIdPart());
            parent.log("Patient creado: " + created.getIdElement().getIdPart());
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al crear Patient:\n" + e.getMessage());
            parent.log("Error al crear Patient: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void readPatient() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String id = idField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Patient patient = client.read()
                .resource(Patient.class)
                .withId(id)
                .execute();
            
            resultArea.setText("✓ Patient encontrado:\n\n");
            resultArea.append(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
            
            // Llenar campos
            if (patient.hasName() && !patient.getName().isEmpty()) {
                familyNameField.setText(patient.getNameFirstRep().getFamily());
                if (!patient.getNameFirstRep().getGiven().isEmpty()) {
                    givenNameField.setText(patient.getNameFirstRep().getGiven().get(0).getValue());
                }
            }
            if (patient.hasGender()) {
                genderComboBox.setSelectedItem(patient.getGender().toCode());
            }
            if (patient.hasBirthDate()) {
                birthDateField.setText(patient.getBirthDateElement().getValueAsString());
            }
            
            parent.log("Patient leído: " + id);
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al leer Patient:\n" + e.getMessage());
            parent.log("Error al leer Patient: " + e.getMessage());
        }
    }
    
    private void updatePatient() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String id = idField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Patient patient = new Patient();
            patient.setId(id);
            
            // Nombre
            patient.addName()
                .setFamily(familyNameField.getText())
                .addGiven(givenNameField.getText());
            
            // Género
            String gender = (String) genderComboBox.getSelectedItem();
            patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.fromCode(gender));
            
            // Fecha de nacimiento
            if (!birthDateField.getText().isEmpty()) {
                patient.setBirthDateElement(new org.hl7.fhir.r4.model.DateType(birthDateField.getText()));
            }
            
            // Actualizar
            Patient updated = (Patient) client.update().resource(patient).execute().getResource();
            
            resultArea.setText("✓ Patient actualizado exitosamente:\n\n");
            resultArea.append(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(updated));
            
            parent.log("Patient actualizado: " + id);
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al actualizar Patient:\n" + e.getMessage());
            parent.log("Error al actualizar Patient: " + e.getMessage());
        }
    }
    
    private void deletePatient() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String id = idField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar el Patient con ID: " + id + "?", 
            "Confirmar eliminación", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            client.delete()
                .resourceById("Patient", id)
                .execute();
            
            resultArea.setText("✓ Patient eliminado exitosamente: " + id);
            clearFields();
            parent.log("Patient eliminado: " + id);
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al eliminar Patient:\n" + e.getMessage());
            parent.log("Error al eliminar Patient: " + e.getMessage());
        }
    }
    
    private void searchPatients() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Asegurar que el cliente tenga las credenciales configuradas
        if (username != null && password != null) {
            // Reconfigurar para asegurar que las credenciales estén presentes
            setServerConfig(serverUrl, username, password);
        }
        
        try {
            org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) client.search()
                .forResource(Patient.class)
                .execute();
            
            int total = bundle.getEntry().size();
            resultArea.setText("✓ Patients encontrados: " + total + "\n\n");
            
            for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Patient) {
                    Patient patient = (Patient) entry.getResource();
                    resultArea.append("ID: " + patient.getIdElement().getIdPart() + "\n");
                    if (patient.hasName()) {
                        resultArea.append("Nombre: " + patient.getNameFirstRep().getNameAsSingleString() + "\n");
                    }
                    resultArea.append("---\n");
                }
            }
            
            parent.log("Búsqueda de Patients realizada");
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al buscar Patients:\n" + e.getMessage());
            parent.log("Error al buscar Patients: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        idField.setText("");
        familyNameField.setText("");
        givenNameField.setText("");
        genderComboBox.setSelectedIndex(0);
        birthDateField.setText("");
        resultArea.setText("");
    }
    
    public void disableWriteOperations() {
        if (createButton != null) createButton.setEnabled(false);
        if (updateButton != null) updateButton.setEnabled(false);
        if (deleteButton != null) deleteButton.setEnabled(false);
    }
    
    public void enableWriteOperations() {
        if (createButton != null) createButton.setEnabled(true);
        if (updateButton != null) updateButton.setEnabled(true);
        if (deleteButton != null) deleteButton.setEnabled(true);
    }
}

