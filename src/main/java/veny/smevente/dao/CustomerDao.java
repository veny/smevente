package veny.smevente.dao;

import java.util.List;

import veny.smevente.model.Customer;

/**
 * Interface for persistence operation with <code>Customer</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public interface CustomerDao extends GenericDao<Customer> {

    /**
     * Finds customers according to given parameter name and its value.
     * @param unitId ID to search in
     * @param paramName parameter name
     * @param value parameter value
     * @return list of found customers
     */
    List<Customer> findLikeBy(Object unitId, String paramName, Object value);

    /**
     * Gets customer according to given birth number.
     *
     * @param unitId ID to search in
     * @param birthNumber birth number to search
     * @return found customer or <i>null</i> if not found
     */
    Customer findByBirthNumber(Object unitId, String birthNumber);

}
