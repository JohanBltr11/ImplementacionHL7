package com.example.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.fhir.storage.InMemoryStorage;
import com.example.fhir.util.ValidationUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

/**
 * Proveedor de recursos Patient
 * Implementa operaciones CRUD básicas con almacenamiento en memoria
 */
public class PatientProvider implements IResourceProvider {
    
    private final InMemoryStorage storage = InMemoryStorage.getInstance();
    
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }
    
    /**
     * Crear un nuevo Patient
     * POST /fhir/Patient
     */
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        // Validar recurso
        ValidationUtil.validateResource(patient);
        
        // Generar ID si no existe
        if (patient.getIdElement().isEmpty()) {
            String newId = "Patient-" + System.currentTimeMillis();
            patient.setId(newId);
        }
        
        // Guardar
        storage.savePatient(patient);
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(patient.getIdElement());
        outcome.setResource(patient);
        outcome.setCreated(true);
        
        return outcome;
    }
    
    /**
     * Leer un Patient por ID
     * GET /fhir/Patient/{id}
     */
    @Read
    public Patient readPatient(@IdParam IdType id) {
        Patient patient = storage.getPatient(id.getIdPart());
        if (patient == null) {
            throw new ResourceNotFoundException("Patient con ID '" + id.getIdPart() + "' no encontrado");
        }
        return patient;
    }
    
    /**
     * Actualizar un Patient
     * PUT /fhir/Patient/{id}
     */
    @Update
    public MethodOutcome updatePatient(@IdParam IdType id, @ResourceParam Patient patient) {
        // Validar recurso
        ValidationUtil.validateResource(patient);
        
        // Verificar que existe
        Patient existing = storage.getPatient(id.getIdPart());
        if (existing == null) {
            throw new ResourceNotFoundException("Patient con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        // Actualizar ID si es necesario
        patient.setId(id.getIdPart());
        
        // Guardar
        storage.savePatient(patient);
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(patient.getIdElement());
        outcome.setResource(patient);
        
        return outcome;
    }
    
    /**
     * Eliminar un Patient
     * DELETE /fhir/Patient/{id}
     */
    @Delete
    public MethodOutcome deletePatient(@IdParam IdType id) {
        Patient patient = storage.getPatient(id.getIdPart());
        if (patient == null) {
            throw new ResourceNotFoundException("Patient con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        storage.deletePatient(id.getIdPart());
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(id);
        
        return outcome;
    }
    
    /**
     * Buscar todos los Patients
     * GET /fhir/Patient
     */
    @Search
    public List<Patient> searchPatients() {
        return storage.getAllPatients();
    }
}

