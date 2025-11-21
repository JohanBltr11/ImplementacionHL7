package com.example.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.fhir.storage.InMemoryStorage;
import com.example.fhir.util.ValidationUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;

import java.util.List;

/**
 * Proveedor de recursos Observation
 * Implementa operaciones CRUD básicas con almacenamiento en memoria
 */
public class ObservationProvider implements IResourceProvider {
    
    private final InMemoryStorage storage = InMemoryStorage.getInstance();
    
    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }
    
    /**
     * Crear una nueva Observation
     * POST /fhir/Observation
     */
    @Create
    public MethodOutcome createObservation(@ResourceParam Observation observation) {
        // Validar recurso
        ValidationUtil.validateResource(observation);
        
        // Generar ID si no existe
        if (observation.getIdElement().isEmpty()) {
            String newId = "Observation-" + System.currentTimeMillis();
            observation.setId(newId);
        }
        
        // Guardar
        storage.saveObservation(observation);
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(observation.getIdElement());
        outcome.setResource(observation);
        outcome.setCreated(true);
        
        return outcome;
    }
    
    /**
     * Leer una Observation por ID
     * GET /fhir/Observation/{id}
     */
    @Read
    public Observation readObservation(@IdParam IdType id) {
        Observation observation = storage.getObservation(id.getIdPart());
        if (observation == null) {
            throw new ResourceNotFoundException("Observation con ID '" + id.getIdPart() + "' no encontrado");
        }
        return observation;
    }
    
    /**
     * Actualizar una Observation
     * PUT /fhir/Observation/{id}
     */
    @Update
    public MethodOutcome updateObservation(@IdParam IdType id, @ResourceParam Observation observation) {
        // Validar recurso
        ValidationUtil.validateResource(observation);
        
        // Verificar que existe
        Observation existing = storage.getObservation(id.getIdPart());
        if (existing == null) {
            throw new ResourceNotFoundException("Observation con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        // Actualizar ID si es necesario
        observation.setId(id.getIdPart());
        
        // Guardar
        storage.saveObservation(observation);
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(observation.getIdElement());
        outcome.setResource(observation);
        
        return outcome;
    }
    
    /**
     * Eliminar una Observation
     * DELETE /fhir/Observation/{id}
     */
    @Delete
    public MethodOutcome deleteObservation(@IdParam IdType id) {
        Observation observation = storage.getObservation(id.getIdPart());
        if (observation == null) {
            throw new ResourceNotFoundException("Observation con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        storage.deleteObservation(id.getIdPart());
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(id);
        
        return outcome;
    }
    
    /**
     * Buscar todas las Observations
     * GET /fhir/Observation
     */
    @Search
    public List<Observation> searchObservations() {
        return storage.getAllObservations();
    }
}

