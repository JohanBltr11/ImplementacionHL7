package com.example.fhir.gui;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel para gestionar recursos Observation
 */
public class ObservationPanel extends JPanel {
    
    private FhirClientGUI parent;
    private IGenericClient client;
    private FhirContext fhirContext;
    
    private JTextField idField;
    private JTextField patientIdField;
    private JTextField codeField;
    private JTextField valueField;
    private JComboBox<String> valueTypeComboBox;
    private JTextField unitField;
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
    
    public ObservationPanel(FhirClientGUI parent) {
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
        
        // Patient ID
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1;
        patientIdField = new JTextField(20);
        inputPanel.add(patientIdField, gbc);
        
        // Código
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Código (ej: body-weight):"), gbc);
        gbc.gridx = 1;
        codeField = new JTextField(20);
        codeField.setText("body-weight");
        inputPanel.add(codeField, gbc);
        
        // Valor
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Valor:"), gbc);
        gbc.gridx = 1;
        valueField = new JTextField(20);
        inputPanel.add(valueField, gbc);
        
        // Tipo de valor
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Tipo de Valor:"), gbc);
        gbc.gridx = 1;
        valueTypeComboBox = new JComboBox<>(new String[]{"Quantity", "String", "CodeableConcept"});
        valueTypeComboBox.setSelectedIndex(0);
        inputPanel.add(valueTypeComboBox, gbc);
        
        // Unidad
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Unidad (ej: kg):"), gbc);
        gbc.gridx = 1;
        unitField = new JTextField(20);
        unitField.setText("kg");
        inputPanel.add(unitField, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        createButton = new JButton("Crear");
        createButton.addActionListener(e -> createObservation());
        buttonPanel.add(createButton);
        
        readButton = new JButton("Leer");
        readButton.addActionListener(e -> readObservation());
        buttonPanel.add(readButton);
        
        updateButton = new JButton("Actualizar");
        updateButton.addActionListener(e -> updateObservation());
        buttonPanel.add(updateButton);
        
        deleteButton = new JButton("Eliminar");
        deleteButton.addActionListener(e -> deleteObservation());
        buttonPanel.add(deleteButton);
        
        searchButton = new JButton("Buscar Todas");
        searchButton.addActionListener(e -> searchObservations());
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
        BasicAuthInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
        client.registerInterceptor(authInterceptor);
        
        // Agregar logging del cliente (diferente del del servidor)
        ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor clientLoggingInterceptor = 
            new ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor();
        clientLoggingInterceptor.setLogRequestSummary(true);
        clientLoggingInterceptor.setLogRequestBody(true);
        client.registerInterceptor(clientLoggingInterceptor);
    }
    
    private void createObservation() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Observation observation = new Observation();
            observation.setStatus(Observation.ObservationStatus.FINAL);
            
            // Código
            CodeableConcept code = new CodeableConcept();
            code.addCoding()
                .setSystem("http://loinc.org")
                .setCode(codeField.getText())
                .setDisplay(codeField.getText());
            observation.setCode(code);
            
            // Subject (Patient)
            if (!patientIdField.getText().isEmpty()) {
                observation.setSubject(new Reference("Patient/" + patientIdField.getText()));
            }
            
            // Valor
            String valueType = (String) valueTypeComboBox.getSelectedItem();
            if ("Quantity".equals(valueType)) {
                Quantity quantity = new Quantity();
                quantity.setValue(Double.parseDouble(valueField.getText()));
                quantity.setUnit(unitField.getText());
                quantity.setSystem("http://unitsofmeasure.org");
                quantity.setCode(unitField.getText());
                observation.setValue(quantity);
            } else if ("String".equals(valueType)) {
                observation.setValue(new StringType(valueField.getText()));
            }
            
            // Fecha
            observation.setEffective(new DateTimeType(new java.util.Date()));
            
            // Establecer ID si el usuario lo ingresó
            String userProvidedId = idField.getText().trim();
            if (!userProvidedId.isEmpty()) {
                observation.setId("Observation/" + userProvidedId);
                parent.log("ObservationPanel: Usando ID proporcionado por usuario: " + userProvidedId);
            }
            
            // Crear en servidor
            Observation created = (Observation) client.create().resource(observation).execute().getResource();
            
            resultArea.setText("✓ Observation creada exitosamente:\n\n");
            resultArea.append(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(created));
            
            idField.setText(created.getIdElement().getIdPart());
            parent.log("Observation creada: " + created.getIdElement().getIdPart());
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al crear Observation:\n" + e.getMessage());
            parent.log("Error al crear Observation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void readObservation() {
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
            Observation observation = client.read()
                .resource(Observation.class)
                .withId(id)
                .execute();
            
            resultArea.setText("✓ Observation encontrada:\n\n");
            resultArea.append(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation));
            
            // Llenar campos
            if (observation.hasSubject()) {
                String subjectRef = observation.getSubject().getReference();
                if (subjectRef.contains("/")) {
                    patientIdField.setText(subjectRef.split("/")[1]);
                }
            }
            if (observation.hasCode() && !observation.getCode().getCoding().isEmpty()) {
                codeField.setText(observation.getCode().getCodingFirstRep().getCode());
            }
            if (observation.hasValue()) {
                if (observation.getValue() instanceof Quantity) {
                    Quantity qty = (Quantity) observation.getValue();
                    valueField.setText(qty.getValue().toString());
                    unitField.setText(qty.getUnit());
                    valueTypeComboBox.setSelectedItem("Quantity");
                } else if (observation.getValue() instanceof StringType) {
                    valueField.setText(((StringType) observation.getValue()).getValue());
                    valueTypeComboBox.setSelectedItem("String");
                }
            }
            
            parent.log("Observation leída: " + id);
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al leer Observation:\n" + e.getMessage());
            parent.log("Error al leer Observation: " + e.getMessage());
        }
    }
    
    private void updateObservation() {
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
            Observation observation = new Observation();
            observation.setId(id);
            observation.setStatus(Observation.ObservationStatus.FINAL);
            
            // Código
            CodeableConcept code = new CodeableConcept();
            code.addCoding()
                .setSystem("http://loinc.org")
                .setCode(codeField.getText())
                .setDisplay(codeField.getText());
            observation.setCode(code);
            
            // Subject
            if (!patientIdField.getText().isEmpty()) {
                observation.setSubject(new Reference("Patient/" + patientIdField.getText()));
            }
            
            // Valor
            String valueType = (String) valueTypeComboBox.getSelectedItem();
            if ("Quantity".equals(valueType)) {
                Quantity quantity = new Quantity();
                quantity.setValue(Double.parseDouble(valueField.getText()));
                quantity.setUnit(unitField.getText());
                quantity.setSystem("http://unitsofmeasure.org");
                quantity.setCode(unitField.getText());
                observation.setValue(quantity);
            } else if ("String".equals(valueType)) {
                observation.setValue(new StringType(valueField.getText()));
            }
            
            // Actualizar
            Observation updated = (Observation) client.update().resource(observation).execute().getResource();
            
            resultArea.setText("✓ Observation actualizada exitosamente:\n\n");
            resultArea.append(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(updated));
            
            parent.log("Observation actualizada: " + id);
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al actualizar Observation:\n" + e.getMessage());
            parent.log("Error al actualizar Observation: " + e.getMessage());
        }
    }
    
    private void deleteObservation() {
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
            "¿Está seguro de eliminar la Observation con ID: " + id + "?", 
            "Confirmar eliminación", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            client.delete()
                .resourceById("Observation", id)
                .execute();
            
            resultArea.setText("✓ Observation eliminada exitosamente: " + id);
            clearFields();
            parent.log("Observation eliminada: " + id);
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al eliminar Observation:\n" + e.getMessage());
            parent.log("Error al eliminar Observation: " + e.getMessage());
        }
    }
    
    private void searchObservations() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Debe conectarse al servidor primero", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) client.search()
                .forResource(Observation.class)
                .execute();
            
            int total = bundle.getEntry().size();
            resultArea.setText("✓ Observations encontradas: " + total + "\n\n");
            
            for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Observation) {
                    Observation observation = (Observation) entry.getResource();
                    resultArea.append("ID: " + observation.getIdElement().getIdPart() + "\n");
                    if (observation.hasCode()) {
                        resultArea.append("Código: " + observation.getCode().getCodingFirstRep().getCode() + "\n");
                    }
                    if (observation.hasValue()) {
                        resultArea.append("Valor: " + observation.getValue().toString() + "\n");
                    }
                    resultArea.append("---\n");
                }
            }
            
            parent.log("Búsqueda de Observations realizada");
            
        } catch (Exception e) {
            resultArea.setText("✗ Error al buscar Observations:\n" + e.getMessage());
            parent.log("Error al buscar Observations: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        idField.setText("");
        patientIdField.setText("");
        codeField.setText("body-weight");
        valueField.setText("");
        valueTypeComboBox.setSelectedIndex(0);
        unitField.setText("kg");
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

