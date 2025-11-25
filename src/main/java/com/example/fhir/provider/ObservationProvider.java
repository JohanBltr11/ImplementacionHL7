package com.example.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.fhir.model.User;
import com.example.fhir.storage.InMemoryStorage;
import com.example.fhir.util.AuthContext;
import com.example.fhir.util.ValidationUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;

import java.util.List;
import java.util.stream.Collectors;

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
     * Solo administradores pueden crear Observations
     */
    @Create
    public MethodOutcome createObservation(@ResourceParam Observation observation, RequestDetails requestDetails) {
        // Verificar permisos - solo admin puede crear
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("Solo los administradores pueden crear recursos Observation");
        }
        
        // Validar recurso
        ValidationUtil.validateResource(observation);
        
        // Usar ID del recurso si existe, o generar uno nuevo
        String finalId;
        if (observation.getIdElement().isEmpty()) {
            // Generar ID único
            do {
                finalId = "Observation-" + System.currentTimeMillis();
            } while (storage.getObservation(finalId) != null);
            observation.setId(finalId);
        } else {
            // Si el recurso ya tiene un ID, extraer solo la parte del ID (sin el tipo de recurso)
            String idPart = observation.getIdElement().getIdPart();
            // Si el ID incluye el tipo de recurso (ej: "Observation/123"), extraer solo "123"
            if (idPart.contains("/")) {
                idPart = idPart.substring(idPart.lastIndexOf("/") + 1);
            }
            finalId = idPart;
            
            // Verificar que el ID no exista ya
            if (storage.getObservation(finalId) != null) {
            throw new InvalidRequestException(
                "Ya existe una Observation con el ID '" + finalId + "'. Por favor, use un ID diferente."
            );
            }
            
            observation.setId("Observation/" + finalId);
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
     * Usuarios regulares solo pueden leer Observations de su Patient
     */
    @Read
    public Observation readObservation(@IdParam IdType id, RequestDetails requestDetails) {
        Observation observation = storage.getObservation(id.getIdPart());
        if (observation == null) {
            throw new ResourceNotFoundException("Observation con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        // Verificar permisos de acceso
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            // Verificar que la Observation pertenezca al Patient del usuario
            if (observation.hasSubject() && observation.getSubject().hasReference()) {
                String subjectRef = observation.getSubject().getReference();
                String patientId = subjectRef.contains("/") ? subjectRef.split("/")[1] : subjectRef;
                
                if (!patientId.equals(user.getPatientId())) {
                    throw new ForbiddenOperationException("No tiene permisos para acceder a este recurso Observation");
                }
            } else {
                throw new ForbiddenOperationException("No tiene permisos para acceder a este recurso Observation");
            }
        }
        
        return observation;
    }
    
    /**
     * Actualizar una Observation
     * PUT /fhir/Observation/{id}
     * Solo administradores pueden actualizar
     */
    @Update
    public MethodOutcome updateObservation(@IdParam IdType id, @ResourceParam Observation observation, RequestDetails requestDetails) {
        // Verificar permisos - solo admin puede actualizar
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("Solo los administradores pueden actualizar recursos Observation");
        }
        
        // Validar recurso
        ValidationUtil.validateResource(observation);
        
        // Verificar que existe
        Observation existing = storage.getObservation(id.getIdPart());
        if (existing == null) {
            throw new ResourceNotFoundException("Observation con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        // Verificar si el recurso tiene un ID diferente al parámetro
        String resourceId = observation.getIdElement().getIdPart();
        if (resourceId.contains("/")) {
            resourceId = resourceId.substring(resourceId.lastIndexOf("/") + 1);
        }
        
        // Si el ID del recurso es diferente al ID del parámetro, verificar que no exista ya
        if (!resourceId.equals(id.getIdPart()) && storage.getObservation(resourceId) != null) {
            throw new InvalidRequestException(
                "Ya existe una Observation con el ID '" + resourceId + "'. No se puede cambiar el ID a uno que ya existe."
            );
        }
        
        // Actualizar ID si es necesario
        observation.setId("Observation/" + id.getIdPart());
        
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
     * Solo administradores pueden eliminar
     */
    @Delete
    public MethodOutcome deleteObservation(@IdParam IdType id, RequestDetails requestDetails) {
        // Verificar permisos - solo admin puede eliminar
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("Solo los administradores pueden eliminar recursos Observation");
        }
        
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
     * Usuarios regulares solo ven Observations de su Patient
     */
    @Search
    public List<Observation> searchObservations(RequestDetails requestDetails) {
        User user = getUserFromRequest(requestDetails);
        
        // Administradores ven todas las Observations
        if (user.isAdmin()) {
            return storage.getAllObservations();
        }
        
        // Usuarios regulares solo ven Observations de su Patient
        if (user.getPatientId() != null && !user.getPatientId().isEmpty()) {
            String patientRef = "Patient/" + user.getPatientId();
            return storage.getAllObservations().stream()
                .filter(obs -> obs.hasSubject() && 
                              obs.getSubject().hasReference() && 
                              obs.getSubject().getReference().equals(patientRef))
                .collect(Collectors.toList());
        }
        
        // Si el usuario no tiene Patient asociado, retorna lista vacía
        return List.of();
    }
    
    /**
     * Obtener el usuario autenticado desde RequestDetails o ThreadLocal
     */
    private User getUserFromRequest(RequestDetails requestDetails) {
        // Primero intentar obtener de ThreadLocal (más confiable)
        User user = AuthContext.getUser();
        
        // Si no está en ThreadLocal, intentar desde RequestDetails
        if (user == null && requestDetails != null) {
            user = (User) requestDetails.getAttribute("USER");
        }
        
        if (user == null) {
            throw new ForbiddenOperationException("Usuario no autenticado. El SecurityInterceptor no se ejecutó correctamente.");
        }
        
        return user;
    }
}

