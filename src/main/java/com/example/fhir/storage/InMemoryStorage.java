package com.example.fhir.storage;

import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacenamiento en memoria para recursos FHIR
 * Usa ConcurrentHashMap para thread-safety
 */
public class InMemoryStorage {
    
    private static final InMemoryStorage instance = new InMemoryStorage();
    
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();
    private final Map<String, Observation> observations = new ConcurrentHashMap<>();
    
    private InMemoryStorage() {
        // Singleton
    }
    
    public static InMemoryStorage getInstance() {
        return instance;
    }
    
    // Operaciones Patient
    public void savePatient(Patient patient) {
        String id = patient.getIdElement().getIdPart();
        patients.put(id, patient);
    }
    
    public Patient getPatient(String id) {
        return patients.get(id);
    }
    
    public void deletePatient(String id) {
        patients.remove(id);
    }
    
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }
    
    // Operaciones Observation
    public void saveObservation(Observation observation) {
        String id = observation.getIdElement().getIdPart();
        observations.put(id, observation);
    }
    
    public Observation getObservation(String id) {
        return observations.get(id);
    }
    
    public void deleteObservation(String id) {
        observations.remove(id);
    }
    
    public List<Observation> getAllObservations() {
        return new ArrayList<>(observations.values());
    }
    
    // Limpiar almacenamiento (útil para pruebas)
    public void clear() {
        patients.clear();
        observations.clear();
    }
}

