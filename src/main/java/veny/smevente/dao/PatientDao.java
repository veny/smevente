package veny.smevente.dao;

import java.util.List;

import veny.smevente.model.Patient;

/**
 * Interface for persistence operation with <code>Patient</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public interface PatientDao extends GenericDao<Patient> {

    /**
     * Finds patients according to given parameter name and its value.
     * @param unitId ID to search in
     * @param paramName parameter name
     * @param value parameter value
     * @return list of found patients
     */
    List<Patient> findLikeBy(Object unitId, String paramName, Object value);

    /**
     * Gets patient according to given birth number.
     *
     * @param unitId ID to search in
     * @param birthNumber birth number to search
     * @return found patient or <i>null</i> if not found
     */
    Patient findByBirthNumber(Object unitId, String birthNumber);

}
