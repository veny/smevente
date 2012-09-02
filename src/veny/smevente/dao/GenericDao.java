package veny.smevente.dao;

import java.util.List;

/**
 * Interface for most common DAO operations.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 *
 * @param <T> the entity class
 */
public interface GenericDao< T > {

    /**
     * Get entity by id.
     * @param id id of entity
     * @return desired entity
     * @throws ObjectNotFoundException if entity with given id is not found.
     */
    T getById(String id) throws ObjectNotFoundException;

    /**
     * Get all entities of concrete type (excluding the deleted by SoftDelete entities).
     *
     * @return list of entities
     */
    List< T > getAll();

    /**
     * Get all entities of concrete type with or without the deleted by SoftDelete entities.
     *
     * @param withDeleted whether the soft deleted entities should be included
     * @return list of entities
     */
    List< T > getAll(boolean withDeleted);

    /**
     * Finds entities of concrete type by constructing the WHERE clause
     * with a given parameter and its value.
     *
     * @param paramName name of the WHERE parameter
     * @param value value of the WHERE parameter
     * @param orderBy ORDER BY attribute
     * @return list of entities
     */
    List< T > findBy(String paramName, Object value, String orderBy);

    /**
     * Finds entities of concrete type by constructing the WHERE clause
     * with a given parameter and its value.
     *
     * @param paramName1 name of the first WHERE parameter
     * @param value1 value of the first WHERE parameter
     * @param paramName2 name of the second WHERE parameter
     * @param value2 value of the second WHERE parameter
     * @param orderBy ORDER BY attribute
     * @return list of entities
     */
    List< T > findBy(String paramName1, Object value1, String paramName2, Object value2, String orderBy);

    /**
     * Gets single entity of concrete type by constructing the WHERE clause
     * with a given parameter and its value.
     *
     * @param paramName name of the WHERE parameter
     * @param value value of the WHERE parameter
     * @return found entity
     */
    T getBy(String paramName, Object value);

    /**
     * Store entity into storage.
     *
     * @param entity entity to store
     */
    void persist(T entity);

    /**
     * Merge entity into storage.
     *
     * @param entity the entity to merge
     * @return merged entity
     */
    T merge(T entity);

    /**
     * Delete entity from storage.
     *
     * @param entity the entity to delete
     */
    void remove(T entity);

    /**
     * Delete entity from storage by ID.
     *
     * @param entityId the entity ID to delete
     */
    void remove(String entityId);

//    /**
//     * Count entities.
//     * @return count all of target entity in database
//     */
//    Integer count();

}

