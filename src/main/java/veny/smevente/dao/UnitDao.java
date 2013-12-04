package veny.smevente.dao;

import java.util.List;

import veny.smevente.model.Unit;

/**
 * Interface for persistence operation with <code>Unit</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 2.9.2012
 */
public interface UnitDao extends GenericDao<Unit> {

   /**
    * Gets units for given user.
    *
    * @param userId user ID
    * @return list of units where given user is member in
    */
    List<Unit> getUnitsByUser(Object userId);

}
