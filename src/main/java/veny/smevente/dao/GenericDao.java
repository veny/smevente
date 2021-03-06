package veny.smevente.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.model.AbstractEntity;

/**
 * Interface for most common DAO operations.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 *
 * @param <T> the entity class
 */
public interface GenericDao<T extends AbstractEntity> {

    /**
     * This is a pretty dirty approach how to hand over some options
     * related to underlying storage engine
     * and keep the API clean at the same time.
     * It is storage to manage the DB operations with special options
     * like 'fetch strategy' or 'detaching' of objects.
     */
    ThreadLocal<Map<String, String>> OPTIONS_HOLDER = new ThreadLocal<Map<String, String>>() {
        /** {@inheritDoc} */
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };


    /**
     * Get entity by id.
     * @param id id of entity
     * @return desired entity
     * @throws ObjectNotFoundException if entity with given id is not found.
     */
    T getById(Object id) throws ObjectNotFoundException;

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
     * @return persisted entity
     */
    T persist(T entity);

    /**
     * Delete entity from storage.
     *
     * @param id id of entity
     */
    void remove(Object id);

//    /**
//     * Count entities.
//     * @return count all of target entity in database
//     */
//    Integer count();

    /**
     * Detaches entity and recursively the entire object tree.
     * All data contained in the document will be copied in the associated object.
     *
     * @param entity entity to be detached
     * @return entity with detached data
     */
    T detach(T entity);

    /**
     * Reloads a given object from database (with tree fetch plan and ignoring cache).
     *
     * @param entity object to be reloaded
     * @return the reloaded object
     */
    T reload(T entity);
}

