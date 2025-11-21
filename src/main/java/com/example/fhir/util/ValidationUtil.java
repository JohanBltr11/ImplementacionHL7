package com.example.fhir.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.Resource;

/**
 * Utilidad para validar recursos FHIR
 * Usa el validador integrado de HAPI FHIR
 */
public class ValidationUtil {
    
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
    private static final FhirValidator VALIDATOR = FHIR_CONTEXT.newValidator();
    
    /**
     * Valida un recurso FHIR
     * @param resource Recurso a validar
     * @throws UnprocessableEntityException Si el recurso no es válido
     */
    public static void validateResource(Resource resource) {
        ValidationResult result = VALIDATOR.validateWithResult(resource);
        
        if (!result.isSuccessful()) {
            StringBuilder errors = new StringBuilder();
            errors.append("El recurso no cumple con el estándar FHIR R4:\n");
            
            result.getMessages().forEach(message -> {
                errors.append("- ").append(message.getSeverity())
                      .append(": ").append(message.getMessage())
                      .append(" (en: ").append(message.getLocationString()).append(")\n");
            });
            
            throw new UnprocessableEntityException(errors.toString());
        }
    }
}

