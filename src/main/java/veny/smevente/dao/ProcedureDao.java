package veny.smevente.dao;

import veny.smevente.model.Event;
import veny.smevente.model.Procedure;

/**
 * Interface for persistence operation with <code>Procedure</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public interface ProcedureDao extends GenericDao<Procedure> {

    /**
     * Finds procedure according to given name.
     *
     * @param unitId ID to search in
     * @param name procedure name to search
     * @param type type of procedure
     * @return instance of found procedure
     */
    Procedure findByNameAndType(Object unitId, final String name, Event.Type type);

}
